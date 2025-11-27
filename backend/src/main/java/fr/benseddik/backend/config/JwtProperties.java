package fr.benseddik.backend.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Propriétés de configuration JWT.
 * Chargées depuis application.yml sous le préfixe "jwt".
 *
 * 🛡️ Validation : clé secrète obligatoire et min 256 bits.
 */
@ConfigurationProperties(prefix = "jwt")
@Validated
public record JwtProperties(
        @NotBlank(message = "JWT secret is required")
        String secret,

        AccessToken accessToken,
        RefreshToken refreshToken,

        String issuer,
        String audience
) {
    public record AccessToken(Duration expiration) {
        public AccessToken {
            if (expiration == null) {
                expiration = Duration.ofMinutes(15);
            }
        }
    }

    public record RefreshToken(Duration expiration) {
        public RefreshToken {
            if (expiration == null) {
                expiration = Duration.ofDays(7);
            }
        }
    }

    public JwtProperties {
        if (accessToken == null) {
            accessToken = new AccessToken(Duration.ofMinutes(15));
        }
        if (refreshToken == null) {
            refreshToken = new RefreshToken(Duration.ofDays(7));
        }
        if (issuer == null || issuer.isBlank()) {
            issuer = "petcare-api";
        }
        if (audience == null || audience.isBlank()) {
            audience = "petcare-app";
        }
    }
}