package monitor.repository;

import monitor.entity.SensorMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensorMetadataRepository extends JpaRepository<SensorMetadata, String> {

    List<SensorMetadata> findByEnabledTrue();

    List<SensorMetadata> findByControllerId(String controllerId);

    List<SensorMetadata> findByType(SensorMetadata.SensorType type);

    @Query("SELECT s FROM SensorMetadata s WHERE s.enabled = true")
    List<SensorMetadata> findAllEnabled();

    Optional<SensorMetadata> findByIdAndEnabledTrue(String id);
}