package fr.benseddik.backend.repository;

import fr.benseddik.backend.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Session (refresh tokens).
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    /**
     * Recherche une session active par hash du refresh token.
     */
    @Query("""
        SELECT s FROM Session s 
        WHERE s.refreshTokenHash = :tokenHash 
        AND s.revokedAt IS NULL 
        AND s.expiresAt > :now
    """)
    Optional<Session> findValidByRefreshTokenHash(String tokenHash, Instant now);

    /**
     * Recherche toutes les sessions actives d'un utilisateur.
     */
    @Query("""
        SELECT s FROM Session s 
        WHERE s.user.id = :userId 
        AND s.revokedAt IS NULL 
        AND s.expiresAt > :now 
        ORDER BY s.createdAt DESC
    """)
    List<Session> findActiveSessionsByUserId(UUID userId, Instant now);

    /**
     * Révoque toutes les sessions d'un utilisateur.
     */
    @Modifying
    @Query("UPDATE Session s SET s.revokedAt = :now WHERE s.user.id = :userId AND s.revokedAt IS NULL")
    int revokeAllUserSessions(UUID userId, Instant now);

    /**
     * Révoque une session spécifique.
     */
    @Modifying
    @Query("UPDATE Session s SET s.revokedAt = :now WHERE s.id = :sessionId AND s.revokedAt IS NULL")
    int revokeSession(UUID sessionId, Instant now);

    /**
     * Supprime les sessions expirées (nettoyage périodique).
     */
    @Modifying
    @Query("DELETE FROM Session s WHERE s.expiresAt < :now")
    int deleteExpiredSessions(Instant now);

    /**
     * Supprime les sessions révoquées anciennes (nettoyage audit).
     * Les sessions révoquées sont conservées 30 jours pour audit/forensics.
     */
    @Modifying
    @Query("DELETE FROM Session s WHERE s.revokedAt IS NOT NULL AND s.revokedAt < :before")
    int deleteRevokedSessionsOlderThan(Instant before);

    /**
     * Compte les sessions actives d'un utilisateur.
     */
    @Query("""
        SELECT COUNT(s) FROM Session s 
        WHERE s.user.id = :userId 
        AND s.revokedAt IS NULL 
        AND s.expiresAt > :now
    """)
    long countActiveSessionsByUserId(UUID userId, Instant now);
}