package monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sensor_metadata",
        indexes = {
                @Index(name = "idx_sensor_controller", columnList = "controller_id"),
                @Index(name = "idx_sensor_type", columnList = "type"),
                @Index(name = "idx_sensor_enabled", columnList = "enabled")
        })
public class SensorMetadata {

    @Id
    @Column(length = 100)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "controller_id")
    private ControllerMetadata controller;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorType type;

    @Column(length = 20)
    private String unit;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "default_value")
    private Double defaultValue;

    @Column(name = "warning_low")
    private Double warningLow;

    @Column(name = "warning_high")
    private Double warningHigh;

    @Column(name = "alarm_low")
    private Double alarmLow;

    @Column(name = "alarm_high")
    private Double alarmHigh;

    @Column(name = "polling_interval")
    private Integer pollingInterval = 1000;

    @Column
    private Double deadband = 0.0;

    @Column(nullable = false)
    private boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SensorType {
        TEMPERATURE,    // °C, °F
        PRESSURE,       // bar, psi, Pa
        LEVEL,          // %, m
        FLOW,           // m³/h, l/min
        VOLTAGE,        // V
        CURRENT,        // A
        SPEED,          // RPM, m/s
        HUMIDITY,       // %
        POSITION,       // %, mm
        STATUS          // ON/OFF, OPEN/CLOSED
    }

    public enum AlarmLevel {
        NORMAL, WARNING, CRITICAL
    }

    public AlarmLevel calculateAlarmLevel(Double value) {
        if (value == null) return AlarmLevel.NORMAL;

        if ((alarmLow != null && value <= alarmLow) ||
                (alarmHigh != null && value >= alarmHigh)) {
            return AlarmLevel.CRITICAL;
        }

        if ((warningLow != null && value <= warningLow) ||
                (warningHigh != null && value >= warningHigh)) {
            return AlarmLevel.WARNING;
        }

        return AlarmLevel.NORMAL;
    }
}