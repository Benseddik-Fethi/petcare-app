package fr.benseddik.backend.repository;

import fr.benseddik.backend.domain.WeightLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité WeightLog.
 *
 * 🛡️ SÉCURITÉ : Les requêtes passent par Pet pour vérifier la propriété.
 */
@Repository
public interface WeightLogRepository extends JpaRepository<WeightLog, UUID> {

    /**
     * Trouve tous les poids d'un animal, triés par date (plus récent en premier).
     */
    List<WeightLog> findByPetIdOrderByDateDesc(UUID petId);

    /**
     * Trouve tous les poids d'un animal, triés par date (plus ancien en premier).
     */
    List<WeightLog> findByPetIdOrderByDateAsc(UUID petId);

    /**
     * Trouve un poids par ID pour un animal spécifique (sécurité).
     */
    Optional<WeightLog> findByIdAndPetId(UUID id, UUID petId);

    /**
     * Trouve le dernier poids enregistré pour un animal.
     */
    @Query("SELECT w FROM WeightLog w WHERE w.pet.id = :petId ORDER BY w.date DESC LIMIT 1")
    Optional<WeightLog> findLatestByPetId(@Param("petId") UUID petId);

    /**
     * Trouve les poids dans une période donnée.
     */
    @Query("SELECT w FROM WeightLog w WHERE w.pet.id = :petId " +
           "AND w.date BETWEEN :startDate AND :endDate " +
           "ORDER BY w.date ASC")
    List<WeightLog> findByPetIdAndDateBetween(@Param("petId") UUID petId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * Vérifie si un poids appartient à un animal.
     */
    boolean existsByIdAndPetId(UUID id, UUID petId);

    /**
     * Supprime tous les poids d'un animal.
     */
    void deleteByPetId(UUID petId);
}
