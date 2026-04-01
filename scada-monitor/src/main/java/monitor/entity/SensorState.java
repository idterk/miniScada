package monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sensor_state",
        indexes = {
                @Index(name = "idx_state_sensor_id", columnList = "sensor_id"),
                @Index(name = "idx_state_timestamp", columnList = "timestamp"),
                @Index(name = "idx_state_current", columnList = "is_current")
        })
public class SensorState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sensor_id", nullable = false, length = 100)
    private String sensorId;

    @Column
    private Double value;

    @Column(name = "fill_level")
    private Integer fillLevel;

    @Column
    private Integer quality = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "alarm_level")
    private SensorMetadata.AlarmLevel alarmLevel = SensorMetadata.AlarmLevel.NORMAL;

    @Column(name = "status_message", length = 500)
    private String statusMessage;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "is_current")
    private boolean isCurrent = false;

    @Version
    private Long version;

    public static SensorState currentState(String sensorId, Double value, Integer fillLevel, LocalDateTime timestamp) {
        return SensorState.builder()
                .sensorId(sensorId)
                .value(value)
                .fillLevel(fillLevel)
                .timestamp(timestamp)
                .isCurrent(true)
                .quality(100)
                .build();
    }

    public SensorState toHistory() {
        return SensorState.builder()
                .sensorId(this.sensorId)
                .value(this.value)
                .fillLevel(this.fillLevel)
                .quality(this.quality)
                .alarmLevel(this.alarmLevel)
                .statusMessage(this.statusMessage)
                .timestamp(this.timestamp)
                .isCurrent(false)
                .build();
    }
}