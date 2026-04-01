package monitor.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import monitor.dto.SensorCommandDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommandProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public CommandProducer(KafkaTemplate<String, Object> kafkaTemplate,
                           ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendCommand(String sensorId, String command, Object value) {
        SensorCommandDTO cmd = new SensorCommandDTO(
                sensorId,
                command,
                value,
                System.currentTimeMillis() / 1000
        );

        kafkaTemplate.send("scada.commands.v1", sensorId, cmd);
        log.info("📤 Команда отправлена в Kafka: {} {} = {}", sensorId, command, value);
    }
}
