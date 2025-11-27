package fr.benseddik.backend.domain;

/**
 * Rôles utilisateur dans l'application.
 * Correspond à l'enum Role de Prisma.
 */
public enum Role {
    OWNER,  // Propriétaire d'animaux (par défaut)
    ADMIN,  // Administrateur
    VET     // Vétérinaire
}
