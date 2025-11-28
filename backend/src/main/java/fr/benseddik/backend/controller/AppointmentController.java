package fr.benseddik.backend.controller;

import fr.benseddik.backend.domain.User;
import fr.benseddik.backend.dto.request.CreateAppointmentRequest;
import fr.benseddik.backend.dto.request.UpdateAppointmentRequest;
import fr.benseddik.backend.dto.response.AppointmentResponse;
import fr.benseddik.backend.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des rendez-vous vétérinaires.
 *
 * 🛡️ SÉCURITÉ :
 * - Nécessite une authentification
 * - Toutes les opérations sont filtrées par utilisateur
 */
@Slf4j
@RestController
@RequestMapping("/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Crée un nouveau rendez-vous.
     */
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("📅 POST /v1/appointments - Création d'un rendez-vous");

        AppointmentResponse response = appointmentService.createAppointment(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupère tous les rendez-vous de l'utilisateur.
     */
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments(
            @AuthenticationPrincipal User user
    ) {
        log.debug("🔍 GET /v1/appointments - Récupération des rendez-vous");

        List<AppointmentResponse> appointments = appointmentService.getAllAppointments(user.getId());
        return ResponseEntity.ok(appointments);
    }

    /**
     * Récupère les rendez-vous à venir.
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<AppointmentResponse>> getUpcomingAppointments(
            @AuthenticationPrincipal User user
    ) {
        log.debug("🔍 GET /v1/appointments/upcoming");

        List<AppointmentResponse> appointments = appointmentService.getUpcomingAppointments(user.getId());
        return ResponseEntity.ok(appointments);
    }

    /**
     * Récupère un rendez-vous par ID.
     */
    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(
            @PathVariable UUID appointmentId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("🔍 GET /v1/appointments/{}", appointmentId);

        AppointmentResponse appointment = appointmentService.getAppointmentById(appointmentId, user.getId());
        return ResponseEntity.ok(appointment);
    }

    /**
     * Récupère les rendez-vous d'un animal.
     */
    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByPet(
            @PathVariable UUID petId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("🔍 GET /v1/appointments/pet/{}", petId);

        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByPet(petId, user.getId());
        return ResponseEntity.ok(appointments);
    }

    /**
     * Récupère les rendez-vous dans une période.
     */
    @GetMapping("/range")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @AuthenticationPrincipal User user
    ) {
        log.debug("🔍 GET /v1/appointments/range?start={}&end={}", start, end);

        List<AppointmentResponse> appointments = appointmentService.getAppointmentsBetween(user.getId(), start, end);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Met à jour un rendez-vous.
     */
    @PatchMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody UpdateAppointmentRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("✏️ PATCH /v1/appointments/{}", appointmentId);

        AppointmentResponse appointment = appointmentService.updateAppointment(appointmentId, request, user.getId());
        return ResponseEntity.ok(appointment);
    }

    /**
     * Annule un rendez-vous.
     */
    @PostMapping("/{appointmentId}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable UUID appointmentId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("❌ POST /v1/appointments/{}/cancel", appointmentId);

        AppointmentResponse appointment = appointmentService.cancelAppointment(appointmentId, user.getId());
        return ResponseEntity.ok(appointment);
    }

    /**
     * Supprime un rendez-vous.
     */
    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<Void> deleteAppointment(
            @PathVariable UUID appointmentId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("🗑️ DELETE /v1/appointments/{}", appointmentId);

        appointmentService.deleteAppointment(appointmentId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
