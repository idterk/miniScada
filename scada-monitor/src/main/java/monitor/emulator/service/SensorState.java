package monitor.emulator.service;

import lombok.Data;
import monitor.emulator.config.EmulatorProperties;

@Data
public class SensorState {
    private final String sensorId;
    private final String type;
    private double currentTemperature;
    private int currentFillLevel;
    private final double baseTemperature;
    private final int baseFillLevel;
    private final double minTemp;
    private final double maxTemp;
    private boolean online = true;
    private long lastUpdateTime;

    public SensorState(EmulatorProperties.SensorConfig config) {
        this.sensorId = config.getId();
        this.type = config.getType();
        this.baseTemperature = config.getBaseTemp();
        this.baseFillLevel = config.getBaseFill();
        this.currentTemperature = config.getBaseTemp();
        this.currentFillLevel = config.getBaseFill();
        this.minTemp = config.getMinTemp();
        this.maxTemp = config.getMaxTemp();
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void applyCommand(String command, Object value) {
        switch (command) {
            case "SET_TEMPERATURE":
                if (value instanceof Number) {
                    double newTemp = ((Number) value).doubleValue();
                    this.currentTemperature = Math.max(minTemp, Math.min(maxTemp, newTemp));
                }
                break;

            case "SET_FILL_LEVEL":
                if (value instanceof Number) {
                    int newFill = ((Number) value).intValue();
                    this.currentFillLevel = Math.max(0, Math.min(100, newFill));
                }
                break;

            case "CALIBRATE":
            case "RESET":
                this.currentTemperature = this.baseTemperature;
                this.currentFillLevel = this.baseFillLevel;
                break;
        }
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void simulateDrift() {
        double tempDrift = (Math.random() - 0.5) * 0.3;
        int fillDrift = (int)(Math.random() * 3) - 1;

        this.currentTemperature += tempDrift;
        this.currentFillLevel += fillDrift;

        this.currentTemperature = Math.max(minTemp, Math.min(maxTemp, this.currentTemperature));
        this.currentFillLevel = Math.max(0, Math.min(100, this.currentFillLevel));
    }
}