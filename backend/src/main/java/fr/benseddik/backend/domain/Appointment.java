package fr.benseddik.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité Appointment - Rendez-vous vétérinaire.
 */
@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appointment_user", columnList = "user_id"),
        @Index(name = "idx_appointment_pet", columnList = "pet_id"),
        @Index(name = "idx_appointment_date", columnList = "date"),
        @Index(name = "idx_appointment_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Date et heure du rendez-vous
     */
    @Column(nullable = false)
    private LocalDateTime date;

    /**
     * Motif du rendez-vous
     */
    @Column(nullable = false, length = 500)
    private String reason;

    /**
     * Statut : scheduled, completed, cancelled
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "scheduled";

    /**
     * Notes du vétérinaire après consultation
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Durée estimée en minutes
     */
    @Column(name = "duration_minutes")
    @Builder.Default
    private Integer durationMinutes = 30;

    // ═══════════════════════════════════════════════════════════════════════════
    // RELATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id")
    private Vet vet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;

    // ═══════════════════════════════════════════════════════════════════════════
    // TIMESTAMPS
    // ═══════════════════════════════════════════════════════════════════════════

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Vérifie si le rendez-vous est dans le futur.
     */
    public boolean isUpcoming() {
        return "scheduled".equals(status) && date.isAfter(LocalDateTime.now());
    }

    /**
     * Vérifie si le rendez-vous est passé mais non complété.
     */
    public boolean isPastDue() {
        return "scheduled".equals(status) && date.isBefore(LocalDateTime.now());
    }

    /**
     * Annule le rendez-vous.
     */
    public void cancel() {
        this.status = "cancelled";
    }

    /**
     * Marque le rendez-vous comme complété.
     */
    public void complete(String notes) {
        this.status = "completed";
        this.notes = notes;
    }
}