package fr.benseddik.backend.repository;

import fr.benseddik.backend.domain.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'entité Clinic.
 *
 * Les cliniques sont des données de référence accessibles par tous.
 */
@Repository
public interface ClinicRepository extends JpaRepository<Clinic, UUID> {

    /**
     * Trouve les cliniques par nom (partial match, case insensitive).
     */
    @Query("SELECT c FROM Clinic c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Clinic> searchByName(@Param("name") String name);

    /**
     * Trouve les cliniques par ville.
     */
    @Query("SELECT c FROM Clinic c WHERE LOWER(c.address) LIKE LOWER(CONCAT('%', :city, '%'))")
    List<Clinic> findByCity(@Param("city") String city);

    /**
     * Trouve toutes les cliniques avec leurs vétérinaires.
     */
    @Query("SELECT DISTINCT c FROM Clinic c LEFT JOIN FETCH c.vets")
    List<Clinic> findAllWithVets();

    /**
     * Vérifie si une clinique existe par nom.
     */
    boolean existsByName(String name);
}
