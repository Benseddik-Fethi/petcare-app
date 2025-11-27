package fr.benseddik.backend.repository;

import fr.benseddik.backend.domain.OAuthAuthorizationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les codes d'autorisation OAuth2.
 */
@Repository
public interface OAuthAuthorizationCodeRepository extends JpaRepository<OAuthAuthorizationCode, UUID> {

    /**
     * Recherche un code valide (non expiré et non utilisé).
     */
    @Query("""
        SELECT c FROM OAuthAuthorizationCode c 
        WHERE c.code = :code 
        AND c.used = false 
        AND c.expiresAt > :now
    """)
    Optional<OAuthAuthorizationCode> findValidByCode(String code, Instant now);

    /**
     * Supprime les codes expirés (nettoyage périodique).
     */
    @Modifying
    @Query("DELETE FROM OAuthAuthorizationCode c WHERE c.expiresAt < :now")
    int deleteExpiredCodes(Instant now);

    /**
     * Supprime les codes utilisés (nettoyage).
     */
    @Modifying
    @Query("DELETE FROM OAuthAuthorizationCode c WHERE c.used = true")
    int deleteUsedCodes();
}