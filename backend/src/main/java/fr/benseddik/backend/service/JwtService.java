package fr.benseddik.backend.service;

import fr.benseddik.backend.domain.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Service de gestion des tokens JWT.
 *
 * Responsabilités :
 * - Génération des access tokens et refresh tokens
 * - Validation et parsing des tokens
 * - Extraction des claims
 */
public interface JwtService {

    /**
     * Génère un access token pour un utilisateur.
     * Durée de vie courte (15 min par défaut).
     *
     * @param user Utilisateur authentifié
     * @return Access token JWT
     */
    String generateAccessToken(User user);

    /**
     * Génère un refresh token pour un utilisateur.
     * Durée de vie longue (7 jours par défaut).
     *
     * @param user Utilisateur authentifié
     * @return Refresh token JWT
     */
    String generateRefreshToken(User user);

    /**
     * Valide un token JWT.
     *
     * @param token Token à valider
     * @return true si le token est valide, false sinon
     */
    boolean isTokenValid(String token);

    /**
     * Extrait l'ID utilisateur du token.
     *
     * @param token Token JWT
     * @return UUID de l'utilisateur ou empty si invalide
     */
    Optional<UUID> extractUserId(String token);

    /**
     * Extrait l'email du token.
     *
     * @param token Token JWT
     * @return Email de l'utilisateur ou empty si invalide
     */
    Optional<String> extractEmail(String token);

    /**
     * Vérifie si le token est un access token.
     *
     * @param token Token JWT
     * @return true si access token, false si refresh token
     */
    boolean isAccessToken(String token);

    /**
     * Hash un refresh token pour stockage sécurisé.
     * Utilise SHA-256.
     *
     * @param refreshToken Token brut
     * @return Hash du token
     */
    String hashToken(String refreshToken);
}