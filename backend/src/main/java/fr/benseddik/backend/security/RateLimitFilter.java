package fr.benseddik.backend.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.benseddik.backend.config.SecurityProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 🛡️ SÉCURITÉ NIVEAU BANCAIRE : Rate Limiting par IP.
 *
 * Protection contre :
 * - DDoS application-level
 * - Brute force distribué
 * - Abus d'API
 *
 * Utilise Bucket4j (Token Bucket Algorithm) avec cache Caffeine.
 * Pour production : remplacer Caffeine par Redis (cache distribué).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;

    // Cache en mémoire (Caffeine) : IP -> Bucket
    // ⚠️ En production : utiliser Redis pour un cache distribué entre instances
    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .maximumSize(100_000)
            .build();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!securityProperties.rateLimit().enabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        String path = request.getServletPath();

        // Déterminer la limite selon le type d'endpoint
        int limit = isAuthEndpoint(path)
                ? securityProperties.rateLimit().authRequestsPerMinute()
                : securityProperties.rateLimit().requestsPerMinute();

        // Récupérer ou créer le bucket pour cette IP
        Bucket bucket = cache.get(ip, key -> createBucket(limit));

        if (bucket == null || !bucket.tryConsume(1)) {
            // Rate limit dépassé
            log.warn("Rate limit dépassé pour IP: {} sur {}", ip, path);
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write(
                String.format(
                    "{\"error\":\"Too Many Requests\",\"message\":\"Limite de %d requêtes/minute dépassée. Réessayez plus tard.\"}",
                    limit
                )
            );
            return;
        }

        // Ajouter header avec nombre de requêtes restantes
        long remaining = bucket.getAvailableTokens();
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));

        filterChain.doFilter(request, response);
    }

    /**
     * Crée un bucket avec la limite spécifiée (Token Bucket Algorithm).
     */
    private Bucket createBucket(int requestsPerMinute) {
        Bandwidth limit = Bandwidth.classic(
            requestsPerMinute,
            Refill.intervally(requestsPerMinute, Duration.ofMinutes(1))
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Détermine si l'endpoint est un endpoint d'authentification (limite plus stricte).
     */
    private boolean isAuthEndpoint(String path) {
        return path.startsWith("/api/v1/auth/login") ||
               path.startsWith("/api/v1/auth/register") ||
               path.startsWith("/api/v1/auth/refresh") ||
               path.startsWith("/api/v1/users/forgot-password") ||
               path.startsWith("/api/v1/users/reset-password");
    }

    /**
     * Extrait l'IP du client (gère les proxies et load balancers).
     */
    private String getClientIp(HttpServletRequest request) {
        // Headers standards pour IP derrière proxy/load balancer
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For peut contenir plusieurs IPs : "client, proxy1, proxy2"
            // Prendre la première (IP du client)
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Ne pas appliquer le rate limiting sur les endpoints de santé
        String path = request.getServletPath();
        return path.startsWith("/actuator/health");
    }
}
