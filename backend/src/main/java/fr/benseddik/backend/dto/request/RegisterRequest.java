package fr.benseddik.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO pour l'inscription d'un utilisateur.
 */
public record RegisterRequest(
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 12, max = 128, message = "Le mot de passe doit contenir entre 12 et 128 caractères")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=\\[\\]{};':\"\\\\|,.<>/`~])[A-Za-z\\d@$!%*?&#^()_+\\-=\\[\\]{};':\"\\\\|,.<>/`~]{12,}$",
            message = "Le mot de passe doit contenir au minimum : " +
                      "1 minuscule, 1 majuscule, 1 chiffre et 1 caractère spécial (@$!%*?&#^()_+-=[]{}etc.)"
        )
        String password,

        @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
        String firstName,

        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String lastName
) {}