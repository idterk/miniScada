package monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "controller_metadata")
public class ControllerMetadata {

    @Id
    @Column(length = 100)
    private String id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String location;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column
    private Integer port;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ControllerStatus status = ControllerStatus.ACTIVE;

    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;

    @OneToMany(mappedBy = "controller", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SensorMetadata> sensors = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ControllerStatus {
        ACTIVE,      // Online and working
        MAINTENANCE, // Under maintenance
        OFFLINE,     // Not responding
        ERROR        // Error state
    }
}