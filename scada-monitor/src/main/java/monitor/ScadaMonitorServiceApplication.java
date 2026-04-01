package monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScadaMonitorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScadaMonitorServiceApplication.class, args);
        System.out.println("\n🚀 SCADA запущен");
        System.out.println("📡 Kafka: scada.telemetry.v1 (in) / scada.commands.v1 (out)");
        System.out.println("🌐 WebSocket: /scada-websocket → /topic/sensor-updates");
        System.out.println("🖥️  http://localhost:8081");
    }
}