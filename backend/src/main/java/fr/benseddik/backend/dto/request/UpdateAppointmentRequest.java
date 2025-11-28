package fr.benseddik.backend.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour la mise à jour d'un rendez-vous vétérinaire.
 *
 * 🛡️ SÉCURITÉ : Validation stricte des modifications.
 * Tous les champs sont optionnels (mise à jour partielle).
 */
public record UpdateAppointmentRequest(

        @Future(message = "Le rendez-vous doit être dans le futur")
        LocalDateTime date,

        @Size(min = 3, max = 500, message = "Le motif doit contenir entre 3 et 500 caractères")
        String reason,

        @Pattern(
                regexp = "^(scheduled|completed|cancelled)$",
                message = "Le statut doit être : scheduled, completed ou cancelled"
        )
        String status,

        @Size(max = 5000, message = "Les notes ne peuvent dépasser 5000 caractères")
        String notes,

        UUID vetId,

        UUID clinicId,

        @Positive(message = "La durée doit être positive")
        @Max(value = 480, message = "La durée ne peut dépasser 480 minutes (8h)")
        Integer durationMinutes
) {
}
