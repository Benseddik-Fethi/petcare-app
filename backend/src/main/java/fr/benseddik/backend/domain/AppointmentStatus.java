package fr.benseddik.backend.domain;

/**
 * Statuts possibles pour un rendez-vous.
 */
public enum AppointmentStatus {
    SCHEDULED,   // Planifié
    CONFIRMED,   // Confirmé par la clinique
    COMPLETED,   // Terminé
    CANCELLED,   // Annulé
    NO_SHOW      // Patient non présenté
}