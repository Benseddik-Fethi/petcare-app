package fr.benseddik.backend.repository;

import fr.benseddik.backend.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Appointment.
 *
 * 🛡️ SÉCURITÉ : Les requêtes filtrent par user_id pour éviter les accès non autorisés.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    /**
     * Trouve tous les rendez-vous d'un utilisateur.
     */
    List<Appointment> findByUserIdOrderByDateDesc(UUID userId);

    /**
     * Trouve tous les rendez-vous à venir d'un utilisateur.
     */
    @Query("SELECT a FROM Appointment a WHERE a.user.id = :userId " +
           "AND a.date > :now AND a.status = 'scheduled' " +
           "ORDER BY a.date ASC")
    List<Appointment> findUpcomingByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    /**
     * Trouve les N prochains rendez-vous.
     */
    @Query("SELECT a FROM Appointment a WHERE a.user.id = :userId " +
           "AND a.date > :now AND a.status = 'scheduled' " +
           "ORDER BY a.date ASC LIMIT :limit")
    List<Appointment> findNextAppointments(@Param("userId") UUID userId,
                                           @Param("now") LocalDateTime now,
                                           @Param("limit") int limit);

    /**
     * Trouve un rendez-vous par ID et user (sécurité).
     */
    Optional<Appointment> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Trouve tous les rendez-vous d'un animal.
     */
    List<Appointment> findByPetIdOrderByDateDesc(UUID petId);

    /**
     * Trouve les rendez-vous dans une période donnée pour un utilisateur.
     */
    @Query("SELECT a FROM Appointment a WHERE a.user.id = :userId " +
           "AND a.date BETWEEN :startDate AND :endDate " +
           "ORDER BY a.date ASC")
    List<Appointment> findByUserIdAndDateBetween(@Param("userId") UUID userId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    /**
     * Compte le nombre de rendez-vous à venir pour un utilisateur.
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.user.id = :userId " +
           "AND a.date > :now AND a.status = 'scheduled'")
    long countUpcomingByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    /**
     * Vérifie si un rendez-vous appartient à un utilisateur.
     */
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
