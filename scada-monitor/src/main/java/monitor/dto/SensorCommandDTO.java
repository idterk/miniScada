package monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorCommandDTO {
    private String sensorId;
    private String command;
    private Object value;
    private Long timestamp;
}