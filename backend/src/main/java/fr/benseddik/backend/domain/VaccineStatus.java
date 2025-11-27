package fr.benseddik.backend.domain;

/**
 * Statuts possibles pour un vaccin.
 */
public enum VaccineStatus {
    VALID,      // Vaccin à jour
    UPCOMING,   // Rappel à prévoir (dans les 30 jours)
    EXPIRED     // Vaccin expiré
}