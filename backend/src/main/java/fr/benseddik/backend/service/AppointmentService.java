package fr.benseddik.backend.service;

import fr.benseddik.backend.dto.request.CreateAppointmentRequest;
import fr.benseddik.backend.dto.request.UpdateAppointmentRequest;
import fr.benseddik.backend.dto.response.AppointmentResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service pour la gestion des rendez-vous vétérinaires.
 *
 * 🛡️ SÉCURITÉ : Toutes les méthodes filtrent par utilisateur.
 */
public interface AppointmentService {

    /**
     * Crée un nouveau rendez-vous pour l'utilisateur connecté.
     */
    AppointmentResponse createAppointment(CreateAppointmentRequest request, UUID userId);

    /**
     * Récupère tous les rendez-vous de l'utilisateur.
     */
    List<AppointmentResponse> getAllAppointments(UUID userId);

    /**
     * Récupère les rendez-vous à venir de l'utilisateur.
     */
    List<AppointmentResponse> getUpcomingAppointments(UUID userId);

    /**
     * Récupère un rendez-vous par ID (avec vérification de propriété).
     */
    AppointmentResponse getAppointmentById(UUID appointmentId, UUID userId);

    /**
     * Récupère les rendez-vous d'un animal spécifique.
     */
    List<AppointmentResponse> getAppointmentsByPet(UUID petId, UUID userId);

    /**
     * Récupère les rendez-vous dans une période donnée.
     */
    List<AppointmentResponse> getAppointmentsBetween(UUID userId, LocalDateTime start, LocalDateTime end);

    /**
     * Met à jour un rendez-vous (avec vérification de propriété).
     */
    AppointmentResponse updateAppointment(UUID appointmentId, UpdateAppointmentRequest request, UUID userId);

    /**
     * Annule un rendez-vous.
     */
    AppointmentResponse cancelAppointment(UUID appointmentId, UUID userId);

    /**
     * Supprime un rendez-vous (avec vérification de propriété).
     */
    void deleteAppointment(UUID appointmentId, UUID userId);
}
