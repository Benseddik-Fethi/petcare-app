package fr.benseddik.backend.controller;

import fr.benseddik.backend.dto.response.VetResponse;
import fr.benseddik.backend.service.VetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des vétérinaires.
 *
 * 🔓 DONNÉES PUBLIQUES : Accessibles à tous les utilisateurs authentifiés.
 */
@Slf4j
@RestController
@RequestMapping("/v1/vets")
@RequiredArgsConstructor
public class VetController {

    private final VetService vetService;

    /**
     * Récupère tous les vétérinaires.
     */
    @GetMapping
    public ResponseEntity<List<VetResponse>> getAllVets() {
        log.debug("🔍 GET /v1/vets - Récupération des vétérinaires");

        List<VetResponse> vets = vetService.getAllVets();
        return ResponseEntity.ok(vets);
    }

    /**
     * Récupère un vétérinaire par ID.
     */
    @GetMapping("/{vetId}")
    public ResponseEntity<VetResponse> getVetById(@PathVariable UUID vetId) {
        log.debug("🔍 GET /v1/vets/{}", vetId);

        VetResponse vet = vetService.getVetById(vetId);
        return ResponseEntity.ok(vet);
    }

    /**
     * Récupère les vétérinaires d'une clinique.
     */
    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<List<VetResponse>> getVetsByClinic(@PathVariable UUID clinicId) {
        log.debug("🔍 GET /v1/vets/clinic/{}", clinicId);

        List<VetResponse> vets = vetService.getVetsByClinic(clinicId);
        return ResponseEntity.ok(vets);
    }

    /**
     * Recherche des vétérinaires par nom.
     */
    @GetMapping("/search")
    public ResponseEntity<List<VetResponse>> searchVets(@RequestParam String query) {
        log.debug("🔎 GET /v1/vets/search?query={}", query);

        List<VetResponse> vets = vetService.searchVetsByName(query);
        return ResponseEntity.ok(vets);
    }

    /**
     * Recherche des vétérinaires par spécialité.
     */
    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<VetResponse>> searchVetsBySpecialty(@PathVariable String specialty) {
        log.debug("🔎 GET /v1/vets/specialty/{}", specialty);

        List<VetResponse> vets = vetService.searchVetsBySpecialty(specialty);
        return ResponseEntity.ok(vets);
    }
}
