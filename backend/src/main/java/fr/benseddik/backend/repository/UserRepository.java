package fr.benseddik.backend.repository;

import fr.benseddik.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité User.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Recherche un utilisateur par email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si un email existe déjà.
     */
    boolean existsByEmail(String email);

    /**
     * Recherche un utilisateur par ID Google.
     */
    Optional<User> findByGoogleId(String googleId);

    /**
     * Recherche un utilisateur par ID Facebook.
     */
    Optional<User> findByFacebookId(String facebookId);

    /**
     * Réinitialise les tentatives de connexion échouées.
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = null WHERE u.id = :userId")
    void resetFailedLoginAttempts(UUID userId);

    /**
     * Incrémente les tentatives de connexion échouées.
     */
    @Modifying
    @Query("""
        UPDATE User u 
        SET u.failedLoginAttempts = u.failedLoginAttempts + 1, 
            u.lastFailedLogin = :now 
        WHERE u.id = :userId
    """)
    void incrementFailedLoginAttempts(UUID userId, Instant now);

    /**
     * Verrouille un compte jusqu'à une date donnée.
     */
    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = :lockedUntil WHERE u.id = :userId")
    void lockAccount(UUID userId, Instant lockedUntil);
}