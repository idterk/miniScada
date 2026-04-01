package monitor.emulator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "emulator")
public class EmulatorProperties {

    /**
     * Включить эмулятор
     */
    private boolean enabled = false;

    /**
     * Режим работы:
     * - full: и фон, и ответы на команды
     * - simulate: только фоновая генерация
     * - respond: только ответы на команды
     */
    private String mode = "full";

    /**
     * Список датчиков для эмуляции
     */
    private List<SensorConfig> sensors = new ArrayList<>();

    /**
     * Интервал фоновой генерации (секунды)
     */
    private int backgroundInterval = 5;

    /**
     * Добавлять случайный шум
     */
    private boolean addNoise = true;

    /**
     * Диапазон шума
     */
    private double noiseRange = 1.5;

    /**
     * Время обработки команды (мс)
     */
    private long processingTimeMs = 300;

    @Data
    public static class SensorConfig {
        private String id;
        private double baseTemp = 22.0;
        private int baseFill = 50;
        private String type = "standard";
        private double minTemp = -10;
        private double maxTemp = 100;
        private boolean enabled = true;
    }
}
