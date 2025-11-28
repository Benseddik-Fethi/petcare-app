package fr.benseddik.backend.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de réponse pour un animal de compagnie.
 *
 * 🛡️ SÉCURITÉ : Ne contient que les données publiques de l'animal.
 */
public record PetResponse(
        UUID id,
        String name,
        String species,
        String breed,
        LocalDate birthDate,
        Integer ageInYears,
        String gender,
        String microchip,
        String color,
        String avatar,
        Double currentWeight,
        Instant createdAt,
        Instant updatedAt
) {
}
