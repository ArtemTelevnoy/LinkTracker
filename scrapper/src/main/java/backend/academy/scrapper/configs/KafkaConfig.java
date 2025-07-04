package backend.academy.scrapper.configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    @Value("${app.updates-topic-name}")
    private String updatesTopicName;

    @Value("${app.dlq-topic-name}")
    private String dlqTopicName;

    @Bean
    public NewTopic updateTopic() {
        return new NewTopic(updatesTopicName, 1, (short) 1);
    }

    @Bean
    public NewTopic dlqTopic() {
        return new NewTopic(dlqTopicName, 1, (short) 1);
    }
}
