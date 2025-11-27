package fr.benseddik.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO pour l'échange du code d'autorisation OAuth2 contre les tokens.
 */
public record OAuthCodeExchangeRequest(
        @NotBlank(message = "Le code d'autorisation est obligatoire")
        String code
) {}