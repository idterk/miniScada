package monitor.repository;

import monitor.entity.SensorState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorStateRepository extends JpaRepository<SensorState, Long> {

    Optional<SensorState> findBySensorIdAndIsCurrentTrue(String sensorId);

    List<SensorState> findBySensorIdAndIsCurrentFalseOrderByTimestampDesc(String sensorId, Pageable pageable);

    @Query("SELECT s FROM SensorState s WHERE s.sensorId = :sensorId AND s.timestamp >= :from ORDER BY s.timestamp DESC")
    List<SensorState> findHistory(@Param("sensorId") String sensorId,
                                  @Param("from") LocalDateTime from,
                                  Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE SensorState s SET s.isCurrent = false WHERE s.sensorId = :sensorId AND s.isCurrent = true")
    void unsetCurrentFlag(@Param("sensorId") String sensorId);
}