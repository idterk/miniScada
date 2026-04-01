package monitor.controller;

import monitor.dto.SensorDataDTO;
import monitor.entity.ErrorLog;
import monitor.entity.UserAuditLog;
import monitor.kafka.CommandProducer;
import monitor.service.AuditService;
import monitor.service.ErrorLogService;
import monitor.service.SensorMetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/sensor")
public class SensorController {

    private final CommandProducer commandProducer;
    private final SensorMetadataService metadataService;
    private final AuditService auditService;
    private final ErrorLogService errorLogService;

    public SensorController(CommandProducer commandProducer,
                            SensorMetadataService metadataService,
                            AuditService auditService,
                            ErrorLogService errorLogService) {
        this.commandProducer = commandProducer;
        this.metadataService = metadataService;
        this.auditService = auditService;
        this.errorLogService = errorLogService;
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateSensor(@RequestBody @Valid SensorDataDTO dto,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (dto.getTimestamp() == null || dto.getTimestamp() <= 0) {
                dto.setTimestamp(System.currentTimeMillis() / 1000);
            }

            // Проверяем существование датчика через метаданные
            var metadata = metadataService.getSensorMetadata(dto.getSensorId());
            if (metadata.isEmpty()) {
                errorLogService.logError(
                        ErrorLog.ErrorType.UNKNOWN_SENSOR,
                        dto.getSensorId(),
                        "Попытка обновить неизвестный датчик"
                );
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Датчик не найден: " + dto.getSensorId()
                ));
            }

            String username = userDetails != null ? userDetails.getUsername() : "unknown";
            log.info("🖱️ Запрос на изменение от пользователя: {} для датчика: {}", username, dto.getSensorId());

            // Отправляем команды в Kafka
            if (dto.getTemperature() != null) {
                commandProducer.sendCommand(dto.getSensorId(), "SET_TEMPERATURE", dto.getTemperature());

                auditService.log(
                        username,
                        UserAuditLog.ACTION_UPDATE_VALUE,
                        "SENSOR",
                        dto.getSensorId(),
                        null,
                        "temperature: " + dto.getTemperature()
                );
            }

            if (dto.getFillLevel() != null) {
                commandProducer.sendCommand(dto.getSensorId(), "SET_FILL_LEVEL", dto.getFillLevel());

                auditService.log(
                        username,
                        UserAuditLog.ACTION_UPDATE_VALUE,
                        "SENSOR",
                        dto.getSensorId(),
                        null,
                        "fillLevel: " + dto.getFillLevel()
                );
            }

            return ResponseEntity.ok(Map.of(
                    "status", "processing",
                    "message", "Команды отправлены в шину данных"
            ));

        } catch (Exception e) {
            log.error("Ошибка при обработке запроса: {}", e.getMessage());
            errorLogService.logException(
                    ErrorLog.ErrorType.INTERNAL_ERROR,
                    dto.getSensorId(),
                    e
            );
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Внутренняя ошибка сервера"
            ));
        }
    }
}