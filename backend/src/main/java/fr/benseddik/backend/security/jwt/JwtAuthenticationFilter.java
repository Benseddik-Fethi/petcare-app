package fr.benseddik.backend.security.jwt;

import fr.benseddik.backend.security.CookieUtils;
import fr.benseddik.backend.security.CustomUserDetailsService;
import fr.benseddik.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtre JWT pour intercepter et valider les tokens d'authentification.
 *
 * 🛡️ Sécurité :
 * - Exécuté une seule fois par requête (OncePerRequestFilter)
 * - Lit le token depuis : Cookie HTTP-only OU Header Authorization
 * - Valide le token avant de définir l'authentification
 * - Vérifie que c'est un access token (pas un refresh token)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final CookieUtils cookieUtils;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateWithToken(token, request);
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'authentification JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrait le token JWT : d'abord depuis le cookie, sinon depuis le header.
     *
     * Priorité :
     * 1. Cookie HTTP-only (plus sécurisé)
     * 2. Header Authorization: Bearer xxx (pour les clients qui ne supportent pas les cookies)
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // 1. Essayer d'abord le cookie HTTP-only
        String token = cookieUtils.getAccessTokenFromCookie(request).orElse(null);

        if (token != null) {
            log.trace("Token extrait du cookie");
            return token;
        }

        // 2. Fallback sur le header Authorization
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            log.trace("Token extrait du header Authorization");
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * Authentifie l'utilisateur à partir du token JWT.
     */
    private void authenticateWithToken(String token, HttpServletRequest request) {
        if (!jwtService.isTokenValid(token)) {
            log.debug("Token JWT invalide");
            return;
        }

        if (!jwtService.isAccessToken(token)) {
            log.debug("Le token n'est pas un access token");
            return;
        }

        UUID userId = jwtService.extractUserId(token).orElse(null);
        if (userId == null) {
            log.debug("Impossible d'extraire l'ID utilisateur du token");
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserById(userId);

        if (!userDetails.isAccountNonLocked()) {
            log.debug("Compte utilisateur verrouillé: {}", userId);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Utilisateur authentifié via JWT: {}", userId);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/login") ||
                path.startsWith("/api/v1/auth/register") ||
                path.startsWith("/api/v1/auth/refresh") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs");
    }
}