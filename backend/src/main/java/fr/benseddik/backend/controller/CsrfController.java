package fr.benseddik.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint CSRF pour compatibilité avec le frontend React.
 *
 * ⚠️ Note : CSRF est désactivé dans Spring Security car cette API est stateless (JWT).
 * Cet endpoint existe uniquement pour éviter les erreurs 404 du frontend qui
 * essaie de récupérer un token CSRF.
 *
 * Le frontend peut appeler /api/csrf-token mais le token retourné n'est pas utilisé
 * car CSRF est désactivé côté backend.
 */
@RestController
@RequestMapping("/api")
public class CsrfController {

    /**
     * Endpoint CSRF dummy pour compatibilité frontend.
     *
     * Retourne un token "disabled" car CSRF est désactivé (API stateless JWT).
     */
    @GetMapping("/csrf-token")
    public Map<String, String> getCsrfToken() {
        return Map.of(
            "csrfToken", "disabled",
            "message", "CSRF protection is disabled for this stateless JWT API"
        );
    }
}
