package fr.benseddik.backend.controller;

import fr.benseddik.backend.dto.request.LoginRequest;
import fr.benseddik.backend.dto.request.OAuthCodeExchangeRequest;
import fr.benseddik.backend.dto.request.RefreshTokenRequest;
import fr.benseddik.backend.dto.request.RegisterRequest;
import fr.benseddik.backend.dto.response.AuthResponse;
import fr.benseddik.backend.dto.response.UserResponse;
import fr.benseddik.backend.security.CookieUtils;
import fr.benseddik.backend.security.CustomUserDetails;
import fr.benseddik.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur REST pour l'authentification.
 *
 * 🛡️ Sécurité : Les tokens sont stockés en cookies HTTP-only.
 *
 * Endpoints :
 * - POST /api/v1/auth/register - Inscription
 * - POST /api/v1/auth/login - Connexion
 * - POST /api/v1/auth/refresh - Rafraîchir les tokens
 * - POST /api/v1/auth/logout - Déconnexion
 * - POST /api/v1/auth/logout-all - Déconnexion de toutes les sessions
 * - GET  /api/v1/auth/me - Infos utilisateur courant
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    /**
     * Inscrit un nouvel utilisateur.
     * Les tokens sont stockés en cookies HTTP-only.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthResponse response = authService.register(request, httpRequest);

        // Stocker les tokens en cookies HTTP-only
        cookieUtils.addAccessTokenCookie(httpResponse, response.accessToken());
        cookieUtils.addRefreshTokenCookie(httpResponse, response.refreshToken());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authentifie un utilisateur.
     * Les tokens sont stockés en cookies HTTP-only.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthResponse response = authService.login(request, httpRequest);

        // Stocker les tokens en cookies HTTP-only
        cookieUtils.addAccessTokenCookie(httpResponse, response.accessToken());
        cookieUtils.addRefreshTokenCookie(httpResponse, response.refreshToken());

        return ResponseEntity.ok(response);
    }

    /**
     * Rafraîchit les tokens.
     * Lit le refresh token depuis le cookie ou le body.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        // Récupérer le refresh token depuis le cookie ou le body
        String refreshToken = cookieUtils.getRefreshTokenFromCookie(httpRequest)
                .orElseGet(() -> request != null ? request.refreshToken() : null);

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken);
        AuthResponse response = authService.refreshToken(tokenRequest, httpRequest);

        // Mettre à jour les cookies
        cookieUtils.addAccessTokenCookie(httpResponse, response.accessToken());
        cookieUtils.addRefreshTokenCookie(httpResponse, response.refreshToken());

        return ResponseEntity.ok(response);
    }

    /**
     * Échange un code d'autorisation OAuth2 contre les tokens.
     * (Conservé pour compatibilité, mais les cookies sont préférés)
     */
    @PostMapping("/oauth/exchange")
    public ResponseEntity<AuthResponse> exchangeOAuthCode(
            @Valid @RequestBody OAuthCodeExchangeRequest request,
            HttpServletResponse httpResponse
    ) {
        AuthResponse response = authService.exchangeOAuthCode(request);

        // Stocker les tokens en cookies HTTP-only
        cookieUtils.addAccessTokenCookie(httpResponse, response.accessToken());
        cookieUtils.addRefreshTokenCookie(httpResponse, response.refreshToken());

        return ResponseEntity.ok(response);
    }

    /**
     * Déconnecte l'utilisateur (révoque le refresh token et supprime les cookies).
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        // Récupérer le refresh token depuis le cookie ou le body
        String refreshToken = cookieUtils.getRefreshTokenFromCookie(httpRequest)
                .orElseGet(() -> request != null ? request.refreshToken() : null);

        authService.logout(refreshToken, httpRequest);

        // Supprimer les cookies
        cookieUtils.clearAuthCookies(httpResponse);

        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }

    /**
     * Déconnecte l'utilisateur de toutes ses sessions.
     */
    @PostMapping("/logout-all")
    public ResponseEntity<Map<String, String>> logoutAll(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        authService.logoutAll(httpRequest);

        // Supprimer les cookies
        cookieUtils.clearAuthCookies(httpResponse);

        return ResponseEntity.ok(Map.of("message", "Toutes les sessions ont été révoquées"));
    }

    /**
     * Retourne les informations de l'utilisateur authentifié.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(UserResponse.fromEntity(userDetails.getUser()));
    }
}