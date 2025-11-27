package fr.benseddik.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entité Account - Comptes OAuth2 liés à un utilisateur.
 *
 * Permet à un utilisateur d'avoir plusieurs méthodes de connexion
 * (ex: email + Google + Facebook).
 */
@Entity
@Table(name = "accounts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_account_provider_id",
                columnNames = {"provider", "provider_account_id"}
        ),
        indexes = {
                @Index(name = "idx_account_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    /**
     * ID unique chez le fournisseur OAuth (ex: Google sub, Facebook id).
     */
    @Column(name = "provider_account_id", nullable = false, length = 255)
    private String providerAccountId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}