package fr.benseddik.backend.service;

import fr.benseddik.backend.dto.request.CreateWeightLogRequest;
import fr.benseddik.backend.dto.response.WeightLogResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service pour la gestion de l'historique des pesées.
 *
 * 🛡️ SÉCURITÉ : Vérification que l'animal appartient à l'utilisateur.
 */
public interface WeightLogService {

    /**
     * Enregistre une nouvelle pesée pour un animal.
     */
    WeightLogResponse createWeightLog(CreateWeightLogRequest request, UUID userId);

    /**
     * Récupère l'historique des pesées d'un animal.
     */
    List<WeightLogResponse> getWeightLogsByPet(UUID petId, UUID userId);

    /**
     * Récupère la dernière pesée d'un animal.
     */
    WeightLogResponse getLatestWeightLog(UUID petId, UUID userId);

    /**
     * Récupère les pesées dans une période donnée.
     */
    List<WeightLogResponse> getWeightLogsBetween(UUID petId, UUID userId, LocalDate start, LocalDate end);

    /**
     * Supprime une pesée.
     */
    void deleteWeightLog(UUID weightLogId, UUID userId);
}
