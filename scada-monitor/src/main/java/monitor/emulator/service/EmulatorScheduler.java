package monitor.emulator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import monitor.emulator.config.EmulatorProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class EmulatorScheduler {

    private final ControllerEmulator emulator;
    private final EmulatorProperties properties;

    @Scheduled(fixedDelayString = "${emulator.background-interval:5}000")
    public void generateBackgroundData() {
        if (!properties.isEnabled() || "respond".equalsIgnoreCase(properties.getMode())) {
            return;
        }

        emulator.generateBackgroundTelemetry();
    }

    @Scheduled(fixedDelay = 60000)
    public void logStats() {
        if (properties.isEnabled()) {
            log.info("📊 Статистика эмулятора: {}", emulator.getStats());
        }
    }
}