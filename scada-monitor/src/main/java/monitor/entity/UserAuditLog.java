package monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_audit_log",
        indexes = {
                @Index(name = "idx_audit_user", columnList = "user_id"),
                @Index(name = "idx_audit_action", columnList = "action"),
                @Index(name = "idx_audit_created", columnList = "created_at")
        })
public class UserAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(length = 50)
    private String username;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_CREATE_SENSOR = "CREATE_SENSOR";
    public static final String ACTION_UPDATE_SENSOR = "UPDATE_SENSOR";
    public static final String ACTION_DELETE_SENSOR = "DELETE_SENSOR";
    public static final String ACTION_UPDATE_VALUE = "UPDATE_VALUE";
    public static final String ACTION_CREATE_CONTROLLER = "CREATE_CONTROLLER";
    public static final String ACTION_UPDATE_CONTROLLER = "UPDATE_CONTROLLER";
    public static final String ACTION_DELETE_CONTROLLER = "DELETE_CONTROLLER";
    public static final String ACTION_UPDATE_USER = "UPDATE_USER";
    public static final String ACTION_EMULATE = "EMULATE";
}