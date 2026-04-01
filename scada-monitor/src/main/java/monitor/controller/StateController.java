package monitor.controller;

import monitor.dto.SensorDataDTO;
import monitor.service.SensorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/state")
public class StateController {

    private final SensorService sensorService;

    public StateController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping("/{sensorId}")
    public SensorDataDTO getSensorState(@PathVariable String sensorId) {
        return sensorService.getSensor(sensorId);
    }

}