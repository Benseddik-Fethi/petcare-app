package fr.benseddik.backend.repository;

import fr.benseddik.backend.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'entité AuditLog.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Recherche les logs d'un utilisateur.
     */
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Recherche les logs par action.
     */
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);

    /**
     * Recherche les logs entre deux dates.
     */
    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant start, Instant end);

    /**
     * Recherche les logs d'un utilisateur par action.
     */
    List<AuditLog> findByUserIdAndActionOrderByCreatedAtDesc(UUID userId, String action);

    /**
     * Compte les tentatives de connexion échouées récentes pour une IP.
     */
    long countByIpAddressAndActionAndCreatedAtAfter(String ipAddress, String action, Instant after);
}