package fr.benseddik.backend.repository;

import fr.benseddik.backend.domain.Vet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'entité Vet.
 *
 * Les vétérinaires sont des données de référence accessibles par tous.
 */
@Repository
public interface VetRepository extends JpaRepository<Vet, UUID> {

    /**
     * Trouve les vétérinaires d'une clinique.
     */
    List<Vet> findByClinicId(UUID clinicId);

    /**
     * Cherche des vétérinaires par nom.
     */
    @Query("SELECT v FROM Vet v WHERE LOWER(v.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Vet> searchByName(@Param("name") String name);

    /**
     * Cherche des vétérinaires par spécialité.
     */
    @Query("SELECT v FROM Vet v WHERE LOWER(v.specialty) LIKE LOWER(CONCAT('%', :specialty, '%'))")
    List<Vet> findBySpecialty(@Param("specialty") String specialty);

    /**
     * Trouve tous les vétérinaires avec leur clinique.
     */
    @Query("SELECT v FROM Vet v JOIN FETCH v.clinic")
    List<Vet> findAllWithClinic();
}
