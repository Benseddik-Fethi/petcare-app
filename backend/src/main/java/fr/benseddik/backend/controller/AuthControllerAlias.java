package fr.benseddik.backend.controller;

import fr.benseddik.backend.dto.request.LoginRequest;
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

/**
 * ✅ Contrôleur ALIAS pour rétrocompatibilité avec le frontend React.
 *
 * Le frontend original attend les endpoints sur /api/auth/*
 * alors que Spring Boot utilise /api/v1/auth/*
 *
 * Ce contrôleur duplique les endpoints principaux sans /v1/
 * pour permettre au frontend de fonctionner sans modification.
 *
 * ⚠️ À long terme : Mettre à jour le frontend pour utiliser /api/v1/auth/*
 *
 * Endpoints compatibles frontend :
 * - POST /api/auth/register
 * - POST /api/auth/login
 * - POST /api/auth/refresh
 * - POST /api/auth/logout
 * - GET  /api/auth/me
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthControllerAlias {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    /**
     * Inscription (alias sans /v1/).
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthResponse response = authService.register(request, httpRequest);
        cookieUtils.setAuthCookies(httpResponse, response.accessToken(), response.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Connexion (alias sans /v1/).
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthResponse response = authService.login(request, httpRequest);
        cookieUtils.setAuthCookies(httpResponse, response.accessToken(), response.refreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Rafraîchir les tokens (alias sans /v1/).
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String refreshToken = request != null && request.refreshToken() != null
                ? request.refreshToken()
                : cookieUtils.getRefreshTokenFromCookie(httpRequest)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token manquant"));

        AuthResponse response = authService.refreshToken(
                new RefreshTokenRequest(refreshToken),
                httpRequest
        );

        cookieUtils.setAuthCookies(httpResponse, response.accessToken(), response.refreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Déconnexion (alias sans /v1/).
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String refreshToken = cookieUtils.getRefreshTokenFromCookie(httpRequest)
                .orElse(null);

        if (refreshToken != null) {
            authService.logout(new RefreshTokenRequest(refreshToken));
        }

        cookieUtils.clearAuthCookies(httpResponse);
        return ResponseEntity.noContent().build();
    }

    /**
     * Infos utilisateur courant (alias sans /v1/).
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserResponse user = UserResponse.fromEntity(userDetails.getUser());
        return ResponseEntity.ok(user);
    }
}
