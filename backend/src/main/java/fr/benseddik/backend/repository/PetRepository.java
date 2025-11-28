package fr.benseddik.backend.repository;

import fr.benseddik.backend.domain.Pet;
import fr.benseddik.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Pet.
 *
 * 🛡️ SÉCURITÉ : Les requêtes filtrent par propriétaire pour éviter les accès non autorisés.
 */
@Repository
public interface PetRepository extends JpaRepository<Pet, UUID> {

    /**
     * Trouve tous les animaux d'un propriétaire.
     */
    List<Pet> findByOwner(User owner);

    /**
     * Trouve tous les animaux d'un propriétaire (par ID).
     */
    List<Pet> findByOwnerId(UUID ownerId);

    /**
     * Trouve un animal par ID et propriétaire (sécurité).
     */
    Optional<Pet> findByIdAndOwner(UUID id, User owner);

    /**
     * Trouve un animal par ID et propriétaire ID (sécurité).
     */
    Optional<Pet> findByIdAndOwnerId(UUID id, UUID ownerId);

    /**
     * Cherche des animaux par nom (partial match, case insensitive).
     */
    @Query("SELECT p FROM Pet p WHERE p.owner = :owner AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Pet> searchByNameAndOwner(@Param("name") String name, @Param("owner") User owner);

    /**
     * Cherche des animaux par nom ou race.
     */
    @Query("SELECT p FROM Pet p WHERE p.owner.id = :ownerId " +
           "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.breed) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Pet> searchByNameOrBreed(@Param("search") String search, @Param("ownerId") UUID ownerId);

    /**
     * Compte le nombre d'animaux d'un propriétaire.
     */
    long countByOwnerId(UUID ownerId);

    /**
     * Vérifie si un animal appartient à un utilisateur.
     */
    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);
}
