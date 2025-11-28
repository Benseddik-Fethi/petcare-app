package fr.benseddik.backend.config;

import fr.benseddik.backend.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 🛡️ Tâches planifiées pour la sécurité et la maintenance.
 *
 * Opérations périodiques :
 * - Nettoyage des sessions expirées (tokens JWT refresh)
 * - Purge des tokens de vérification email expirés
 * - Purge des tokens de réinitialisation mot de passe expirés
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final SessionRepository sessionRepository;

    /**
     * 🛡️ SÉCURITÉ : Nettoyage quotidien des sessions expirées.
     *
     * Exécution : Tous les jours à 2h00 du matin (heure serveur)
     * Objectif : Éviter l'accumulation de sessions expirées en base de données
     *
     * CRON : "0 0 2 * * ?" = seconde 0, minute 0, heure 2, tous les jours
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredSessions() {
        log.info("🧹 Démarrage du nettoyage des sessions expirées...");

        try {
            int deletedCount = sessionRepository.deleteExpiredSessions(Instant.now());

            if (deletedCount > 0) {
                log.info("✅ Sessions expirées supprimées: {}", deletedCount);
            } else {
                log.debug("✅ Aucune session expirée à supprimer");
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors du nettoyage des sessions expirées", e);
        }
    }

    /**
     * 🛡️ SÉCURITÉ : Nettoyage des sessions révoquées anciennes.
     *
     * Exécution : Tous les dimanches à 3h00 du matin
     * Objectif : Supprimer les sessions révoquées de plus de 30 jours (audit)
     *
     * CRON : "0 0 3 * * SUN" = seconde 0, minute 0, heure 3, tous les dimanches
     *
     * Note : Les sessions révoquées sont conservées 30 jours pour audit/forensics,
     * puis supprimées pour ne pas surcharger la base de données.
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    public void cleanupOldRevokedSessions() {
        log.info("🧹 Démarrage du nettoyage des sessions révoquées anciennes...");

        try {
            // Supprimer les sessions révoquées il y a plus de 30 jours
            Instant thirtyDaysAgo = Instant.now().minusSeconds(30L * 24 * 60 * 60);

            int deletedCount = sessionRepository.deleteRevokedSessionsOlderThan(thirtyDaysAgo);

            if (deletedCount > 0) {
                log.info("✅ Sessions révoquées anciennes supprimées: {}", deletedCount);
            } else {
                log.debug("✅ Aucune session révoquée ancienne à supprimer");
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors du nettoyage des sessions révoquées", e);
        }
    }
}
