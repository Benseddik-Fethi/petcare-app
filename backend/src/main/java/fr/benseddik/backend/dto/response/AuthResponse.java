package fr.benseddik.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de réponse d'authentification.
 */
public record AuthResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        Long expiresIn,

        UserResponse user
) {
    /**
     * Constructeur simplifié avec token_type "Bearer" par défaut.
     */
    public AuthResponse(String accessToken, String refreshToken, Long expiresIn, UserResponse user) {
        this(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}