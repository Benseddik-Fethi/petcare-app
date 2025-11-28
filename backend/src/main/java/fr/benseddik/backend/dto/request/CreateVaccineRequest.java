package fr.benseddik.backend.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO pour l'enregistrement d'un vaccin.
 *
 * 🛡️ SÉCURITÉ : Validation stricte des données de vaccination.
 */
public record CreateVaccineRequest(

        @NotNull(message = "L'ID de l'animal est obligatoire")
        UUID petId,

        @NotBlank(message = "Le nom du vaccin est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom du vaccin doit contenir entre 2 et 100 caractères")
        String name,

        @NotNull(message = "La date d'administration est obligatoire")
        @PastOrPresent(message = "La date d'administration ne peut être dans le futur")
        LocalDate date,

        @Future(message = "La date de rappel doit être dans le futur")
        LocalDate nextDate
) {
}
