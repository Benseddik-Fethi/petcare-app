package fr.benseddik.backend.dto.response;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de réponse pour une pesée.
 *
 * 🛡️ SÉCURITÉ : Contient les données de pesée.
 */
public record WeightLogResponse(
        UUID id,
        Double weight,
        LocalDate date,
        String note,
        UUID petId,
        String petName
) {
}
