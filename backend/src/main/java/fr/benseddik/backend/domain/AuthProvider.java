package fr.benseddik.backend.domain;

/**
 * Fournisseurs d'authentification supportés.
 * Correspond à l'enum AuthProvider de Prisma.
 */
public enum AuthProvider {
    EMAIL,      // Authentification classique email/password
    GOOGLE,     // OAuth2 Google
    FACEBOOK    // OAuth2 Facebook
}