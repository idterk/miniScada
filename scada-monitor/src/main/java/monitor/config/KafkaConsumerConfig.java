package monitor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public NewTopic telemetryTopic() {
        return TopicBuilder.name("scada.telemetry.v1")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic commandTopic() {
        return TopicBuilder.name("scada.commands.v1")
                .partitions(1)
                .replicas(1)
                .build();
    }
}