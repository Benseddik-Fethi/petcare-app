package fr.benseddik.backend.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO pour l'enregistrement d'une pesée.
 *
 * 🛡️ SÉCURITÉ : Validation stricte des données de poids.
 */
public record CreateWeightLogRequest(

        @NotNull(message = "L'ID de l'animal est obligatoire")
        UUID petId,

        @NotNull(message = "Le poids est obligatoire")
        @Positive(message = "Le poids doit être positif")
        @DecimalMax(value = "500.0", message = "Le poids ne peut dépasser 500 kg")
        Double weight,

        @NotNull(message = "La date de pesée est obligatoire")
        @PastOrPresent(message = "La date de pesée ne peut être dans le futur")
        LocalDate date,

        @Size(max = 500, message = "La note ne peut dépasser 500 caractères")
        String note
) {
}
