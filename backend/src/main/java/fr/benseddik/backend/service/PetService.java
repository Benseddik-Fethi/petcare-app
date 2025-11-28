package fr.benseddik.backend.service;

import fr.benseddik.backend.dto.request.CreatePetRequest;
import fr.benseddik.backend.dto.request.UpdatePetRequest;
import fr.benseddik.backend.dto.response.PetResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service pour la gestion des animaux de compagnie.
 *
 * 🛡️ SÉCURITÉ : Toutes les méthodes filtrent par propriétaire.
 */
public interface PetService {

    /**
     * Crée un nouvel animal pour l'utilisateur connecté.
     */
    PetResponse createPet(CreatePetRequest request, UUID userId);

    /**
     * Récupère tous les animaux de l'utilisateur connecté.
     */
    List<PetResponse> getAllPets(UUID userId);

    /**
     * Récupère un animal par ID (avec vérification de propriété).
     */
    PetResponse getPetById(UUID petId, UUID userId);

    /**
     * Met à jour un animal (avec vérification de propriété).
     */
    PetResponse updatePet(UUID petId, UpdatePetRequest request, UUID userId);

    /**
     * Supprime un animal (avec vérification de propriété).
     */
    void deletePet(UUID petId, UUID userId);

    /**
     * Recherche des animaux par nom ou race.
     */
    List<PetResponse> searchPets(String query, UUID userId);
}
