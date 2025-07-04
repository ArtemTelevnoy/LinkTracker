package backend.academy.scrapper.clients;

import backend.academy.dto.links.LinkUpdate;
import backend.academy.scrapper.configs.ScrapperConfig;
import backend.academy.scrapper.service.HttpBotRetryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "message-transport", havingValue = "Kafka")
public class KafkaBotClient implements BotClient {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private BotClient httpBotClient;
    private final String updatesTopicName;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public KafkaBotClient(
            KafkaTemplate<String, String> kafkaTemplate,
            ScrapperConfig config,
            ClientsUtils clientsUtils,
            HttpBotRetryService retryService) {
        this(kafkaTemplate, config.updatesTopicName());
        httpBotClient = new HttpBotClient(config, clientsUtils, retryService);
    }

    @Override
    @CircuitBreaker(name = "webClientCircuitBreaker", fallbackMethod = "fallback")
    public void sendUpdatesWithControl(LinkUpdate linkUpdate) {
        sendUpdatesNoControl(linkUpdate);
    }

    @Override
    public void sendUpdatesNoControl(LinkUpdate linkUpdate) {
        try {
            kafkaTemplate.send(updatesTopicName, mapper.writeValueAsString(linkUpdate));
        } catch (JsonProcessingException e) {
            log.error("something went wrong: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    private void fallback(LinkUpdate linkUpdate, @NotNull Exception e) {
        log.warn("Something went wrong: {}, trying fallback to kafka", e.getMessage());
        httpBotClient.sendUpdatesNoControl(linkUpdate);
    }
}
