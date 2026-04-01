package monitor.controller;

import monitor.dto.SensorDataDTO;
import monitor.service.SensorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sensors")
public class SensorManagerController {

    private final SensorService sensorService;

    public SensorManagerController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping
    public List<String> getAllSensors() {
        return sensorService.getAllSensorIds();
    }

    @GetMapping("/{sensorId}")
    public SensorDataDTO getSensor(@PathVariable String sensorId) {
        return sensorService.getSensor(sensorId);
    }

    @PostMapping("/add")
    public SensorDataDTO addSensor() {
        return sensorService.addNewSensor();
    }
}