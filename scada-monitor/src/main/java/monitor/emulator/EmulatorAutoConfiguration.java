package monitor.emulator;

import monitor.emulator.config.EmulatorKafkaConfig;
import monitor.emulator.config.EmulatorProperties;
import monitor.emulator.consumer.CommandConsumer;
import monitor.emulator.producer.TelemetryProducer;
import monitor.emulator.service.ControllerEmulator;
import monitor.emulator.service.EmulatorScheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "emulator.enabled", havingValue = "true")
@Import({
        EmulatorProperties.class,
        EmulatorKafkaConfig.class,
        CommandConsumer.class,
        TelemetryProducer.class,
        ControllerEmulator.class,
        EmulatorScheduler.class
})
public class EmulatorAutoConfiguration {
    // Этот класс просто включает все бины эмулятора
    // когда emulator.enabled = true
}