package fr.benseddik.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration pour activer les opérations asynchrones.
 * Utilisé notamment pour l'envoi d'emails en arrière-plan.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}