package fr.benseddik.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de réponse pour une clinique vétérinaire.
 *
 * 🔓 DONNÉES PUBLIQUES : Les cliniques sont visibles par tous.
 */
public record ClinicResponse(
        UUID id,
        String name,
        String address,
        String phone,
        String email,
        String website,
        String openingHours,
        Instant createdAt,
        Instant updatedAt
) {
}
