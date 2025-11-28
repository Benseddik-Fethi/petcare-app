package fr.benseddik.backend.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour la création d'un rendez-vous vétérinaire.
 *
 * 🛡️ SÉCURITÉ : Validation stricte des données de rendez-vous.
 */
public record CreateAppointmentRequest(

        @NotNull(message = "L'ID de l'animal est obligatoire")
        UUID petId,

        @NotNull(message = "La date du rendez-vous est obligatoire")
        @Future(message = "Le rendez-vous doit être dans le futur")
        LocalDateTime date,

        @NotBlank(message = "Le motif du rendez-vous est obligatoire")
        @Size(min = 3, max = 500, message = "Le motif doit contenir entre 3 et 500 caractères")
        String reason,

        UUID vetId,

        UUID clinicId,

        @Positive(message = "La durée doit être positive")
        @Max(value = 480, message = "La durée ne peut dépasser 480 minutes (8h)")
        Integer durationMinutes
) {
}
