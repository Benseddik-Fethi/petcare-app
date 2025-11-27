package fr.benseddik.backend.service;

import fr.benseddik.backend.dto.request.LoginRequest;
import fr.benseddik.backend.dto.request.OAuthCodeExchangeRequest;
import fr.benseddik.backend.dto.request.RefreshTokenRequest;
import fr.benseddik.backend.dto.request.RegisterRequest;
import fr.benseddik.backend.dto.response.AuthResponse;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Service d'authentification.
 *
 * Responsabilités :
 * - Inscription
 * - Connexion
 * - Rafraîchissement des tokens
 * - Déconnexion
 * - Échange de code OAuth2
 */
public interface AuthService {

    /**
     * Inscrit un nouvel utilisateur.
     *
     * @param request Données d'inscription
     * @param httpRequest Requête HTTP (pour IP/User-Agent)
     * @return Tokens d'authentification
     */
    AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest);

    /**
     * Authentifie un utilisateur.
     *
     * @param request Credentials
     * @param httpRequest Requête HTTP (pour IP/User-Agent)
     * @return Tokens d'authentification
     */
    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * Rafraîchit les tokens à partir d'un refresh token valide.
     *
     * @param request Refresh token
     * @param httpRequest Requête HTTP
     * @return Nouveaux tokens
     */
    AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest);

    /**
     * Échange un code d'autorisation OAuth2 contre les tokens.
     *
     * 🛡️ Sécurité : Le code est à usage unique et expire après 30 secondes.
     *
     * @param request Code d'autorisation
     * @return Tokens d'authentification
     */
    AuthResponse exchangeOAuthCode(OAuthCodeExchangeRequest request);

    /**
     * Déconnecte l'utilisateur (révoque le refresh token courant).
     *
     * @param refreshToken Token à révoquer
     * @param httpRequest Requête HTTP
     */
    void logout(String refreshToken, HttpServletRequest httpRequest);

    /**
     * Déconnecte l'utilisateur de toutes ses sessions.
     *
     * @param httpRequest Requête HTTP
     */
    void logoutAll(HttpServletRequest httpRequest);
}