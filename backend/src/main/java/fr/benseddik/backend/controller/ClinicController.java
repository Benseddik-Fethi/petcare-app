package fr.benseddik.backend.controller;

import fr.benseddik.backend.dto.response.ClinicResponse;
import fr.benseddik.backend.service.ClinicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des cliniques vétérinaires.
 *
 * 🔓 DONNÉES PUBLIQUES : Accessibles à tous les utilisateurs authentifiés.
 */
@Slf4j
@RestController
@RequestMapping("/v1/clinics")
@RequiredArgsConstructor
public class ClinicController {

    private final ClinicService clinicService;

    /**
     * Récupère toutes les cliniques.
     */
    @GetMapping
    public ResponseEntity<List<ClinicResponse>> getAllClinics() {
        log.debug("🔍 GET /v1/clinics - Récupération des cliniques");

        List<ClinicResponse> clinics = clinicService.getAllClinics();
        return ResponseEntity.ok(clinics);
    }

    /**
     * Récupère une clinique par ID.
     */
    @GetMapping("/{clinicId}")
    public ResponseEntity<ClinicResponse> getClinicById(@PathVariable UUID clinicId) {
        log.debug("🔍 GET /v1/clinics/{}", clinicId);

        ClinicResponse clinic = clinicService.getClinicById(clinicId);
        return ResponseEntity.ok(clinic);
    }

    /**
     * Recherche des cliniques par nom.
     */
    @GetMapping("/search")
    public ResponseEntity<List<ClinicResponse>> searchClinics(@RequestParam String query) {
        log.debug("🔎 GET /v1/clinics/search?query={}", query);

        List<ClinicResponse> clinics = clinicService.searchClinicsByName(query);
        return ResponseEntity.ok(clinics);
    }

    /**
     * Recherche des cliniques par ville.
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<ClinicResponse>> searchClinicsByCity(@PathVariable String city) {
        log.debug("🔎 GET /v1/clinics/city/{}", city);

        List<ClinicResponse> clinics = clinicService.searchClinicsByCity(city);
        return ResponseEntity.ok(clinics);
    }
}
