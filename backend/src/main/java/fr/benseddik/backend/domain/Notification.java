package fr.benseddik.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entité Notification - Notifications et rappels utilisateur.
 *
 * Gère les alertes pour :
 * - Rappels de vaccins
 * - Rendez-vous à venir
 * - Messages système
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user", columnList = "user_id"),
        @Index(name = "idx_notification_read", columnList = "isRead"),
        @Index(name = "idx_notification_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Type de notification.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    /**
     * Titre de la notification.
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Message de la notification.
     */
    @Column(nullable = false, length = 1000)
    private String message;

    /**
     * Lien vers la ressource concernée (optionnel).
     * Ex: "/pets/{id}/vaccines/{vaccineId}"
     */
    @Column(length = 500)
    private String link;

    /**
     * Indique si la notification a été lue.
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * Date de lecture (null si non lue).
     */
    @Column(name = "read_at")
    private Instant readAt;

    // ═══════════════════════════════════════════════════════════════════════════
    // RELATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Animal concerné (optionnel).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    // ═══════════════════════════════════════════════════════════════════════════
    // TIMESTAMPS
    // ═══════════════════════════════════════════════════════════════════════════

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Marque la notification comme lue.
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = Instant.now();
    }

    /**
     * Types de notifications.
     */
    public enum NotificationType {
        VACCINE_REMINDER,      // Rappel de vaccination
        VACCINE_EXPIRED,       // Vaccin expiré
        APPOINTMENT_REMINDER,  // Rappel de rendez-vous (J-1, H-2)
        APPOINTMENT_CONFIRMED, // Rendez-vous confirmé
        APPOINTMENT_CANCELLED, // Rendez-vous annulé
        WEIGHT_ALERT,          // Alerte variation de poids
        SYSTEM,                // Message système
        WELCOME,               // Bienvenue
        ACCOUNT                // Compte (sécurité, etc.)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FACTORY METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    public static Notification vaccineReminder(User user, Pet pet, String vaccineName, int daysUntilDue) {
        return Notification.builder()
                .user(user)
                .pet(pet)
                .type(NotificationType.VACCINE_REMINDER)
                .title("Rappel de vaccination")
                .message(String.format("Le vaccin %s de %s arrive à échéance dans %d jours.",
                        vaccineName, pet.getName(), daysUntilDue))
                .link("/pets/" + pet.getId() + "/vaccines")
                .build();
    }

    public static Notification vaccineExpired(User user, Pet pet, String vaccineName) {
        return Notification.builder()
                .user(user)
                .pet(pet)
                .type(NotificationType.VACCINE_EXPIRED)
                .title("Vaccin expiré")
                .message(String.format("Le vaccin %s de %s est expiré. Prenez rendez-vous rapidement.",
                        vaccineName, pet.getName()))
                .link("/pets/" + pet.getId() + "/vaccines")
                .build();
    }

    public static Notification appointmentReminder(User user, Pet pet, Appointment appointment) {
        return Notification.builder()
                .user(user)
                .pet(pet)
                .type(NotificationType.APPOINTMENT_REMINDER)
                .title("Rappel de rendez-vous")
                .message(String.format("Rendez-vous demain pour %s : %s",
                        pet.getName(), appointment.getReason()))
                .link("/appointments/" + appointment.getId())
                .build();
    }

    public static Notification welcome(User user) {
        return Notification.builder()
                .user(user)
                .type(NotificationType.WELCOME)
                .title("Bienvenue sur PetCare ! 🐾")
                .message("Commencez par ajouter votre premier animal de compagnie.")
                .link("/pets/new")
                .build();
    }
}