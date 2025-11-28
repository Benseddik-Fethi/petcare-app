package fr.benseddik.backend.repository;

import fr.benseddik.backend.domain.Vaccine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Vaccine.
 *
 * 🛡️ SÉCURITÉ : Les requêtes passent par Pet pour vérifier la propriété.
 */
@Repository
public interface VaccineRepository extends JpaRepository<Vaccine, UUID> {

    /**
     * Trouve tous les vaccins d'un animal.
     */
    List<Vaccine> findByPetIdOrderByNextDateAsc(UUID petId);

    /**
     * Trouve un vaccin par ID pour un animal spécifique (sécurité).
     */
    Optional<Vaccine> findByIdAndPetId(UUID id, UUID petId);

    /**
     * Trouve les vaccins expirés pour un animal.
     */
    @Query("SELECT v FROM Vaccine v WHERE v.pet.id = :petId AND v.nextDate < :today")
    List<Vaccine> findExpiredByPetId(@Param("petId") UUID petId, @Param("today") LocalDate today);

    /**
     * Trouve les vaccins avec rappel imminent (dans les N prochains jours).
     */
    @Query("SELECT v FROM Vaccine v WHERE v.pet.id = :petId " +
           "AND v.nextDate BETWEEN :today AND :futureDate")
    List<Vaccine> findUpcomingByPetId(@Param("petId") UUID petId,
                                      @Param("today") LocalDate today,
                                      @Param("futureDate") LocalDate futureDate);

    /**
     * Trouve les vaccins d'un utilisateur approchant de leur rappel.
     */
    @Query("SELECT v FROM Vaccine v WHERE v.pet.owner.id = :ownerId " +
           "AND v.nextDate BETWEEN :today AND :futureDate " +
           "ORDER BY v.nextDate ASC")
    List<Vaccine> findUpcomingByOwnerId(@Param("ownerId") UUID ownerId,
                                        @Param("today") LocalDate today,
                                        @Param("futureDate") LocalDate futureDate);

    /**
     * Vérifie si un vaccin appartient à un animal.
     */
    boolean existsByIdAndPetId(UUID id, UUID petId);

    /**
     * Supprime tous les vaccins d'un animal (cascade manuel si nécessaire).
     */
    void deleteByPetId(UUID petId);
}
