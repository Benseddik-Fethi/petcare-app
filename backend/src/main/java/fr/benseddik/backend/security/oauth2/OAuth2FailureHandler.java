package fr.benseddik.backend.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Handler d'échec OAuth2.
 * Redirige vers le frontend avec un message d'erreur générique.
 *
 * 🛡️ SÉCURITÉ : Ne pas exposer les détails techniques de l'exception dans l'URL
 * (visibles dans historique navigateur, logs proxy, headers Referer).
 */
@Component
@Slf4j
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.security.cors.allowed-origins:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        // Logger les détails complets côté serveur (avec stacktrace si nécessaire)
        log.error("OAuth2 authentication failed - Type: {} - Message: {}",
                exception.getClass().getSimpleName(),
                exception.getMessage());
        log.debug("OAuth2 failure details", exception);

        // Redirection avec message générique (pas d'exposition de détails techniques)
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/error")
                .queryParam("error", "oauth2_error")
                .queryParam("message", "L'authentification a échoué. Veuillez réessayer.")
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}