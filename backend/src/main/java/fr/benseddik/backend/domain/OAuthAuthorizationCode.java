package fr.benseddik.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Code d'autorisation temporaire pour OAuth2.
 *
 * 🛡️ Sécurité :
 * - Expiration très courte (30 secondes)
 * - Usage unique (supprimé après échange)
 * - Stocke les tokens de manière sécurisée côté serveur
 */
@Entity
@Table(name = "oauth_authorization_codes", indexes = {
        @Index(name = "idx_oauth_code", columnList = "code", unique = true),
        @Index(name = "idx_oauth_expires", columnList = "expiresAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthAuthorizationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Code unique envoyé dans l'URL de callback.
     */
    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Access token pré-généré (stocké côté serveur).
     */
    @Column(name = "access_token", nullable = false, length = 1000)
    private String accessToken;

    /**
     * Refresh token pré-généré (stocké côté serveur).
     */
    @Column(name = "refresh_token", nullable = false, length = 1000)
    private String refreshToken;

    /**
     * Expiration du code (30 secondes).
     */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /**
     * Indique si le code a été utilisé.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean used = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Vérifie si le code est valide (non expiré et non utilisé).
     */
    public boolean isValid() {
        return !used && Instant.now().isBefore(expiresAt);
    }

    /**
     * Marque le code comme utilisé.
     */
    public void markAsUsed() {
        this.used = true;
    }

    /**
     * Crée un nouveau code d'autorisation avec expiration dans 30 secondes.
     */
    public static OAuthAuthorizationCode create(User user, String accessToken, String refreshToken) {
        return OAuthAuthorizationCode.builder()
                .code(UUID.randomUUID().toString().replace("-", ""))
                .user(user)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresAt(Instant.now().plusSeconds(30))
                .used(false)
                .build();
    }
}