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
@Table(name = "error_log",
        indexes = {
                @Index(name = "idx_error_type", columnList = "error_type"),
                @Index(name = "idx_error_sensor", columnList = "sensor_id"),
                @Index(name = "idx_error_created", columnList = "created_at")
        })
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_type", nullable = false, length = 50)
    private ErrorType errorType;

    @Column(name = "sensor_id", length = 100)
    private String sensorId;

    @Column(name = "controller_id", length = 100)
    private String controllerId;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum ErrorType {
        // Validation errors
        VALIDATION_ERROR,
        OUT_OF_RANGE,
        UNKNOWN_SENSOR,
        UNKNOWN_CONTROLLER,

        // Kafka errors
        KAFKA_CONSUMER_ERROR,
        KAFKA_PRODUCER_ERROR,
        KAFKA_DESERIALIZATION_ERROR,

        // Database errors
        DATABASE_ERROR,
        CONSTRAINT_VIOLATION,

        // Security errors
        AUTHENTICATION_ERROR,
        AUTHORIZATION_ERROR,
        TOKEN_EXPIRED,
        TOKEN_INVALID,

        // WebSocket errors
        WEBSOCKET_ERROR,

        // System errors
        INTERNAL_ERROR,
        EXTERNAL_SERVICE_ERROR
    }
}