package fr.benseddik.backend.controller;

import fr.benseddik.backend.domain.User;
import fr.benseddik.backend.dto.request.CreatePetRequest;
import fr.benseddik.backend.dto.request.UpdatePetRequest;
import fr.benseddik.backend.dto.response.PetResponse;
import fr.benseddik.backend.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des animaux de compagnie.
 *
 * 🛡️ SÉCURITÉ :
 * - Nécessite une authentification
 * - L'ID utilisateur est extrait du principal
 * - Toutes les opérations sont filtrées par propriétaire
 */
@Slf4j
@RestController
@RequestMapping("/v1/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    /**
     * Crée un nouvel animal pour l'utilisateur connecté.
     */
    @PostMapping
    public ResponseEntity<PetResponse> createPet(
            @Valid @RequestBody CreatePetRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("🐾 POST /v1/pets - Création d'un animal pour: {}", user.getEmail());

        PetResponse response = petService.createPet(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupère tous les animaux de l'utilisateur connecté.
     */
    @GetMapping
    public ResponseEntity<List<PetResponse>> getAllPets(@AuthenticationPrincipal User user) {
        log.debug("🔍 GET /v1/pets - Récupération des animaux de: {}", user.getEmail());

        List<PetResponse> pets = petService.getAllPets(user.getId());
        return ResponseEntity.ok(pets);
    }

    /**
     * Récupère un animal par ID.
     */
    @GetMapping("/{petId}")
    public ResponseEntity<PetResponse> getPetById(
            @PathVariable UUID petId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("🔍 GET /v1/pets/{} - Récupération d'un animal", petId);

        PetResponse pet = petService.getPetById(petId, user.getId());
        return ResponseEntity.ok(pet);
    }

    /**
     * Met à jour un animal.
     */
    @PatchMapping("/{petId}")
    public ResponseEntity<PetResponse> updatePet(
            @PathVariable UUID petId,
            @Valid @RequestBody UpdatePetRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("✏️ PATCH /v1/pets/{} - Mise à jour d'un animal", petId);

        PetResponse pet = petService.updatePet(petId, request, user.getId());
        return ResponseEntity.ok(pet);
    }

    /**
     * Supprime un animal.
     */
    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deletePet(
            @PathVariable UUID petId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("🗑️ DELETE /v1/pets/{} - Suppression d'un animal", petId);

        petService.deletePet(petId, user.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Recherche des animaux par nom ou race.
     */
    @GetMapping("/search")
    public ResponseEntity<List<PetResponse>> searchPets(
            @RequestParam String query,
            @AuthenticationPrincipal User user
    ) {
        log.debug("🔎 GET /v1/pets/search?query={}", query);

        List<PetResponse> pets = petService.searchPets(query, user.getId());
        return ResponseEntity.ok(pets);
    }
}
