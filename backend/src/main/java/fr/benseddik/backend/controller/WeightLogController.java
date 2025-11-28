package fr.benseddik.backend.controller;

import fr.benseddik.backend.domain.User;
import fr.benseddik.backend.dto.request.CreateWeightLogRequest;
import fr.benseddik.backend.dto.response.WeightLogResponse;
import fr.benseddik.backend.service.WeightLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des pesées.
 *
 * 🛡️ SÉCURITÉ :
 * - Nécessite une authentification
 * - Vérifie que l'animal appartient à l'utilisateur
 */
@Slf4j
@RestController
@RequestMapping("/v1/weight-logs")
@RequiredArgsConstructor
public class WeightLogController {

    private final WeightLogService weightLogService;

    /**
     * Enregistre une nouvelle pesée.
     */
    @PostMapping
    public ResponseEntity<WeightLogResponse> createWeightLog(
            @Valid @RequestBody CreateWeightLogRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("⚖️ POST /v1/weight-logs - Enregistrement d'une pesée");

        WeightLogResponse response = weightLogService.createWeightLog(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupère l'historique des pesées d'un animal.
     */
    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<WeightLogResponse>> getWeightLogsByPet(
            @PathVariable UUID petId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("🔍 GET /v1/weight-logs/pet/{}", petId);

        List<WeightLogResponse> weightLogs = weightLogService.getWeightLogsByPet(petId, user.getId());
        return ResponseEntity.ok(weightLogs);
    }

    /**
     * Récupère la dernière pesée d'un animal.
     */
    @GetMapping("/pet/{petId}/latest")
    public ResponseEntity<WeightLogResponse> getLatestWeightLog(
            @PathVariable UUID petId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("🔍 GET /v1/weight-logs/pet/{}/latest", petId);

        WeightLogResponse weightLog = weightLogService.getLatestWeightLog(petId, user.getId());
        return ResponseEntity.ok(weightLog);
    }

    /**
     * Récupère les pesées dans une période.
     */
    @GetMapping("/pet/{petId}/range")
    public ResponseEntity<List<WeightLogResponse>> getWeightLogsBetween(
            @PathVariable UUID petId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @AuthenticationPrincipal User user
    ) {
        log.debug("🔍 GET /v1/weight-logs/pet/{}/range?start={}&end={}", petId, start, end);

        List<WeightLogResponse> weightLogs = weightLogService.getWeightLogsBetween(petId, user.getId(), start, end);
        return ResponseEntity.ok(weightLogs);
    }

    /**
     * Supprime une pesée.
     */
    @DeleteMapping("/{weightLogId}")
    public ResponseEntity<Void> deleteWeightLog(
            @PathVariable UUID weightLogId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("🗑️ DELETE /v1/weight-logs/{}", weightLogId);

        weightLogService.deleteWeightLog(weightLogId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
