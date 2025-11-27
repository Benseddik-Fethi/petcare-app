package fr.benseddik.backend.service.impl;

import fr.benseddik.backend.domain.AuditLog;
import fr.benseddik.backend.domain.PasswordResetToken;
import fr.benseddik.backend.domain.User;
import fr.benseddik.backend.domain.VerificationToken;
import fr.benseddik.backend.dto.request.ChangePasswordRequest;
import fr.benseddik.backend.dto.request.ForgotPasswordRequest;
import fr.benseddik.backend.dto.request.ResendVerificationRequest;
import fr.benseddik.backend.dto.request.ResetPasswordRequest;
import fr.benseddik.backend.dto.response.UserResponse;
import fr.benseddik.backend.exception.BadRequestException;
import fr.benseddik.backend.exception.ResourceNotFoundException;
import fr.benseddik.backend.repository.AuditLogRepository;
import fr.benseddik.backend.repository.PasswordResetTokenRepository;
import fr.benseddik.backend.repository.SessionRepository;
import fr.benseddik.backend.repository.UserRepository;
import fr.benseddik.backend.repository.VerificationTokenRepository;
import fr.benseddik.backend.service.EmailService;
import fr.benseddik.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Implémentation du service utilisateur.
 *
 * 🛡️ Sécurité :
 * - Tokens à usage unique
 * - Expiration des tokens
 * - Hashage des mots de passe (Argon2)
 * - Audit des actions sensibles
 * - Messages génériques (pas de fuite d'information)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final SessionRepository sessionRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    // ═══════════════════════════════════════════════════════════════════════════
    // VÉRIFICATION D'EMAIL
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void sendVerificationEmail(UUID userId) {
        User user = findUserById(userId);

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            log.debug("Email déjà vérifié pour: {}", user.getEmail());
            return;
        }

        // Supprimer l'ancien token s'il existe
        verificationTokenRepository.deleteByUserId(userId);

        // Créer un nouveau token
        VerificationToken token = VerificationToken.create(user);
        verificationTokenRepository.save(token);

        // Envoyer l'email
        String verificationLink = buildVerificationLink(token.getToken());
        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFirstName() != null ? user.getFirstName() : "Utilisateur",
                verificationLink
        );

        log.info("Email de vérification envoyé à: {}", user.getEmail());
    }

    @Override
    public void resendVerificationEmail(ResendVerificationRequest request) {
        // 🛡️ Message générique pour ne pas révéler si l'email existe
        userRepository.findByEmail(request.email())
                .filter(user -> !Boolean.TRUE.equals(user.getEmailVerified()))
                .ifPresent(user -> sendVerificationEmail(user.getId()));

        log.debug("Demande de renvoi de vérification pour: {}", request.email());
    }

    @Override
    public boolean verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository
                .findValidByToken(token, Instant.now())
                .orElse(null);

        if (verificationToken == null) {
            log.warn("Token de vérification invalide ou expiré");
            return false;
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        // Supprimer le token (usage unique)
        verificationTokenRepository.delete(verificationToken);

        // Envoyer un email de bienvenue
        emailService.sendWelcomeEmail(
                user.getEmail(),
                user.getFirstName() != null ? user.getFirstName() : "Utilisateur"
        );

        log.info("Email vérifié avec succès pour: {}", user.getEmail());
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RÉINITIALISATION DE MOT DE PASSE
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        // 🛡️ Message générique - ne pas révéler si l'email existe
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            // Invalider les anciens tokens
            passwordResetTokenRepository.invalidateAllUserTokens(user.getId());

            // Créer un nouveau token
            PasswordResetToken token = PasswordResetToken.create(user);
            passwordResetTokenRepository.save(token);

            // Envoyer l'email
            String resetLink = buildPasswordResetLink(token.getToken());
            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getFirstName() != null ? user.getFirstName() : "Utilisateur",
                    resetLink
            );

            log.info("Email de réinitialisation envoyé à: {}", user.getEmail());
        });

        log.debug("Demande de réinitialisation pour: {}", request.email());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isResetTokenValid(String token) {
        return passwordResetTokenRepository
                .findValidByToken(token, Instant.now())
                .isPresent();
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findValidByToken(request.token(), Instant.now())
                .orElseThrow(() -> new BadRequestException("Token invalide ou expiré"));

        User user = resetToken.getUser();

        // Mettre à jour le mot de passe
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Marquer le token comme utilisé
        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);

        // Révoquer toutes les sessions existantes (sécurité)
        sessionRepository.revokeAllUserSessions(user.getId(), Instant.now());

        // Envoyer un email de confirmation
        emailService.sendPasswordChangedEmail(
                user.getEmail(),
                user.getFirstName() != null ? user.getFirstName() : "Utilisateur"
        );

        // Audit
        auditLogRepository.save(AuditLog.passwordChanged(user, "password_reset"));

        log.info("Mot de passe réinitialisé pour: {}", user.getEmail());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CHANGEMENT DE MOT DE PASSE
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findUserById(userId);

        // Vérifier le mot de passe actuel
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Mot de passe actuel incorrect");
        }

        // Vérifier que le nouveau mot de passe est différent
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        // Mettre à jour le mot de passe
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Envoyer un email de confirmation
        emailService.sendPasswordChangedEmail(
                user.getEmail(),
                user.getFirstName() != null ? user.getFirstName() : "Utilisateur"
        );

        // Audit
        auditLogRepository.save(AuditLog.passwordChanged(user, "user_change"));

        log.info("Mot de passe changé pour: {}", user.getEmail());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PROFIL
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = findUserById(userId);
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return UserResponse.fromEntity(user);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════════════════════

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private String buildVerificationLink(String token) {
        return frontendUrl + "/auth/verify-email?token=" + token;
    }

    private String buildPasswordResetLink(String token) {
        return frontendUrl + "/auth/reset-password?token=" + token;
    }
}