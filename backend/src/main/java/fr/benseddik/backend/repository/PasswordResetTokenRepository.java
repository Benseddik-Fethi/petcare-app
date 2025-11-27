package fr.benseddik.backend.repository;

import fr.benseddik.backend.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les tokens de réinitialisation de mot de passe.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    /**
     * Recherche un token valide (non expiré et non utilisé).
     */
    @Query("""
        SELECT t FROM PasswordResetToken t 
        WHERE t.token = :token 
        AND t.used = false 
        AND t.expiresAt > :now
    """)
    Optional<PasswordResetToken> findValidByToken(String token, Instant now);

    /**
     * Recherche tous les tokens d'un utilisateur.
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.user.id = :userId ORDER BY t.createdAt DESC")
    Optional<PasswordResetToken> findLatestByUserId(UUID userId);

    /**
     * Invalide tous les tokens d'un utilisateur (quand un nouveau mot de passe est défini).
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true WHERE t.user.id = :userId AND t.used = false")
    int invalidateAllUserTokens(UUID userId);

    /**
     * Supprime les tokens expirés (nettoyage).
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(Instant now);

    /**
     * Supprime les tokens utilisés (nettoyage).
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.used = true")
    int deleteUsedTokens();
}