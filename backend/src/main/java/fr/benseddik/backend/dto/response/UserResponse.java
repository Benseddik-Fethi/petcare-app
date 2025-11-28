package fr.benseddik.backend.dto.response;

import fr.benseddik.backend.domain.Role;
import fr.benseddik.backend.domain.User;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de réponse utilisateur (sans données sensibles).
 *
 * ✅ Compatible avec le frontend React (camelCase).
 * Le frontend attend :
 * {
 *   "id": "uuid",
 *   "email": "user@example.com",
 *   "firstName": "John" | null,
 *   "lastName": "Doe" | null,
 *   "role": "OWNER" | "VET" | "ADMIN",
 *   "createdAt": "2025-01-...",
 *   "updatedAt": "2025-01-..."
 * }
 */
public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String avatar,
        Role role,
        Boolean emailVerified,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * Construit un UserResponse à partir d'une entité User.
     */
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatar(),
                user.getRole(),
                user.getEmailVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}