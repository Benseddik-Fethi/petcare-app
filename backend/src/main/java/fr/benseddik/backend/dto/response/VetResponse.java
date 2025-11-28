package fr.benseddik.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de réponse pour un vétérinaire.
 *
 * 🔓 DONNÉES PUBLIQUES : Les vétérinaires sont visibles par tous.
 */
public record VetResponse(
        UUID id,
        String firstName,
        String lastName,
        String fullName,
        String specialty,
        String phone,
        String email,
        String avatar,
        UUID clinicId,
        String clinicName,
        Instant createdAt,
        Instant updatedAt
) {
}
