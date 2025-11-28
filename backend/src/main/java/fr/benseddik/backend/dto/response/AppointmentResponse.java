package fr.benseddik.backend.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour un rendez-vous vétérinaire.
 *
 * 🛡️ SÉCURITÉ : Inclut uniquement les données autorisées pour l'utilisateur.
 */
public record AppointmentResponse(
        UUID id,
        LocalDateTime date,
        String reason,
        String status,
        String notes,
        Integer durationMinutes,
        UUID petId,
        String petName,
        VetResponse vet,
        ClinicResponse clinic,
        Instant createdAt,
        Instant updatedAt
) {
}
