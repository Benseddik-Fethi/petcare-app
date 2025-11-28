package fr.benseddik.backend.security.oauth2;

import fr.benseddik.backend.domain.AuditLog;
import fr.benseddik.backend.domain.AuthProvider;
import fr.benseddik.backend.domain.Role;
import fr.benseddik.backend.domain.Session;
import fr.benseddik.backend.domain.User;
import fr.benseddik.backend.repository.AuditLogRepository;
import fr.benseddik.backend.repository.SessionRepository;
import fr.benseddik.backend.repository.UserRepository;
import fr.benseddik.backend.security.CookieUtils;
import fr.benseddik.backend.service.JwtService;
import fr.benseddik.backend.config.JwtProperties;
import fr.benseddik.backend.util.IpAddressResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Handler de succès OAuth2 pour Google Login.
 *
 * 🔄 Workflow sécurisé avec cookies HTTP-only :
 * 1. Récupère les infos utilisateur de Google
 * 2. Crée ou met à jour l'utilisateur en BDD
 * 3. Génère les tokens JWT
 * 4. Stocke les tokens en cookies HTTP-only
 * 5. Redirige vers le frontend
 *
 * 🛡️ Sécurité :
 * - Tokens en cookies HTTP-only (inaccessibles au JavaScript)
 * - Secure flag (HTTPS uniquement en prod)
 * - SameSite=Strict (protection CSRF)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final AuditLogRepository auditLogRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final CookieUtils cookieUtils;
    private final IpAddressResolver ipAddressResolver;

    @Value("${app.security.cors.allowed-origins:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId();

        log.debug("OAuth2 success - Provider: {}", provider);

        // Extraire les informations utilisateur
        String email = oAuth2User.getAttribute("email");
        String providerId = oAuth2User.getAttribute("sub");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String avatar = oAuth2User.getAttribute("picture");

        if (email == null) {
            log.error("Email non fourni par le provider OAuth2");
            response.sendRedirect(frontendUrl + "/auth/error?message=email_required");
            return;
        }

        // Créer ou récupérer l'utilisateur
        User user = findOrCreateUser(email, providerId, firstName, lastName, avatar, provider);

        // Générer les tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Créer la session
        createSession(user, refreshToken, request);

        // 🛡️ Stocker les tokens en cookies HTTP-only
        cookieUtils.addAccessTokenCookie(response, accessToken);
        cookieUtils.addRefreshTokenCookie(response, refreshToken);

        // Log d'audit
        auditLogRepository.save(AuditLog.oauthLogin(
                user,
                AuthProvider.valueOf(provider.toUpperCase()),
                ipAddressResolver.resolveClientIp(request),
                request.getHeader("User-Agent")
        ));

        log.info("OAuth2 login réussi pour: {} - Tokens en cookies HTTP-only", email);

        // Rediriger vers le frontend (sans tokens dans l'URL !)
        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/auth/callback");
    }

    private User findOrCreateUser(
            String email,
            String providerId,
            String firstName,
            String lastName,
            String avatar,
            String provider
    ) {
        return userRepository.findByGoogleId(providerId)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existingUser -> {
                            existingUser.setGoogleId(providerId);
                            if (existingUser.getAvatar() == null) {
                                existingUser.setAvatar(avatar);
                            }
                            return userRepository.save(existingUser);
                        })
                        .orElseGet(() -> {
                            User newUser = User.builder()
                                    .email(email)
                                    .firstName(firstName)
                                    .lastName(lastName)
                                    .avatar(avatar)
                                    .googleId(providerId)
                                    .provider(AuthProvider.GOOGLE)
                                    .role(Role.OWNER)
                                    .emailVerified(true)
                                    .build();

                            log.info("Nouvel utilisateur créé via OAuth2: {}", email);
                            return userRepository.save(newUser);
                        }));
    }

    private void createSession(User user, String refreshToken, HttpServletRequest request) {
        Session session = Session.builder()
                .user(user)
                .refreshTokenHash(jwtService.hashToken(refreshToken))
                .ipAddress(ipAddressResolver.resolveClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .expiresAt(Instant.now().plus(jwtProperties.refreshToken().expiration()))
                .build();

        sessionRepository.save(session);
    }
}