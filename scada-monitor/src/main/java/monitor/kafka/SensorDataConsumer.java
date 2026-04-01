package monitor.kafka;

import monitor.dto.SensorDataDTO;
import monitor.entity.ErrorLog;
import monitor.service.ErrorLogService;
import monitor.service.SensorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SensorDataConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final SensorService sensorService;
    private final ErrorLogService errorLogService;

    public SensorDataConsumer(SimpMessagingTemplate messagingTemplate,
                              SensorService sensorService,
                              ErrorLogService errorLogService) {
        this.messagingTemplate = messagingTemplate;
        this.sensorService = sensorService;
        this.errorLogService = errorLogService;
    }

    @KafkaListener(topics = "scada.telemetry.v1", groupId = "scada-minimal-group")
    public void consume(SensorDataDTO dto) {
        try {
            // Валидация
            if (dto.getSensorId() == null || dto.getSensorId().isBlank()) {
                errorLogService.logError(
                        ErrorLog.ErrorType.VALIDATION_ERROR,
                        null,
                        "Получены данные без sensorId"
                );
                return;
            }

            if (dto.getTimestamp() == null || dto.getTimestamp() <= 0) {
                dto.setTimestamp(System.currentTimeMillis() / 1000);
            }

            // Проверяем существование датчика
            if (sensorService.getSensor(dto.getSensorId()) == null) {
                errorLogService.logError(
                        ErrorLog.ErrorType.UNKNOWN_SENSOR,
                        dto.getSensorId(),
                        "Получены данные для неизвестного датчика"
                );
                return;
            }

            // 1. Обновляем состояние в сервисе
            sensorService.updateOrCreateSensor(dto);

            // 2. Рассылаем клиентам
            messagingTemplate.convertAndSend("/topic/sensor-updates", dto);
            log.debug("✅ Данные обработаны: {}", dto);

        } catch (Exception e) {
            log.error("❌ Ошибка обработки Kafka сообщения: {}", e.getMessage());
            errorLogService.logException(
                    ErrorLog.ErrorType.KAFKA_CONSUMER_ERROR,
                    dto != null ? dto.getSensorId() : null,
                    e
            );
        }
    }
}