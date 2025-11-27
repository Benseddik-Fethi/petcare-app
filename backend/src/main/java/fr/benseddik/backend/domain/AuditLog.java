package fr.benseddik.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entité AuditLog - Journal d'audit des actions sensibles.
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_user", columnList = "user_id"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ═══════════════════════════════════════════════════════════════════════════
    // FACTORY METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    public static AuditLog loginSuccess(User user, String ipAddress, String userAgent) {
        return AuditLog.builder()
                .user(user)
                .action("LOGIN_SUCCESS")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .metadata(Map.of("email", user.getEmail()))
                .build();
    }

    public static AuditLog loginFailed(String email, String ipAddress, String userAgent, String reason) {
        return AuditLog.builder()
                .action("LOGIN_FAILED")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .metadata(Map.of("email", email, "reason", reason))
                .build();
    }

    public static AuditLog logout(User user, String ipAddress) {
        return AuditLog.builder()
                .user(user)
                .action("LOGOUT")
                .ipAddress(ipAddress)
                .metadata(Map.of("email", user.getEmail()))
                .build();
    }

    public static AuditLog accountLocked(User user, String ipAddress) {
        return AuditLog.builder()
                .user(user)
                .action("ACCOUNT_LOCKED")
                .ipAddress(ipAddress)
                .metadata(Map.of(
                        "email", user.getEmail(),
                        "lockedUntil", user.getLockedUntil().toString()
                ))
                .build();
    }

    public static AuditLog passwordChanged(User user, String method) {
        return AuditLog.builder()
                .user(user)
                .action("PASSWORD_CHANGED")
                .metadata(Map.of("email", user.getEmail(), "method", method))
                .build();
    }

    public static AuditLog oauthLogin(User user, AuthProvider provider, String ipAddress, String userAgent) {
        return AuditLog.builder()
                .user(user)
                .action("OAUTH_LOGIN")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .metadata(Map.of("email", user.getEmail(), "provider", provider.name()))
                .build();
    }

    public static AuditLog emailVerified(User user) {
        return AuditLog.builder()
                .user(user)
                .action("EMAIL_VERIFIED")
                .metadata(Map.of("email", user.getEmail()))
                .build();
    }

    public static AuditLog passwordResetRequested(String email, String ipAddress) {
        return AuditLog.builder()
                .action("PASSWORD_RESET_REQUESTED")
                .ipAddress(ipAddress)
                .metadata(Map.of("email", email))
                .build();
    }
}