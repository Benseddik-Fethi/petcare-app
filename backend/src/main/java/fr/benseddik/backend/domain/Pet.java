package fr.benseddik.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité Pet - Animal de compagnie.
 */
@Entity
@Table(name = "pets", indexes = {
        @Index(name = "idx_pet_owner", columnList = "owner_id"),
        @Index(name = "idx_pet_microchip", columnList = "microchip")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Espèce (Chien, Chat, NAC, etc.)
     */
    @Column(nullable = false, length = 50)
    private String species;

    /**
     * Race (ex: Labrador, Siamois...)
     */
    @Column(length = 100)
    private String breed;

    /**
     * Date de naissance (pour calculer l'âge)
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * Genre (Mâle, Femelle)
     */
    @Column(length = 20)
    private String gender;

    /**
     * Numéro de puce électronique
     */
    @Column(length = 50)
    private String microchip;

    /**
     * Couleur / Robe
     */
    @Column(length = 100)
    private String color;

    /**
     * Avatar (emoji ou URL)
     */
    @Column(length = 500)
    private String avatar;

    // ═══════════════════════════════════════════════════════════════════════════
    // RELATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("date DESC")
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("nextDate ASC")
    private List<Vaccine> vaccines = new ArrayList<>();

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("date ASC")
    private List<WeightLog> weights = new ArrayList<>();

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("date DESC")
    private List<MedicalRecord> medicalRecords = new ArrayList<>();

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

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
     * Calcule l'âge de l'animal en années.
     */
    public Integer getAgeInYears() {
        if (birthDate == null) return null;
        return java.time.Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Retourne le dernier poids enregistré.
     */
    public Double getCurrentWeight() {
        if (weights == null || weights.isEmpty()) return null;
        return weights.get(weights.size() - 1).getWeight();
    }
}