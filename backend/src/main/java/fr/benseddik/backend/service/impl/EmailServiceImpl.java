package fr.benseddik.backend.service.impl;

import fr.benseddik.backend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

/**
 * Implémentation du service d'envoi d'emails avec Thymeleaf.
 *
 * 📧 Templates dans : /resources/templates/email/
 *
 * En mode développement (mail.enabled=false) : logs uniquement.
 * En production : envoi via SMTP.
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine emailTemplateEngine;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:noreply@petcare.com}")
    private String fromAddress;

    @Value("${app.name:PetCare}")
    private String appName;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Qualifier("emailTemplateEngine") SpringTemplateEngine emailTemplateEngine
    ) {
        this.mailSender = mailSender;
        this.emailTemplateEngine = emailTemplateEngine;
    }

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:noreply@petcare.com}")
    private String fromAddress;

    @Value("${app.name:PetCare}")
    private String appName;

    @Override
    @Async
    public void sendVerificationEmail(String to, String firstName, String verificationLink) {
        Map<String, Object> variables = Map.of(
                "firstName", firstName,
                "verificationLink", verificationLink,
                "appName", appName
        );

        String subject = "Vérifiez votre adresse email - " + appName;
        sendTemplatedEmail(to, subject, "verification", variables);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String firstName, String resetLink) {
        Map<String, Object> variables = Map.of(
                "firstName", firstName,
                "resetLink", resetLink,
                "appName", appName
        );

        String subject = "Réinitialisation de votre mot de passe - " + appName;
        sendTemplatedEmail(to, subject, "password-reset", variables);
    }

    @Override
    @Async
    public void sendPasswordChangedEmail(String to, String firstName) {
        Map<String, Object> variables = Map.of(
                "firstName", firstName,
                "appName", appName
        );

        String subject = "Votre mot de passe a été modifié - " + appName;
        sendTemplatedEmail(to, subject, "password-changed", variables);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String firstName) {
        Map<String, Object> variables = Map.of(
                "firstName", firstName,
                "appName", appName
        );

        String subject = "Bienvenue sur " + appName + " !";
        sendTemplatedEmail(to, subject, "welcome", variables);
    }

    /**
     * Envoie un email avec un template Thymeleaf.
     *
     * @param to Destinataire
     * @param subject Sujet
     * @param templateName Nom du template (sans extension)
     * @param variables Variables à injecter dans le template
     */
    private void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        // Créer le contexte Thymeleaf
        Context context = new Context();
        context.setVariables(variables);

        // Générer le contenu HTML
        String htmlContent = emailTemplateEngine.process(templateName, context);

        // Envoyer ou logger
        if (!mailEnabled) {
            log.info("📧 [DEV MODE] Email à {} - Sujet: {} - Template: {}", to, subject, templateName);
            log.debug("📧 Variables: {}", variables);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("📧 Email envoyé à {} - Template: {}", to, templateName);

        } catch (MessagingException e) {
            log.error("❌ Erreur envoi email à {}: {}", to, e.getMessage());
        }
    }
}