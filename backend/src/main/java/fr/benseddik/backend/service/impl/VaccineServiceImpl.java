package fr.benseddik.backend.service.impl;

import fr.benseddik.backend.domain.Pet;
import fr.benseddik.backend.domain.Vaccine;
import fr.benseddik.backend.dto.request.CreateVaccineRequest;
import fr.benseddik.backend.dto.response.VaccineResponse;
import fr.benseddik.backend.exception.ResourceNotFoundException;
import fr.benseddik.backend.repository.PetRepository;
import fr.benseddik.backend.repository.VaccineRepository;
import fr.benseddik.backend.service.VaccineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des vaccins.
 *
 * 🛡️ SÉCURITÉ :
 * - Vérification que l'animal appartient à l'utilisateur
 * - Mise à jour automatique du statut des vaccins
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VaccineServiceImpl implements VaccineService {

    private final VaccineRepository vaccineRepository;
    private final PetRepository petRepository;

    @Override
    @Transactional
    public VaccineResponse createVaccine(CreateVaccineRequest request, UUID userId) {
        log.debug("💉 Enregistrement d'un vaccin pour l'animal: {}", request.petId());

        // 🛡️ SÉCURITÉ : Vérifier que l'animal appartient à l'utilisateur
        Pet pet = petRepository.findByIdAndOwnerId(request.petId(), userId)
                .orElseThrow(() -> new AccessDeniedException("Cet animal ne vous appartient pas"));

        Vaccine vaccine = Vaccine.builder()
                .name(request.name())
                .date(request.date())
                .nextDate(request.nextDate())
                .status("valid")
                .pet(pet)
                .build();

        // Mise à jour automatique du statut en fonction des dates
        vaccine.updateStatus();

        Vaccine savedVaccine = vaccineRepository.save(vaccine);
        log.info("✅ Vaccin enregistré: {} pour {}", savedVaccine.getName(), pet.getName());

        return mapToResponse(savedVaccine);
    }

    @Override
    public List<VaccineResponse> getVaccinesByPet(UUID petId, UUID userId) {
        log.debug("🔍 Récupération des vaccins pour l'animal: {}", petId);

        // 🛡️ SÉCURITÉ : Vérifier que l'animal appartient à l'utilisateur
        Pet pet = petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new AccessDeniedException("Cet animal ne vous appartient pas"));

        List<Vaccine> vaccines = vaccineRepository.findByPetIdOrderByNextDateAsc(petId);

        return vaccines.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VaccineResponse getVaccineById(UUID vaccineId, UUID userId) {
        log.debug("🔍 Récupération du vaccin: {}", vaccineId);

        Vaccine vaccine = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccin introuvable"));

        // 🛡️ SÉCURITÉ : Vérifier que l'animal appartient à l'utilisateur
        if (!vaccine.getPet().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Accès non autorisé à ce vaccin");
        }

        return mapToResponse(vaccine);
    }

    @Override
    public List<VaccineResponse> getExpiredVaccines(UUID petId, UUID userId) {
        log.debug("🔍 Récupération des vaccins expirés pour l'animal: {}", petId);

        // 🛡️ SÉCURITÉ : Vérifier que l'animal appartient à l'utilisateur
        petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new AccessDeniedException("Cet animal ne vous appartient pas"));

        List<Vaccine> vaccines = vaccineRepository.findExpiredByPetId(petId, LocalDate.now());

        return vaccines.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VaccineResponse> getUpcomingVaccines(UUID petId, UUID userId) {
        log.debug("🔍 Récupération des rappels à venir pour l'animal: {}", petId);

        // 🛡️ SÉCURITÉ : Vérifier que l'animal appartient à l'utilisateur
        petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new AccessDeniedException("Cet animal ne vous appartient pas"));

        LocalDate today = LocalDate.now();
        LocalDate in30Days = today.plusDays(30);

        List<Vaccine> vaccines = vaccineRepository.findUpcomingByPetId(petId, today, in30Days);

        return vaccines.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteVaccine(UUID vaccineId, UUID userId) {
        log.debug("🗑️ Suppression du vaccin: {}", vaccineId);

        Vaccine vaccine = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccin introuvable"));

        // 🛡️ SÉCURITÉ : Vérifier que l'animal appartient à l'utilisateur
        if (!vaccine.getPet().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Accès non autorisé à ce vaccin");
        }

        vaccineRepository.delete(vaccine);
        log.info("✅ Vaccin supprimé: {}", vaccineId);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPER
    // ═══════════════════════════════════════════════════════════════════════════

    private VaccineResponse mapToResponse(Vaccine vaccine) {
        return new VaccineResponse(
                vaccine.getId(),
                vaccine.getName(),
                vaccine.getDate(),
                vaccine.getNextDate(),
                vaccine.getStatus(),
                vaccine.getPet().getId(),
                vaccine.getPet().getName()
        );
    }
}
