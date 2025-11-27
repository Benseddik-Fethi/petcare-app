package fr.benseddik.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO pour le rafraîchissement des tokens.
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Le refresh token est obligatoire")
        String refreshToken
) {}