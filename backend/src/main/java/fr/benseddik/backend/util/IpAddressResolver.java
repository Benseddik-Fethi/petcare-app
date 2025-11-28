package fr.benseddik.backend.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 🛡️ PROTECTION ANTI-SPOOFING : Résolution sécurisée de l'IP client.
 *
 * Empêche les attaquants de forger l'IP via X-Forwarded-For en ne faisant
 * confiance à ce header que si la requête vient d'un proxy autorisé.
 *
 * Configuration :
 * - app.security.trusted-proxies=127.0.0.1,::1,proxy-ip
 *
 * Références :
 * - OWASP: https://cheatsheetseries.owasp.org/cheatsheets/Attack_Surface_Analysis_Cheat_Sheet.html
 * - RFC 7239: https://datatracker.ietf.org/doc/html/rfc7239
 */
@Component
@Slf4j
public class IpAddressResolver {

    private final Set<String> trustedProxies;

    public IpAddressResolver(
            @Value("${app.security.trusted-proxies:127.0.0.1,::1}") String trustedProxiesConfig
    ) {
        this.trustedProxies = new HashSet<>(Arrays.asList(trustedProxiesConfig.split(",")));
        log.info("🛡️ Trusted proxies configurés : {}", trustedProxies);
    }

    /**
     * Extrait l'IP réelle du client avec protection anti-spoofing.
     *
     * Logique :
     * 1. Si la requête ne vient PAS d'un proxy de confiance → retourne l'IP directe
     * 2. Si la requête vient d'un proxy de confiance → utilise X-Forwarded-For
     *
     * @param request Requête HTTP
     * @return IP réelle du client
     */
    public String resolveClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        // SÉCURITÉ : Vérifier que la requête vient d'un proxy de confiance
        if (!trustedProxies.contains(remoteAddr)) {
            // Requête directe ou proxy non autorisé → utiliser l'IP directe
            log.trace("IP directe (pas de proxy de confiance) : {}", remoteAddr);
            return remoteAddr;
        }

        // Requête d'un proxy de confiance → utiliser X-Forwarded-For
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(forwardedFor)) {
            // X-Forwarded-For peut contenir : "client-ip, proxy1, proxy2"
            String clientIp = forwardedFor.split(",")[0].trim();
            log.trace("IP résolue via X-Forwarded-For : {} (remoteAddr={})", clientIp, remoteAddr);
            return clientIp;
        }

        // Fallback : X-Real-IP (utilisé par Nginx)
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty() && !"unknown".equalsIgnoreCase(realIp)) {
            log.trace("IP résolue via X-Real-IP : {} (remoteAddr={})", realIp, remoteAddr);
            return realIp;
        }

        // Aucun header valide → utiliser l'IP du proxy
        log.trace("Aucun header valide, IP du proxy : {}", remoteAddr);
        return remoteAddr;
    }
}
