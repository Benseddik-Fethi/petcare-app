package fr.benseddik.backend.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO pour la création d'un animal de compagnie.
 *
 * 🛡️ SÉCURITÉ : Validation stricte de toutes les entrées utilisateur.
 */
public record CreatePetRequest(

        @NotBlank(message = "Le nom de l'animal est obligatoire")
        @Size(min = 1, max = 100, message = "Le nom doit contenir entre 1 et 100 caractères")
        String name,

        @NotBlank(message = "L'espèce est obligatoire")
        @Size(max = 50, message = "L'espèce ne peut dépasser 50 caractères")
        @Pattern(
                regexp = "^[a-zA-ZÀ-ÿ\\s-]+$",
                message = "L'espèce ne peut contenir que des lettres, espaces et tirets"
        )
        String species,

        @Size(max = 100, message = "La race ne peut dépasser 100 caractères")
        String breed,

        @PastOrPresent(message = "La date de naissance ne peut être dans le futur")
        LocalDate birthDate,

        @Pattern(
                regexp = "^(Mâle|Femelle|Indéterminé)?$",
                message = "Le genre doit être : Mâle, Femelle ou Indéterminé"
        )
        String gender,

        @Size(max = 50, message = "Le numéro de puce ne peut dépasser 50 caractères")
        @Pattern(
                regexp = "^[0-9A-Za-z]*$",
                message = "Le numéro de puce ne peut contenir que des chiffres et lettres"
        )
        String microchip,

        @Size(max = 100, message = "La couleur ne peut dépasser 100 caractères")
        String color,

        @Size(max = 500, message = "L'avatar ne peut dépasser 500 caractères")
        String avatar
) {
}
