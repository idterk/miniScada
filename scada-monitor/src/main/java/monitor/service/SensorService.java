package monitor.service;

import monitor.dto.SensorDataDTO;
import monitor.entity.ErrorLog;
import monitor.entity.SensorMetadata;
import monitor.entity.SensorState;
import monitor.repository.SensorMetadataRepository;
import monitor.repository.SensorStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorMetadataRepository metadataRepository;
    private final SensorStateRepository stateRepository;
    private final ErrorLogService errorLogService;

    /**
     * Получить все ID датчиков
     */
    public List<String> getAllSensorIds() {
        return metadataRepository.findAll().stream()
                .map(SensorMetadata::getId)
                .collect(Collectors.toList());
    }

    /**
     * Получить состояние датчика
     */
    @Transactional(readOnly = true)
    public SensorDataDTO getSensor(String sensorId) {
        var currentState = stateRepository.findBySensorIdAndIsCurrentTrue(sensorId);

        if (currentState.isEmpty()) {
            log.debug("Датчик {} не имеет текущего состояния", sensorId);
            return null;
        }

        SensorState state = currentState.get();
        SensorDataDTO dto = new SensorDataDTO();
        dto.setSensorId(state.getSensorId());
        dto.setTemperature(state.getValue());
        dto.setFillLevel(state.getFillLevel());
        dto.setTimestamp(state.getTimestamp().atZone(ZoneId.systemDefault()).toEpochSecond());

        return dto;
    }

    /**
     * Добавить новый датчик
     */
    @Transactional
    public SensorDataDTO addNewSensor() {
        // Найти максимальный номер датчика
        List<String> allIds = getAllSensorIds();
        int maxNum = 1;
        for (String id : allIds) {
            if (id.startsWith("sensor")) {
                try {
                    int num = Integer.parseInt(id.substring(6));
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException ignored) {}
            }
        }

        String newSensorId = "sensor" + (maxNum + 1);

        // Создаём метаданные для нового датчика
        SensorMetadata metadata = SensorMetadata.builder()
                .id(newSensorId)
                .name("Sensor " + (maxNum + 1))
                .type(SensorMetadata.SensorType.TEMPERATURE)
                .unit("°C")
                .minValue(-100.0)
                .maxValue(100.0)
                .defaultValue(20.0)
                .enabled(true)
                .build();

        metadataRepository.save(metadata);

        log.info("✅ Добавлен новый датчик: {}", newSensorId);

        SensorDataDTO dto = new SensorDataDTO();
        dto.setSensorId(newSensorId);
        dto.setTemperature(20.0);
        dto.setFillLevel(50);
        dto.setTimestamp(System.currentTimeMillis() / 1000);

        return dto;
    }

    /**
     * Обновить или создать состояние датчика
     */
    @Transactional
    public void updateOrCreateSensor(SensorDataDTO dto) {
        String sensorId = dto.getSensorId();

        // Проверяем существование метаданных
        SensorMetadata metadata = metadataRepository.findById(sensorId).orElse(null);
        if (metadata == null) {
            errorLogService.logError(
                    ErrorLog.ErrorType.UNKNOWN_SENSOR,
                    sensorId,
                    "Попытка обновить неизвестный датчик"
            );
            return;
        }

        if (!metadata.isEnabled()) {
            log.debug("Датчик {} отключен, данные игнорируются", sensorId);
            return;
        }

        Double value = dto.getTemperature() != null ? dto.getTemperature() :
                (dto.getFillLevel() != null ? dto.getFillLevel().doubleValue() : null);

        if (value != null) {
            if ((metadata.getMinValue() != null && value < metadata.getMinValue()) ||
                    (metadata.getMaxValue() != null && value > metadata.getMaxValue())) {
                errorLogService.logError(
                        ErrorLog.ErrorType.OUT_OF_RANGE,
                        sensorId,
                        String.format("Значение %.2f вне диапазона [%.2f, %.2f]",
                                value, metadata.getMinValue(), metadata.getMaxValue())
                );
                return;
            }
        }

        LocalDateTime timestamp = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(dto.getTimestamp()),
                ZoneId.systemDefault()
        );

        // Снимаем флаг is_current с предыдущего состояния
        stateRepository.unsetCurrentFlag(sensorId);

        // Создаём новое текущее состояние
        SensorState newState = SensorState.builder()
                .sensorId(sensorId)
                .value(dto.getTemperature())
                .fillLevel(dto.getFillLevel())
                .timestamp(timestamp)
                .isCurrent(true)
                .quality(100)
                .build();

        // Рассчитываем уровень тревоги
        if (value != null) {
            newState.setAlarmLevel(metadata.calculateAlarmLevel(value));
        }

        stateRepository.save(newState);

        // Сохраняем историческую запись (опционально)
        SensorState historyState = newState.toHistory();
        stateRepository.save(historyState);

        log.debug("✅ Состояние датчика {} обновлено: value={}, fill={}",
                sensorId, dto.getTemperature(), dto.getFillLevel());
    }
}