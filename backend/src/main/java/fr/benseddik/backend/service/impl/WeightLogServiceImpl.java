package fr.benseddik.backend.service.impl;

import fr.benseddik.backend.domain.Pet;
import fr.benseddik.backend.domain.WeightLog;
import fr.benseddik.backend.dto.request.CreateWeightLogRequest;
import fr.benseddik.backend.dto.response.WeightLogResponse;
import fr.benseddik.backend.exception.ResourceNotFoundException;
import fr.benseddik.backend.repository.PetRepository;
import fr.benseddik.backend.repository.WeightLogRepository;
import fr.benseddik.backend.service.WeightLogService;
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
 * Implémentation du service de gestion des pesées.
 *
 * 🛡️ SÉCURITÉ :
 * - Vérification que l'animal appartient à l'utilisateur
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeightLogServiceImpl implements WeightLogService {

    private final WeightLogRepository weightLogRepository;
    private final PetRepository petRepository;

    @Override
    @Transactional
    public WeightLogResponse createWeightLog(CreateWeightLogRequest request, UUID userId) {
        log.debug("⚖️ Enregistrement d'une pesée pour l'animal: {}", request.petId());

        // 🛡️ SÉCURITÉ : Vérifier que l'animal appartient à l'utilisateur
        Pet pet = petRepository.findByIdAndOwnerId(request.petId(), userId)
                .orElseThrow(() -> new AccessDeniedException("Cet animal ne vous appartient pas"));

        WeightLog weightLog = WeightLog.builder()
                .weight(request.weight())
                .date(request.date())
                .note(request.note())
                .pet(pet)
                .build();

        WeightLog savedWeightLog = weightLogRepository.save(weightLog);
        log.info("✅ Pesée enregistrée: {} kg pour {} le {}",
                savedWeightLog.getWeight(), pet.getName(), savedWeightLog.getDate());

        return mapToResponse(savedWeightLog);
    }

    @Override
    public List<WeightLogResponse> getWeightLogsByPet(UUID petId, UUID userId) {
        log.debug("🔍 Récupération de l'historique des pesées pour l'animal: {}", petId);

        // 🛡️ SÉCURITÉ : Vérifier que l'animal appartient à l'utilisateur
        Pet pet = petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new AccessDeniedException("Cet animal ne vous appartient pas"));

        List<WeightLog> weightLogs = weightLogRepository.findByPetIdOrderByDateDesc(petId);

        return weightLogs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WeightLogResponse getLatestWeightLog(UUID petId, UUID userId) {
        log.debug("🔍 Récupération de la dernière pesée pour l'animal: {}", petId);

        // 🛡️ SÉCURITÉ : Vérifier que l'animal appartient à l'utilisateur
        Pet pet = petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new AccessDeniedException("Cet animal ne vous appartient pas"));

        WeightLog weightLog = weightLogRepository.findLatestByPetId(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune pesée enregistrée pour cet animal"));

        return mapToResponse(weightLog);
    }

    @Override
    public List<WeightLogResponse> getWeightLogsBetween(UUID petId, UUID userId, LocalDate start, LocalDate end) {
        log.debug("🔍 Récupération des pesées entre {} et {}", start, end);

        // 🛡️ SÉCURITÉ : Vérifier que l'animal appartient à l'utilisateur
        petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new AccessDeniedException("Cet animal ne vous appartient pas"));

        List<WeightLog> weightLogs = weightLogRepository.findByPetIdAndDateBetween(petId, start, end);

        return weightLogs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteWeightLog(UUID weightLogId, UUID userId) {
        log.debug("🗑️ Suppression de la pesée: {}", weightLogId);

        WeightLog weightLog = weightLogRepository.findById(weightLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Pesée introuvable"));

        // 🛡️ SÉCURITÉ : Vérifier que l'animal appartient à l'utilisateur
        if (!weightLog.getPet().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Accès non autorisé à cette pesée");
        }

        weightLogRepository.delete(weightLog);
        log.info("✅ Pesée supprimée: {}", weightLogId);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPER
    // ═══════════════════════════════════════════════════════════════════════════

    private WeightLogResponse mapToResponse(WeightLog weightLog) {
        return new WeightLogResponse(
                weightLog.getId(),
                weightLog.getWeight(),
                weightLog.getDate(),
                weightLog.getNote(),
                weightLog.getPet().getId(),
                weightLog.getPet().getName()
        );
    }
}
