package monitor.emulator.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import monitor.dto.SensorCommandDTO;
import monitor.emulator.config.EmulatorProperties;
import monitor.emulator.service.ControllerEmulator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandConsumer {

    private final ControllerEmulator emulator;
    private final EmulatorProperties properties;

    @KafkaListener(
            topics = "scada.commands.v1",
            groupId = "scada-emulator-group",
            containerFactory = "emulatorKafkaListenerFactory",
            autoStartup = "${emulator.enabled:false}"
    )
    public void consumeCommand(SensorCommandDTO command) {
        if (!properties.isEnabled() || "simulate".equalsIgnoreCase(properties.getMode())) {
            return;
        }

        emulator.processCommand(command);
    }
}