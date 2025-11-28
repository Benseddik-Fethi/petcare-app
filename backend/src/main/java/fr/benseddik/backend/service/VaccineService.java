package fr.benseddik.backend.service;

import fr.benseddik.backend.dto.request.CreateVaccineRequest;
import fr.benseddik.backend.dto.response.VaccineResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service pour la gestion des vaccins.
 *
 * 🛡️ SÉCURITÉ : Vérification que l'animal appartient à l'utilisateur.
 */
public interface VaccineService {

    /**
     * Enregistre un nouveau vaccin pour un animal.
     */
    VaccineResponse createVaccine(CreateVaccineRequest request, UUID userId);

    /**
     * Récupère tous les vaccins d'un animal.
     */
    List<VaccineResponse> getVaccinesByPet(UUID petId, UUID userId);

    /**
     * Récupère un vaccin par ID.
     */
    VaccineResponse getVaccineById(UUID vaccineId, UUID userId);

    /**
     * Récupère les vaccins expirés d'un animal.
     */
    List<VaccineResponse> getExpiredVaccines(UUID petId, UUID userId);

    /**
     * Récupère les rappels à venir d'un animal.
     */
    List<VaccineResponse> getUpcomingVaccines(UUID petId, UUID userId);

    /**
     * Supprime un vaccin.
     */
    void deleteVaccine(UUID vaccineId, UUID userId);
}
