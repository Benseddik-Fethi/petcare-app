package fr.benseddik.backend.service.impl;

import fr.benseddik.backend.domain.Pet;
import fr.benseddik.backend.domain.User;
import fr.benseddik.backend.dto.request.CreatePetRequest;
import fr.benseddik.backend.dto.request.UpdatePetRequest;
import fr.benseddik.backend.dto.response.PetResponse;
import fr.benseddik.backend.exception.ResourceNotFoundException;
import fr.benseddik.backend.repository.PetRepository;
import fr.benseddik.backend.repository.UserRepository;
import fr.benseddik.backend.service.PetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des animaux de compagnie.
 *
 * 🛡️ SÉCURITÉ :
 * - Vérification stricte de la propriété avant toute opération
 * - Filtrage automatique par propriétaire
 * - Validation des entrées via DTOs
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PetResponse createPet(CreatePetRequest request, UUID userId) {
        log.debug("🐾 Création d'un animal pour l'utilisateur: {}", userId);

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        Pet pet = Pet.builder()
                .name(request.name())
                .species(request.species())
                .breed(request.breed())
                .birthDate(request.birthDate())
                .gender(request.gender())
                .microchip(request.microchip())
                .color(request.color())
                .avatar(request.avatar())
                .owner(owner)
                .build();

        Pet savedPet = petRepository.save(pet);
        log.info("✅ Animal créé avec succès: {} (ID: {})", savedPet.getName(), savedPet.getId());

        return mapToResponse(savedPet);
    }

    @Override
    public List<PetResponse> getAllPets(UUID userId) {
        log.debug("🔍 Récupération des animaux de l'utilisateur: {}", userId);

        List<Pet> pets = petRepository.findByOwnerId(userId);

        return pets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PetResponse getPetById(UUID petId, UUID userId) {
        log.debug("🔍 Récupération de l'animal: {} pour l'utilisateur: {}", petId, userId);

        Pet pet = petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal introuvable ou accès non autorisé"));

        return mapToResponse(pet);
    }

    @Override
    @Transactional
    public PetResponse updatePet(UUID petId, UpdatePetRequest request, UUID userId) {
        log.debug("✏️ Mise à jour de l'animal: {} pour l'utilisateur: {}", petId, userId);

        // 🛡️ SÉCURITÉ : Vérification de propriété
        Pet pet = petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new AccessDeniedException("Accès non autorisé à cet animal"));

        // Mise à jour partielle (seuls les champs non-null sont modifiés)
        if (request.name() != null) pet.setName(request.name());
        if (request.species() != null) pet.setSpecies(request.species());
        if (request.breed() != null) pet.setBreed(request.breed());
        if (request.birthDate() != null) pet.setBirthDate(request.birthDate());
        if (request.gender() != null) pet.setGender(request.gender());
        if (request.microchip() != null) pet.setMicrochip(request.microchip());
        if (request.color() != null) pet.setColor(request.color());
        if (request.avatar() != null) pet.setAvatar(request.avatar());

        Pet updatedPet = petRepository.save(pet);
        log.info("✅ Animal mis à jour: {} (ID: {})", updatedPet.getName(), updatedPet.getId());

        return mapToResponse(updatedPet);
    }

    @Override
    @Transactional
    public void deletePet(UUID petId, UUID userId) {
        log.debug("🗑️ Suppression de l'animal: {} pour l'utilisateur: {}", petId, userId);

        // 🛡️ SÉCURITÉ : Vérification de propriété
        Pet pet = petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new AccessDeniedException("Accès non autorisé à cet animal"));

        petRepository.delete(pet);
        log.info("✅ Animal supprimé: {} (ID: {})", pet.getName(), pet.getId());
    }

    @Override
    public List<PetResponse> searchPets(String query, UUID userId) {
        log.debug("🔎 Recherche d'animaux avec le terme: '{}' pour l'utilisateur: {}", query, userId);

        List<Pet> pets = petRepository.searchByNameOrBreed(query, userId);

        return pets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPER
    // ═══════════════════════════════════════════════════════════════════════════

    private PetResponse mapToResponse(Pet pet) {
        return new PetResponse(
                pet.getId(),
                pet.getName(),
                pet.getSpecies(),
                pet.getBreed(),
                pet.getBirthDate(),
                pet.getAgeInYears(),
                pet.getGender(),
                pet.getMicrochip(),
                pet.getColor(),
                pet.getAvatar(),
                pet.getCurrentWeight(),
                pet.getCreatedAt(),
                pet.getUpdatedAt()
        );
    }
}
