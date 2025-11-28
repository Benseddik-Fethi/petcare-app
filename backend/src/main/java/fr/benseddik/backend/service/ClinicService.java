package fr.benseddik.backend.service;

import fr.benseddik.backend.dto.response.ClinicResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service pour la gestion des cliniques vétérinaires.
 *
 * 🔓 DONNÉES PUBLIQUES : Les cliniques sont accessibles à tous les utilisateurs.
 */
public interface ClinicService {

    /**
     * Récupère toutes les cliniques.
     */
    List<ClinicResponse> getAllClinics();

    /**
     * Récupère une clinique par ID.
     */
    ClinicResponse getClinicById(UUID clinicId);

    /**
     * Recherche des cliniques par nom.
     */
    List<ClinicResponse> searchClinicsByName(String query);

    /**
     * Recherche des cliniques par ville.
     */
    List<ClinicResponse> searchClinicsByCity(String city);
}
