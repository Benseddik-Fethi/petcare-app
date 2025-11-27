package fr.benseddik.backend.service;

import fr.benseddik.backend.dto.request.ChangePasswordRequest;
import fr.benseddik.backend.dto.request.ForgotPasswordRequest;
import fr.benseddik.backend.dto.request.ResendVerificationRequest;
import fr.benseddik.backend.dto.request.ResetPasswordRequest;
import fr.benseddik.backend.dto.response.UserResponse;

import java.util.UUID;

/**
 * Service de gestion des utilisateurs.
 *
 * Responsabilités :
 * - Vérification d'email
 * - Réinitialisation de mot de passe
 * - Changement de mot de passe
 * - Gestion du profil
 */
public interface UserService {

    // ═══════════════════════════════════════════════════════════════════════════
    // VÉRIFICATION D'EMAIL
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Envoie un email de vérification à l'utilisateur.
     *
     * @param userId ID de l'utilisateur
     */
    void sendVerificationEmail(UUID userId);

    /**
     * Renvoie l'email de vérification.
     *
     * @param request Email de l'utilisateur
     */
    void resendVerificationEmail(ResendVerificationRequest request);

    /**
     * Vérifie l'adresse email avec le token.
     *
     * @param token Token de vérification
     * @return true si la vérification a réussi
     */
    boolean verifyEmail(String token);

    // ═══════════════════════════════════════════════════════════════════════════
    // RÉINITIALISATION DE MOT DE PASSE
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Demande une réinitialisation de mot de passe.
     * Envoie un email avec le lien de réinitialisation.
     *
     * @param request Email de l'utilisateur
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Vérifie si un token de reset est valide.
     *
     * @param token Token de réinitialisation
     * @return true si le token est valide
     */
    boolean isResetTokenValid(String token);

    /**
     * Réinitialise le mot de passe avec le token.
     *
     * @param request Token et nouveau mot de passe
     */
    void resetPassword(ResetPasswordRequest request);

    // ═══════════════════════════════════════════════════════════════════════════
    // CHANGEMENT DE MOT DE PASSE
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Change le mot de passe de l'utilisateur connecté.
     *
     * @param userId ID de l'utilisateur
     * @param request Mot de passe actuel et nouveau
     */
    void changePassword(UUID userId, ChangePasswordRequest request);

    // ═══════════════════════════════════════════════════════════════════════════
    // PROFIL
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Récupère les informations d'un utilisateur.
     *
     * @param userId ID de l'utilisateur
     * @return Informations utilisateur
     */
    UserResponse getUserById(UUID userId);

    /**
     * Récupère un utilisateur par son email.
     *
     * @param email Email de l'utilisateur
     * @return Informations utilisateur
     */
    UserResponse getUserByEmail(String email);
}