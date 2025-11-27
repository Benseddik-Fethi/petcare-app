package fr.benseddik.backend.controller;

import fr.benseddik.backend.dto.request.ChangePasswordRequest;
import fr.benseddik.backend.dto.request.ForgotPasswordRequest;
import fr.benseddik.backend.dto.request.ResendVerificationRequest;
import fr.benseddik.backend.dto.request.ResetPasswordRequest;
import fr.benseddik.backend.dto.response.UserResponse;
import fr.benseddik.backend.security.CustomUserDetails;
import fr.benseddik.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur REST pour la gestion du compte utilisateur.
 *
 * Endpoints publics :
 * - POST /api/v1/users/verify-email - Vérifier l'email
 * - POST /api/v1/users/resend-verification - Renvoyer l'email de vérification
 * - POST /api/v1/users/forgot-password - Demander reset password
 * - GET  /api/v1/users/reset-password/validate - Valider token reset
 * - POST /api/v1/users/reset-password - Réinitialiser le password
 *
 * Endpoints authentifiés :
 * - POST /api/v1/users/change-password - Changer le password
 * - GET  /api/v1/users/profile - Récupérer le profil
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ═══════════════════════════════════════════════════════════════════════════
    // VÉRIFICATION D'EMAIL (Public)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Vérifie l'adresse email avec le token reçu par email.
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String token) {
        boolean verified = userService.verifyEmail(token);

        if (verified) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email vérifié avec succès"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Token invalide ou expiré"
            ));
        }
    }

    /**
     * Renvoie l'email de vérification.
     * 🛡️ Message générique pour ne pas révéler si l'email existe.
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request
    ) {
        userService.resendVerificationEmail(request);

        return ResponseEntity.ok(Map.of(
                "message", "Si un compte existe avec cet email et n'est pas encore vérifié, un email a été envoyé"
        ));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RÉINITIALISATION DE MOT DE PASSE (Public)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Demande une réinitialisation de mot de passe.
     * 🛡️ Message générique pour ne pas révéler si l'email existe.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        userService.forgotPassword(request);

        return ResponseEntity.ok(Map.of(
                "message", "Si un compte existe avec cet email, un lien de réinitialisation a été envoyé"
        ));
    }

    /**
     * Vérifie si un token de réinitialisation est valide.
     * Utilisé par le frontend pour afficher le formulaire.
     */
    @GetMapping("/reset-password/validate")
    public ResponseEntity<Map<String, Boolean>> validateResetToken(@RequestParam String token) {
        boolean valid = userService.isResetTokenValid(token);
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    /**
     * Réinitialise le mot de passe avec le token.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        userService.resetPassword(request);

        return ResponseEntity.ok(Map.of(
                "message", "Mot de passe réinitialisé avec succès"
        ));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GESTION DU COMPTE (Authentifié)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Change le mot de passe de l'utilisateur connecté.
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(userDetails.getId(), request);

        return ResponseEntity.ok(Map.of(
                "message", "Mot de passe modifié avec succès"
        ));
    }

    /**
     * Récupère le profil de l'utilisateur connecté.
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserResponse user = userService.getUserById(userDetails.getId());
        return ResponseEntity.ok(user);
    }

    /**
     * Demande l'envoi d'un email de vérification (utilisateur connecté).
     */
    @PostMapping("/send-verification")
    public ResponseEntity<Map<String, String>> sendVerificationEmail(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.sendVerificationEmail(userDetails.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Email de vérification envoyé"
        ));
    }
}