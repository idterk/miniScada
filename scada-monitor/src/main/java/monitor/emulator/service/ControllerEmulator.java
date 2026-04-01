package monitor.emulator.service;

import lombok.extern.slf4j.Slf4j;
import monitor.dto.SensorCommandDTO;
import monitor.dto.SensorDataDTO;
import monitor.entity.SensorMetadata;
import monitor.emulator.config.EmulatorProperties;
import monitor.emulator.producer.TelemetryProducer;
import monitor.repository.SensorMetadataRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@ConditionalOnProperty(name = "emulator.enabled", havingValue = "true")
@EnableScheduling
public class ControllerEmulator {

    private final EmulatorProperties properties;
    private final TelemetryProducer telemetryProducer;
    private final SensorMetadataRepository sensorMetadataRepository;
    private final Random random = new Random();
    private final Map<String, SensorState> sensors = new ConcurrentHashMap<>();

    public ControllerEmulator(EmulatorProperties properties,
                              TelemetryProducer telemetryProducer,
                              SensorMetadataRepository sensorMetadataRepository) {
        this.properties = properties;
        this.telemetryProducer = telemetryProducer;
        this.sensorMetadataRepository = sensorMetadataRepository;
    }

    @PostConstruct
    public void initialize() {
        loadSensorsFromDatabase();
    }

    /**
     * Загружает датчики из базы данных при старте
     */
    private void loadSensorsFromDatabase() {
        var enabledSensors = sensorMetadataRepository.findByEnabledTrue();

        for (SensorMetadata metadata : enabledSensors) {
            if (!sensors.containsKey(metadata.getId())) {
                SensorState sensor = createSensorStateFromMetadata(metadata);
                sensors.put(metadata.getId(), sensor);

                log.info("✅ Датчик загружен из БД: {} ({}°C/{}%, тип: {})",
                        metadata.getId(),
                        sensor.getCurrentTemperature(),
                        sensor.getCurrentFillLevel(),
                        metadata.getType());
            }
        }

        log.info("Всего датчиков загружено из БД: {}", sensors.size());
    }

    /**
     * Периодическая проверка новых датчиков в БД (каждые 30 секунд)
     */
    @Scheduled(fixedDelay = 30000)
    public void refreshSensorsFromDatabase() {
        var enabledSensors = sensorMetadataRepository.findByEnabledTrue();
        int added = 0;

        for (SensorMetadata metadata : enabledSensors) {
            if (!sensors.containsKey(metadata.getId())) {
                SensorState sensor = createSensorStateFromMetadata(metadata);
                sensors.put(metadata.getId(), sensor);
                added++;
                log.info("🆕 Автоматически добавлен новый датчик из БД: {}", metadata.getId());
            }
        }

        if (added > 0) {
            log.info("Всего датчиков в эмуляции теперь: {}", sensors.size());
        }
    }

    /**
     * Создаёт SensorState из метаданных
     */
    private SensorState createSensorStateFromMetadata(SensorMetadata metadata) {
        EmulatorProperties.SensorConfig config = new EmulatorProperties.SensorConfig();
        config.setId(metadata.getId());
        config.setBaseTemp(metadata.getDefaultValue() != null ? metadata.getDefaultValue() : 22.0);
        config.setBaseFill(metadata.getDefaultValue() != null ? metadata.getDefaultValue().intValue() : 50);
        config.setType(metadata.getType().name().toLowerCase());
        config.setMinTemp(metadata.getMinValue() != null ? metadata.getMinValue() : -100);
        config.setMaxTemp(metadata.getMaxValue() != null ? metadata.getMaxValue() : 100);
        config.setEnabled(metadata.isEnabled());

        return new SensorState(config);
    }

    public void processCommand(SensorCommandDTO command) {
        // Проверяем существование датчика в БД
        SensorMetadata metadata = sensorMetadataRepository.findById(command.getSensorId()).orElse(null);

        if (metadata == null) {
            log.warn("Команда для неизвестного датчика: {}", command.getSensorId());
            return;
        }

        if (!metadata.isEnabled()) {
            log.warn("Датчик {} отключен в БД", command.getSensorId());
            return;
        }

        // Получаем или создаём эмулированный датчик
        SensorState sensor = sensors.computeIfAbsent(
                command.getSensorId(),
                id -> createSensorStateFromMetadata(metadata)
        );

        // Имитация времени обработки
        try {
            Thread.sleep(properties.getProcessingTimeMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Применяем команду
        sensor.applyCommand(command.getCommand(), command.getValue());

        // Создаём телеметрию
        SensorDataDTO telemetry = createTelemetry(sensor);

        // Добавляем шум если нужно
        if (properties.isAddNoise()) {
            telemetry = addNoise(telemetry);
        }

        // Отправляем в Kafka
        telemetryProducer.sendTelemetry(telemetry);

        log.debug("📤 Команда {} для {}: {} = {}/{}",
                command.getCommand(),
                command.getSensorId(),
                command.getValue(),
                telemetry.getTemperature(),
                telemetry.getFillLevel());
    }

    public void generateBackgroundTelemetry() {
        for (SensorState sensor : sensors.values()) {
            sensor.simulateDrift();

            SensorDataDTO telemetry = createTelemetry(sensor);

            if (properties.isAddNoise()) {
                telemetry = addNoise(telemetry);
            }

            telemetryProducer.sendTelemetry(telemetry);
        }
    }

    /**
     * Принудительная генерация телеметрии для конкретного датчика
     */
    public void generateSingleTelemetry(String sensorId) {
        SensorState sensor = sensors.get(sensorId);
        if (sensor != null) {
            sensor.simulateDrift();
            SensorDataDTO telemetry = createTelemetry(sensor);
            telemetryProducer.sendTelemetry(telemetry);
            log.debug("📊 Сгенерирована телеметрия для {}: {}/{}",
                    sensorId, telemetry.getTemperature(), telemetry.getFillLevel());
        }
    }

    private SensorDataDTO createTelemetry(SensorState sensor) {
        SensorDataDTO dto = new SensorDataDTO();
        dto.setSensorId(sensor.getSensorId());
        dto.setTemperature(sensor.getCurrentTemperature());
        dto.setFillLevel(sensor.getCurrentFillLevel());
        dto.setTimestamp(System.currentTimeMillis() / 1000);
        return dto;
    }

    private SensorDataDTO addNoise(SensorDataDTO data) {
        double noise = (random.nextDouble() - 0.5) * properties.getNoiseRange();

        SensorDataDTO noisy = new SensorDataDTO();
        noisy.setSensorId(data.getSensorId());
        noisy.setTimestamp(data.getTimestamp());

        if (data.getTemperature() != null) {
            noisy.setTemperature(data.getTemperature() + noise);
        }
        if (data.getFillLevel() != null) {
            int newFill = (int)(data.getFillLevel() + noise);
            noisy.setFillLevel(Math.max(0, Math.min(100, newFill)));
        }

        return noisy;
    }

    public Map<String, Object> getStats() {
        return Map.of(
                "totalSensors", sensors.size(),
                "mode", properties.getMode(),
                "enabled", properties.isEnabled()
        );
    }
}