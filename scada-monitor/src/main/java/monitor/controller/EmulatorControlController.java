package monitor.controller;

import monitor.dto.SensorCommandDTO;
import monitor.emulator.service.ControllerEmulator;
import monitor.entity.SensorMetadata;
import monitor.service.SensorMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/emulator")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENGINEER') or hasRole('ADMIN')")
public class EmulatorControlController {

    private final ControllerEmulator emulator;
    private final SensorMetadataService metadataService;

    /**
     * Ручная симуляция показаний датчика через терминал
     */
    @PostMapping("/simulate/{sensorId}")
    public ResponseEntity<?> simulateSensor(
            @PathVariable String sensorId,
            @RequestParam(required = false) Double temperature,
            @RequestParam(required = false) Integer fillLevel,
            @RequestParam(required = false) Boolean addNoise) {

        log.info("🖥️ Ручная симуляция датчика {}: t={}, fill={}, noise={}",
                sensorId, temperature, fillLevel, addNoise);

        // Проверяем существование датчика
        Optional<SensorMetadata> metadata = metadataService.getSensorMetadata(sensorId);
        if (metadata.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Датчик не найден: " + sensorId
            ));
        }

        SensorCommandDTO command = new SensorCommandDTO();
        command.setSensorId(sensorId);
        command.setTimestamp(System.currentTimeMillis() / 1000);

        Map<String, Object> result = new HashMap<>();
        result.put("sensorId", sensorId);
        result.put("status", "simulated");

        if (temperature != null) {
            command.setCommand("SET_TEMPERATURE");
            command.setValue(temperature);
            emulator.processCommand(command);
            result.put("temperature", temperature);
        }

        if (fillLevel != null) {
            command.setCommand("SET_FILL_LEVEL");
            command.setValue(fillLevel);
            emulator.processCommand(command);
            result.put("fillLevel", fillLevel);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Имитация ошибки датчика
     */
    @PostMapping("/fault/{sensorId}")
    public ResponseEntity<?> injectFault(
            @PathVariable String sensorId,
            @RequestParam String type,
            @RequestParam(required = false) String value) {

        log.warn("⚠️ Инъекция ошибки для датчика {}: тип={}, value={}", sensorId, type, value);

        // Проверяем существование датчика
        Optional<SensorMetadata> metadata = metadataService.getSensorMetadata(sensorId);
        if (metadata.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Датчик не найден: " + sensorId
            ));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("sensorId", sensorId);
        result.put("faultType", type);
        result.put("status", "fault_injected");

        // Здесь можно реализовать логику для разных типов ошибок
        // В текущей версии просто логируем
        // Для расширения функционала можно добавить специальные команды в эмулятор

        return ResponseEntity.ok(result);
    }

    /**
     * Очистить ошибку датчика
     */
    @PostMapping("/clear-fault/{sensorId}")
    public ResponseEntity<?> clearFault(@PathVariable String sensorId) {
        log.info("🔄 Очистка ошибки для датчика: {}", sensorId);

        return ResponseEntity.ok(Map.of(
                "sensorId", sensorId,
                "status", "fault_cleared"
        ));
    }

    /**
     * Получить текущие значения всех эмулируемых датчиков
     */
    @GetMapping("/status")
    public ResponseEntity<?> getEmulatorStatus() {
        return ResponseEntity.ok(emulator.getStats());
    }

    /**
     * Принудительная генерация телеметрии для всех датчиков
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateTelemetry() {
        log.info("🔄 Принудительная генерация телеметрии для всех датчиков");
        emulator.generateBackgroundTelemetry();
        return ResponseEntity.ok(Map.of(
                "status", "generated",
                "totalSensors", emulator.getStats().get("totalSensors")
        ));
    }

    /**
     * Принудительная генерация телеметрии для конкретного датчика
     */
    @PostMapping("/generate/{sensorId}")
    public ResponseEntity<?> generateTelemetryForSensor(@PathVariable String sensorId) {
        log.info("🔄 Принудительная генерация телеметрии для датчика: {}", sensorId);

        Optional<SensorMetadata> metadata = metadataService.getSensorMetadata(sensorId);
        if (metadata.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Датчик не найден: " + sensorId
            ));
        }

        emulator.generateSingleTelemetry(sensorId);

        return ResponseEntity.ok(Map.of(
                "sensorId", sensorId,
                "status", "generated"
        ));
    }
}