package fr.benseddik.backend.service.impl;

import fr.benseddik.backend.domain.Vet;
import fr.benseddik.backend.dto.response.VetResponse;
import fr.benseddik.backend.exception.ResourceNotFoundException;
import fr.benseddik.backend.repository.VetRepository;
import fr.benseddik.backend.service.VetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des vétérinaires.
 *
 * 🔓 DONNÉES PUBLIQUES : Accessibles à tous les utilisateurs authentifiés.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VetServiceImpl implements VetService {

    private final VetRepository vetRepository;

    @Override
    public List<VetResponse> getAllVets() {
        log.debug("🔍 Récupération de tous les vétérinaires");

        List<Vet> vets = vetRepository.findAll();

        return vets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VetResponse getVetById(UUID vetId) {
        log.debug("🔍 Récupération du vétérinaire: {}", vetId);

        Vet vet = vetRepository.findById(vetId)
                .orElseThrow(() -> new ResourceNotFoundException("Vétérinaire introuvable"));

        return mapToResponse(vet);
    }

    @Override
    public List<VetResponse> getVetsByClinic(UUID clinicId) {
        log.debug("🔍 Récupération des vétérinaires de la clinique: {}", clinicId);

        List<Vet> vets = vetRepository.findByClinicId(clinicId);

        return vets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VetResponse> searchVetsByName(String query) {
        log.debug("🔎 Recherche de vétérinaires avec le terme: '{}'", query);

        List<Vet> vets = vetRepository.searchByName(query);

        return vets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VetResponse> searchVetsBySpecialty(String specialty) {
        log.debug("🔎 Recherche de vétérinaires par spécialité: '{}'", specialty);

        List<Vet> vets = vetRepository.findBySpecialty(specialty);

        return vets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPER
    // ═══════════════════════════════════════════════════════════════════════════

    private VetResponse mapToResponse(Vet vet) {
        return new VetResponse(
                vet.getId(),
                vet.getFirstName(),
                vet.getLastName(),
                vet.getFullName(),
                vet.getSpecialty(),
                vet.getPhone(),
                vet.getEmail(),
                vet.getAvatar(),
                vet.getClinic() != null ? vet.getClinic().getId() : null,
                vet.getClinic() != null ? vet.getClinic().getName() : null,
                vet.getCreatedAt(),
                vet.getUpdatedAt()
        );
    }
}
