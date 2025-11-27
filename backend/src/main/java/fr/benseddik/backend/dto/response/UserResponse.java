package fr.benseddik.backend.dto.response;

import fr.benseddik.backend.domain.Role;
import fr.benseddik.backend.domain.User;

import java.util.UUID;

/**
 * DTO de réponse utilisateur (sans données sensibles).
 */
public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String avatar,
        Role role,
        Boolean emailVerified
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
                user.getEmailVerified()
        );
    }
}