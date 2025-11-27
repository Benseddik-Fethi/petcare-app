package fr.benseddik.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entité Vaccine - Vaccination d'un animal.
 */
@Entity
@Table(name = "vaccines", indexes = {
        @Index(name = "idx_vaccine_pet", columnList = "pet_id"),
        @Index(name = "idx_vaccine_next_date", columnList = "next_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vaccine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Nom du vaccin (ex: Rage, Typhus, Leucose...)
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Date d'administration du vaccin
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Date de rappel (optionnelle)
     */
    @Column(name = "next_date")
    private LocalDate nextDate;

    /**
     * Statut du vaccin : valid, expired, upcoming
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "valid";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Met à jour le statut en fonction de la date de rappel.
     */
    public void updateStatus() {
        if (nextDate == null) {
            this.status = "valid";
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate warningDate = nextDate.minusDays(30); // 30 jours avant expiration

        if (today.isAfter(nextDate)) {
            this.status = "expired";
        } else if (today.isAfter(warningDate)) {
            this.status = "upcoming";
        } else {
            this.status = "valid";
        }
    }

    /**
     * Vérifie si le vaccin est expiré.
     */
    public boolean isExpired() {
        return nextDate != null && LocalDate.now().isAfter(nextDate);
    }

    /**
     * Vérifie si le rappel approche (dans les 30 prochains jours).
     */
    public boolean isUpcoming() {
        if (nextDate == null) return false;
        LocalDate today = LocalDate.now();
        return today.isBefore(nextDate) && today.isAfter(nextDate.minusDays(30));
    }
}