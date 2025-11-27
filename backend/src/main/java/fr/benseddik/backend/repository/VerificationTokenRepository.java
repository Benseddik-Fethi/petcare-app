package fr.benseddik.backend.repository;

import fr.benseddik.backend.domain.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les tokens de vérification d'email.
 */
@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    /**
     * Recherche un token valide (non expiré).
     */
    @Query("""
        SELECT t FROM VerificationToken t 
        WHERE t.token = :token 
        AND t.expiresAt > :now
    """)
    Optional<VerificationToken> findValidByToken(String token, Instant now);

    /**
     * Recherche le token d'un utilisateur.
     */
    Optional<VerificationToken> findByUserId(UUID userId);

    /**
     * Supprime le token d'un utilisateur.
     */
    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.user.id = :userId")
    void deleteByUserId(UUID userId);

    /**
     * Supprime les tokens expirés (nettoyage).
     */
    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(Instant now);
}