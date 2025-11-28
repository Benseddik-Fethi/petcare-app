package fr.benseddik.backend.service.impl;

import fr.benseddik.backend.domain.Clinic;
import fr.benseddik.backend.dto.response.ClinicResponse;
import fr.benseddik.backend.exception.ResourceNotFoundException;
import fr.benseddik.backend.repository.ClinicRepository;
import fr.benseddik.backend.service.ClinicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des cliniques vétérinaires.
 *
 * 🔓 DONNÉES PUBLIQUES : Accessibles à tous les utilisateurs authentifiés.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClinicServiceImpl implements ClinicService {

    private final ClinicRepository clinicRepository;

    @Override
    public List<ClinicResponse> getAllClinics() {
        log.debug("🔍 Récupération de toutes les cliniques");

        List<Clinic> clinics = clinicRepository.findAll();

        return clinics.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ClinicResponse getClinicById(UUID clinicId) {
        log.debug("🔍 Récupération de la clinique: {}", clinicId);

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinique introuvable"));

        return mapToResponse(clinic);
    }

    @Override
    public List<ClinicResponse> searchClinicsByName(String query) {
        log.debug("🔎 Recherche de cliniques avec le terme: '{}'", query);

        List<Clinic> clinics = clinicRepository.searchByName(query);

        return clinics.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClinicResponse> searchClinicsByCity(String city) {
        log.debug("🔎 Recherche de cliniques par ville: '{}'", city);

        List<Clinic> clinics = clinicRepository.findByCity(city);

        return clinics.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPER
    // ═══════════════════════════════════════════════════════════════════════════

    private ClinicResponse mapToResponse(Clinic clinic) {
        return new ClinicResponse(
                clinic.getId(),
                clinic.getName(),
                clinic.getAddress(),
                clinic.getPhone(),
                clinic.getEmail(),
                clinic.getWebsite(),
                clinic.getOpeningHours(),
                clinic.getCreatedAt(),
                clinic.getUpdatedAt()
        );
    }
}
