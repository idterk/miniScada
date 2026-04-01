package monitor.service;

import monitor.entity.SensorMetadata;
import monitor.repository.SensorMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorMetadataService {

    private final SensorMetadataRepository metadataRepository;

    /**
     * Получить все метаданные датчиков
     */
    public List<SensorMetadata> getAllSensors() {
        return metadataRepository.findAll();
    }

    /**
     * Получить метаданные датчика по ID
     */
    public Optional<SensorMetadata> getSensorMetadata(String sensorId) {
        return metadataRepository.findById(sensorId);
    }

    /**
     * Получить все включённые датчики
     */
    public List<SensorMetadata> getEnabledSensors() {
        return metadataRepository.findByEnabledTrue();
    }

    /**
     * Создать новый датчик
     */
    @Transactional
    public SensorMetadata createSensor(SensorMetadata metadata) {
        // Устанавливаем значения по умолчанию если не заданы
        if (metadata.getDefaultValue() == null) {
            metadata.setDefaultValue(20.0);
        }
        if (metadata.getMinValue() == null) {
            metadata.setMinValue(-100.0);
        }
        if (metadata.getMaxValue() == null) {
            metadata.setMaxValue(100.0);
        }
        if (metadata.getPollingInterval() == null) {
            metadata.setPollingInterval(1000);
        }

        SensorMetadata saved = metadataRepository.save(metadata);
        log.info("✅ Создан новый датчик: {} ({})", saved.getId(), saved.getType());
        return saved;
    }

    /**
     * Обновить метаданные датчика
     */
    @Transactional
    public SensorMetadata updateSensor(String sensorId, SensorMetadata metadata) {
        metadata.setId(sensorId);
        SensorMetadata updated = metadataRepository.save(metadata);
        log.info("🔄 Обновлён датчик: {}", sensorId);
        return updated;
    }

    /**
     * Удалить датчик
     */
    @Transactional
    public void deleteSensor(String sensorId) {
        metadataRepository.deleteById(sensorId);
        log.info("🗑️ Удалён датчик: {}", sensorId);
    }

    /**
     * Проверить существование датчика
     */
    public boolean exists(String sensorId) {
        return metadataRepository.existsById(sensorId);
    }
}