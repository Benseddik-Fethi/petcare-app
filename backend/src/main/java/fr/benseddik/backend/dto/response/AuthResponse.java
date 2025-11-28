package fr.benseddik.backend.dto.response;

/**
 * DTO de réponse d'authentification.
 *
 * ✅ Utilise camelCase (accessToken, refreshToken) pour compatibilité avec le frontend React.
 * Le frontend attend exactement ce format :
 * {
 *   "accessToken": "...",
 *   "refreshToken": "...",  // Optionnel (aussi envoyé en cookie HTTP-only)
 *   "tokenType": "Bearer",
 *   "expiresIn": 300,
 *   "user": { ... }
 * }
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserResponse user
) {
    /**
     * Constructeur simplifié avec tokenType "Bearer" par défaut.
     */
    public AuthResponse(String accessToken, String refreshToken, Long expiresIn, UserResponse user) {
        this(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}