package monitor.emulator.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import monitor.dto.SensorDataDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetryProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendTelemetry(SensorDataDTO data) {
        kafkaTemplate.send("scada.telemetry.v1", data.getSensorId(), data);
    }
}