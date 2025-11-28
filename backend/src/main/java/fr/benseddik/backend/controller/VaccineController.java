package fr.benseddik.backend.controller;

import fr.benseddik.backend.domain.User;
import fr.benseddik.backend.dto.request.CreateVaccineRequest;
import fr.benseddik.backend.dto.response.VaccineResponse;
import fr.benseddik.backend.service.VaccineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des vaccins.
 *
 * 🛡️ SÉCURITÉ :
 * - Nécessite une authentification
 * - Vérifie que l'animal appartient à l'utilisateur
 */
@Slf4j
@RestController
@RequestMapping("/v1/vaccines")
@RequiredArgsConstructor
public class VaccineController {

    private final VaccineService vaccineService;

    /**
     * Enregistre un nouveau vaccin.
     */
    @PostMapping
    public ResponseEntity<VaccineResponse> createVaccine(
            @Valid @RequestBody CreateVaccineRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("💉 POST /v1/vaccines - Enregistrement d'un vaccin");

        VaccineResponse response = vaccineService.createVaccine(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupère tous les vaccins d'un animal.
     */
    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<VaccineResponse>> getVaccinesByPet(
            @PathVariable UUID petId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("🔍 GET /v1/vaccines/pet/{}", petId);

        List<VaccineResponse> vaccines = vaccineService.getVaccinesByPet(petId, user.getId());
        return ResponseEntity.ok(vaccines);
    }

    /**
     * Récupère un vaccin par ID.
     */
    @GetMapping("/{vaccineId}")
    public ResponseEntity<VaccineResponse> getVaccineById(
            @PathVariable UUID vaccineId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("🔍 GET /v1/vaccines/{}", vaccineId);

        VaccineResponse vaccine = vaccineService.getVaccineById(vaccineId, user.getId());
        return ResponseEntity.ok(vaccine);
    }

    /**
     * Récupère les vaccins expirés d'un animal.
     */
    @GetMapping("/pet/{petId}/expired")
    public ResponseEntity<List<VaccineResponse>> getExpiredVaccines(
            @PathVariable UUID petId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("🔍 GET /v1/vaccines/pet/{}/expired", petId);

        List<VaccineResponse> vaccines = vaccineService.getExpiredVaccines(petId, user.getId());
        return ResponseEntity.ok(vaccines);
    }

    /**
     * Récupère les rappels à venir d'un animal.
     */
    @GetMapping("/pet/{petId}/upcoming")
    public ResponseEntity<List<VaccineResponse>> getUpcomingVaccines(
            @PathVariable UUID petId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("🔍 GET /v1/vaccines/pet/{}/upcoming", petId);

        List<VaccineResponse> vaccines = vaccineService.getUpcomingVaccines(petId, user.getId());
        return ResponseEntity.ok(vaccines);
    }

    /**
     * Supprime un vaccin.
     */
    @DeleteMapping("/{vaccineId}")
    public ResponseEntity<Void> deleteVaccine(
            @PathVariable UUID vaccineId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("🗑️ DELETE /v1/vaccines/{}", vaccineId);

        vaccineService.deleteVaccine(vaccineId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
