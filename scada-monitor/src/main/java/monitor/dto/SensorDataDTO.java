package monitor.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorDataDTO {

    @NotBlank(message = "sensorId не может быть пустым")
    private String sensorId;

    @Min(value = -100, message = "Температура не может быть ниже -100°C")
    @Max(value = 100, message = "Температура не может быть выше 100°C")
    private Double temperature;

    @Min(value = 0, message = "Заполненность не может быть меньше 0%")
    @Max(value = 100, message = "Заполненность не может быть больше 100%")
    private Integer fillLevel;

    private Long timestamp;
}