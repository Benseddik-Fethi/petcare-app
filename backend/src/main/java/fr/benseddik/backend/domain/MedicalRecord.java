package fr.benseddik.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Entité MedicalRecord - Historique médical d'un animal.
 *
 * Stocke les événements médicaux : consultations, traitements,
 * chirurgies, analyses, etc.
 */
@Entity
@Table(name = "medical_records", indexes = {
        @Index(name = "idx_medical_pet", columnList = "pet_id"),
        @Index(name = "idx_medical_date", columnList = "date"),
        @Index(name = "idx_medical_type", columnList = "recordType")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Type d'enregistrement médical.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false, length = 50)
    private RecordType recordType;

    /**
     * Titre / Résumé de l'événement.
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Description détaillée.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Date de l'événement médical.
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Diagnostic posé (si applicable).
     */
    @Column(length = 500)
    private String diagnosis;

    /**
     * Traitement prescrit (si applicable).
     */
    @Column(columnDefinition = "TEXT")
    private String treatment;

    /**
     * Médicaments prescrits (si applicable).
     */
    @Column(length = 1000)
    private String medications;

    /**
     * Résultats d'analyses (JSON flexible).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> labResults;

    /**
     * Fichiers joints (URLs des documents/images).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> attachments;

    /**
     * Coût de l'intervention (optionnel).
     * BigDecimal pour la précision monétaire.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    /**
     * Notes additionnelles.
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    // ═══════════════════════════════════════════════════════════════════════════
    // RELATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    /**
     * Vétérinaire ayant effectué l'acte (optionnel).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id")
    private Vet vet;

    /**
     * Rendez-vous associé (optionnel).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    // ═══════════════════════════════════════════════════════════════════════════
    // TIMESTAMPS
    // ═══════════════════════════════════════════════════════════════════════════

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Types d'enregistrements médicaux possibles.
     */
    public enum RecordType {
        CONSULTATION,      // Consultation générale
        VACCINATION,       // Vaccination
        SURGERY,           // Chirurgie
        LAB_TEST,          // Analyses de laboratoire
        IMAGING,           // Imagerie (radio, écho, etc.)
        DENTAL,            // Soins dentaires
        GROOMING,          // Toilettage médical
        EMERGENCY,         // Urgence
        PRESCRIPTION,      // Prescription médicamenteuse
        FOLLOW_UP,         // Suivi
        HOSPITALIZATION,   // Hospitalisation
        OTHER              // Autre
    }
}




