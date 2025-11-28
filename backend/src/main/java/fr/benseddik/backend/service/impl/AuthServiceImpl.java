package fr.benseddik.backend.service.impl;

import fr.benseddik.backend.config.JwtProperties;
import fr.benseddik.backend.config.SecurityProperties;
import fr.benseddik.backend.domain.*;
import fr.benseddik.backend.dto.request.LoginRequest;
import fr.benseddik.backend.dto.request.OAuthCodeExchangeRequest;
import fr.benseddik.backend.dto.request.RefreshTokenRequest;
import fr.benseddik.backend.dto.request.RegisterRequest;
import fr.benseddik.backend.dto.response.AuthResponse;
import fr.benseddik.backend.dto.response.UserResponse;
import fr.benseddik.backend.exception.AccountLockedException;
import fr.benseddik.backend.exception.AuthenticationException;
import fr.benseddik.backend.exception.BadRequestException;
import fr.benseddik.backend.repository.AuditLogRepository;
import fr.benseddik.backend.repository.OAuthAuthorizationCodeRepository;
import fr.benseddik.backend.repository.SessionRepository;
import fr.benseddik.backend.repository.UserRepository;
import fr.benseddik.backend.repository.VerificationTokenRepository;
import fr.benseddik.backend.security.CustomUserDetails;
import fr.benseddik.backend.service.AuthService;
import fr.benseddik.backend.service.EmailService;
import fr.benseddik.backend.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Implémentation du service d'authentification.
 *
 * 🛡️ Sécurité :
 * - Hashage Argon2 des mots de passe
 * - Protection brute force (verrouillage après N tentatives)
 * - Audit des connexions
 * - Tokens hashés en BDD
 * - Échange de code OAuth2 sécurisé (code à usage unique, 30 sec)
 * - Email de vérification à l'inscription
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final AuditLogRepository auditLogRepository;
    private final OAuthAuthorizationCodeRepository authorizationCodeRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final JwtProperties jwtProperties;
    private final SecurityProperties securityProperties;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Un compte existe déjà avec cet email");
        }

        // Créer l'utilisateur
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(Role.OWNER)
                .provider(AuthProvider.EMAIL)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        log.info("Nouvel utilisateur inscrit: {}", user.getEmail());

        // Envoyer l'email de vérification
        sendVerificationEmail(user);

        // Générer les tokens
        return createAuthResponse(user, httpRequest);
    }

    /**
     * Envoie l'email de vérification à un nouvel utilisateur.
     */
    private void sendVerificationEmail(User user) {
        // Créer un token de vérification
        VerificationToken token = VerificationToken.create(user);
        verificationTokenRepository.save(token);

        // Construire le lien
        String verificationLink = frontendUrl + "/auth/verify-email?token=" + token.getToken();

        // Envoyer l'email (asynchrone)
        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFirstName() != null ? user.getFirstName() : "Utilisateur",
                verificationLink
        );

        log.debug("Email de vérification envoyé à: {}", user.getEmail());
    }

    /**
     * 🛡️ SÉCURITÉ : Protection contre les timing attacks.
     *
     * Cette méthode exécute toujours le hashage du mot de passe (opération coûteuse)
     * même si l'utilisateur n'existe pas, pour éviter qu'un attaquant puisse déduire
     * l'existence d'un compte en mesurant le temps de réponse.
     *
     * Timing constant : ~100-500ms (temps de hashage Argon2) dans tous les cas.
     */
    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        // Rechercher l'utilisateur
        User user = userRepository.findByEmail(request.email()).orElse(null);

        // 🛡️ PROTECTION TIMING ATTACK : Toujours hasher le mot de passe
        // Même si l'utilisateur n'existe pas, on hash pour avoir un temps de réponse constant
        boolean passwordMatches = false;
        if (user != null) {
            passwordMatches = passwordEncoder.matches(request.password(), user.getPasswordHash());
        } else {
            // Hash factice pour simuler le temps de vérification (protection timing attack)
            // Utilise un hash Argon2 pré-calculé pour éviter de générer un nouveau salt à chaque fois
            // Format : $argon2id$v=19$m=65536,t=4,p=4$salt$hash
            passwordEncoder.matches(
                request.password(),
                // Hash factice d'un mot de passe aléatoire (jamais utilisé, juste pour le timing)
                // Paramètres: m=65536 (64MB), t=4 (iterations), p=4 (parallelism)
                "$argon2id$v=19$m=65536,t=4,p=4$AAAAAAAAAAAAAAAAAAAAAA$AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
            );

            // Log l'échec (utilisateur inexistant) APRÈS le hashage
            auditLogRepository.save(AuditLog.loginFailed(
                    request.email(), ip, userAgent, "User not found"
            ));
            throw new AuthenticationException("Email ou mot de passe incorrect");
        }

        // Vérifier si le compte est verrouillé
        if (user.isAccountLocked()) {
            log.warn("Tentative de connexion sur compte verrouillé: {}", user.getEmail());
            throw new AccountLockedException(user.getLockedUntil());
        }

        // Vérifier le résultat du mot de passe
        if (!passwordMatches) {
            handleFailedLogin(user, ip, userAgent);
            throw new AuthenticationException("Email ou mot de passe incorrect");
        }

        // Réinitialiser les tentatives échouées
        user.resetFailedLoginAttempts();
        userRepository.save(user);

        // Log le succès
        auditLogRepository.save(AuditLog.loginSuccess(user, ip, userAgent));

        log.info("Connexion réussie: {}", user.getEmail());

        return createAuthResponse(user, httpRequest);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String refreshToken = request.refreshToken();

        // Valider le token
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new AuthenticationException("Refresh token invalide ou expiré");
        }

        // Vérifier que c'est bien un refresh token (pas un access token)
        if (jwtService.isAccessToken(refreshToken)) {
            throw new AuthenticationException("Token invalide");
        }

        // Rechercher la session en BDD
        String tokenHash = jwtService.hashToken(refreshToken);
        Session session = sessionRepository.findValidByRefreshTokenHash(tokenHash, Instant.now())
                .orElseThrow(() -> new AuthenticationException("Session invalide ou expirée"));

        // Récupérer l'utilisateur
        User user = session.getUser();

        // Révoquer l'ancienne session
        session.revoke();
        sessionRepository.save(session);

        // Créer de nouveaux tokens
        return createAuthResponse(user, httpRequest);
    }

    @Override
    public AuthResponse exchangeOAuthCode(OAuthCodeExchangeRequest request) {
        // Rechercher le code valide
        OAuthAuthorizationCode authCode = authorizationCodeRepository
                .findValidByCode(request.code(), Instant.now())
                .orElseThrow(() -> new AuthenticationException("Code d'autorisation invalide ou expiré"));

        // Marquer le code comme utilisé (usage unique !)
        authCode.markAsUsed();
        authorizationCodeRepository.save(authCode);

        // Récupérer les tokens pré-générés
        User user = authCode.getUser();

        log.info("Code OAuth2 échangé avec succès pour: {}", user.getEmail());

        // Calculer l'expiration
        long expiresIn = jwtProperties.accessToken().expiration().getSeconds();

        return new AuthResponse(
                authCode.getAccessToken(),
                authCode.getRefreshToken(),
                expiresIn,
                UserResponse.fromEntity(user)
        );
    }

    @Override
    public void logout(String refreshToken, HttpServletRequest httpRequest) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        String tokenHash = jwtService.hashToken(refreshToken);
        sessionRepository.findValidByRefreshTokenHash(tokenHash, Instant.now())
                .ifPresent(session -> {
                    session.revoke();
                    sessionRepository.save(session);

                    // Log
                    auditLogRepository.save(AuditLog.logout(
                            session.getUser(),
                            getClientIp(httpRequest)
                    ));

                    log.info("Déconnexion: {}", session.getUser().getEmail());
                });
    }

    @Override
    public void logoutAll(HttpServletRequest httpRequest) {
        // Récupérer l'utilisateur authentifié
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        UUID userId = userDetails.getId();

        // Révoquer toutes les sessions
        int revokedCount = sessionRepository.revokeAllUserSessions(userId, Instant.now());

        log.info("Toutes les sessions révoquées ({}) pour: {}", revokedCount, userDetails.getEmail());
    }

    /**
     * Crée une réponse d'authentification avec les tokens.
     */
    private AuthResponse createAuthResponse(User user, HttpServletRequest httpRequest) {
        // Générer les tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Créer la session
        Session session = Session.builder()
                .user(user)
                .refreshTokenHash(jwtService.hashToken(refreshToken))
                .ipAddress(getClientIp(httpRequest))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .expiresAt(Instant.now().plus(jwtProperties.refreshToken().expiration()))
                .build();

        sessionRepository.save(session);

        // Calculer l'expiration en secondes
        long expiresIn = jwtProperties.accessToken().expiration().getSeconds();

        return new AuthResponse(
                accessToken,
                refreshToken,
                expiresIn,
                UserResponse.fromEntity(user)
        );
    }

    /**
     * Gère une tentative de connexion échouée.
     */
    private void handleFailedLogin(User user, String ip, String userAgent) {
        SecurityProperties.BruteForce bruteForce = securityProperties.bruteForce();

        user.recordFailedLogin(
                bruteForce.maxAttempts(),
                (int) bruteForce.lockDuration().toMinutes()
        );
        userRepository.save(user);

        // Log l'échec
        auditLogRepository.save(AuditLog.loginFailed(
                user.getEmail(), ip, userAgent, "Invalid password"
        ));

        // Si le compte vient d'être verrouillé
        if (user.isAccountLocked()) {
            auditLogRepository.save(AuditLog.accountLocked(user, ip));
            log.warn("Compte verrouillé après {} tentatives: {}",
                    bruteForce.maxAttempts(), user.getEmail());
        }
    }

    /**
     * Récupère l'adresse IP du client.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}