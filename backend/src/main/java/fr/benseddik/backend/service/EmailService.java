package fr.benseddik.backend.service;

/**
 * Service d'envoi d'emails.
 */
public interface EmailService {

    /**
     * Envoie un email de vérification d'adresse.
     *
     * @param to Email destinataire
     * @param firstName Prénom de l'utilisateur
     * @param verificationLink Lien de vérification
     */
    void sendVerificationEmail(String to, String firstName, String verificationLink);

    /**
     * Envoie un email de réinitialisation de mot de passe.
     *
     * @param to Email destinataire
     * @param firstName Prénom de l'utilisateur
     * @param resetLink Lien de réinitialisation
     */
    void sendPasswordResetEmail(String to, String firstName, String resetLink);

    /**
     * Envoie un email de confirmation de changement de mot de passe.
     *
     * @param to Email destinataire
     * @param firstName Prénom de l'utilisateur
     */
    void sendPasswordChangedEmail(String to, String firstName);

    /**
     * Envoie un email de bienvenue.
     *
     * @param to Email destinataire
     * @param firstName Prénom de l'utilisateur
     */
    void sendWelcomeEmail(String to, String firstName);
}