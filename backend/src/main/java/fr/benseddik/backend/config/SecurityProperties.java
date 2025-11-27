package fr.benseddik.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Propriétés de sécurité applicative.
 * Chargées depuis application.yml sous le préfixe "app.security".
 */
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        Cors cors,
        RateLimit rateLimit,
        BruteForce bruteForce
) {
    public record Cors(
            List<String> allowedOrigins,
            List<String> allowedMethods,
            List<String> allowedHeaders,
            Boolean allowCredentials,
            Long maxAge
    ) {
        public Cors {
            if (allowedOrigins == null) {
                allowedOrigins = List.of("http://localhost:5173");
            }
            if (allowedMethods == null) {
                allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
            }
            if (allowedHeaders == null) {
                allowedHeaders = List.of("*");
            }
            if (allowCredentials == null) {
                allowCredentials = true;
            }
            if (maxAge == null) {
                maxAge = 3600L;
            }
        }
    }

    public record RateLimit(
            Boolean enabled,
            Integer requestsPerMinute,
            Integer authRequestsPerMinute
    ) {
        public RateLimit {
            if (enabled == null) {
                enabled = true;
            }
            if (requestsPerMinute == null) {
                requestsPerMinute = 60;
            }
            if (authRequestsPerMinute == null) {
                authRequestsPerMinute = 10;
            }
        }
    }

    public record BruteForce(
            Integer maxAttempts,
            Duration lockDuration
    ) {
        public BruteForce {
            if (maxAttempts == null) {
                maxAttempts = 5;
            }
            if (lockDuration == null) {
                lockDuration = Duration.ofMinutes(15);
            }
        }
    }

    public SecurityProperties {
        if (cors == null) {
            cors = new Cors(null, null, null, null, null);
        }
        if (rateLimit == null) {
            rateLimit = new RateLimit(null, null, null);
        }
        if (bruteForce == null) {
            bruteForce = new BruteForce(null, null);
        }
    }
}