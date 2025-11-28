package fr.benseddik.backend.service;

import fr.benseddik.backend.dto.response.VetResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service pour la gestion des vétérinaires.
 *
 * 🔓 DONNÉES PUBLIQUES : Les vétérinaires sont accessibles à tous les utilisateurs.
 */
public interface VetService {

    /**
     * Récupère tous les vétérinaires.
     */
    List<VetResponse> getAllVets();

    /**
     * Récupère un vétérinaire par ID.
     */
    VetResponse getVetById(UUID vetId);

    /**
     * Récupère les vétérinaires d'une clinique.
     */
    List<VetResponse> getVetsByClinic(UUID clinicId);

    /**
     * Recherche des vétérinaires par nom.
     */
    List<VetResponse> searchVetsByName(String query);

    /**
     * Recherche des vétérinaires par spécialité.
     */
    List<VetResponse> searchVetsBySpecialty(String specialty);
}
