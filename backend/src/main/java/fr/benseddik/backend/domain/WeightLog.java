package fr.benseddik.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entité WeightLog - Historique du poids d'un animal.
 */
@Entity
@Table(name = "weight_logs", indexes = {
        @Index(name = "idx_weight_pet", columnList = "pet_id"),
        @Index(name = "idx_weight_date", columnList = "date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeightLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Poids en kilogrammes
     */
    @Column(nullable = false)
    private Double weight;

    /**
     * Date de la pesée
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Note optionnelle (ex: "Après vermifuge", "Régime en cours")
     */
    @Column(length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;
}