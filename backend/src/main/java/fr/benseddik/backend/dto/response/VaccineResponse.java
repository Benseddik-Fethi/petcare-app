package fr.benseddik.backend.dto.response;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de réponse pour un vaccin.
 *
 * 🛡️ SÉCURITÉ : Contient les informations de vaccination.
 */
public record VaccineResponse(
        UUID id,
        String name,
        LocalDate date,
        LocalDate nextDate,
        String status,
        UUID petId,
        String petName
) {
}
