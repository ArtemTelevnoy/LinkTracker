package backend.academy.bot.controller;

import backend.academy.bot.service.ProcessingUpdates;
import backend.academy.dto.links.LinkUpdate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaController {
    private final ProcessingUpdates processingUpdates;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${app.dlq-topic-name}")
    private String dlqTopicName;

    @KafkaListener(topics = "${app.updates-topic-name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String jsonUpdate) {
        try {
            processingUpdates.updates(mapper.readValue(jsonUpdate, LinkUpdate.class));
        } catch (JsonProcessingException e) {
            kafkaTemplate.send(dlqTopicName, e.getMessage());
        }
    }
}
