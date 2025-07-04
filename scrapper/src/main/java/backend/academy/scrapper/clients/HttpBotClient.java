package backend.academy.scrapper.clients;

import backend.academy.dto.api.*;
import backend.academy.dto.links.LinkUpdate;
import backend.academy.scrapper.configs.ScrapperConfig;
import backend.academy.scrapper.exceptions.BadRequestException;
import backend.academy.scrapper.service.HttpBotRetryService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "message-transport", havingValue = "HTTP")
public class HttpBotClient implements BotClient {
    private final WebClient client;
    private BotClient kafkaBotClient;
    private final ClientsUtils clientsUtils;
    private final Duration timeout;
    private final HttpBotRetryService retryService;

    @Autowired
    public HttpBotClient(
            ScrapperConfig config,
            ClientsUtils clientsUtils,
            KafkaTemplate<String, String> kafkaTemplate,
            HttpBotRetryService retryService) {
        this(config, clientsUtils, retryService);
        kafkaBotClient = new KafkaBotClient(kafkaTemplate, config.updatesTopicName());
    }

    public HttpBotClient(ScrapperConfig config, ClientsUtils clientsUtils, HttpBotRetryService retryService) {
        this(
                WebClient.create(config.botBaseUrl()),
                clientsUtils,
                Duration.ofMillis(config.timeoutDuration()),
                retryService);
    }

    @Override
    @CircuitBreaker(name = "webClientCircuitBreaker", fallbackMethod = "fallback")
    public void sendUpdatesWithControl(LinkUpdate linkUpdate) {
        retryService.sendUpdatesWitRetry(this::sendUpdatesNoControl, linkUpdate);
    }

    @Override
    public void sendUpdatesNoControl(LinkUpdate linkUpdate) {
        final String response = client.post()
                .uri("/updates")
                .bodyValue(linkUpdate)
                .retrieve()
                .onStatus(clientsUtils::isSupportHttp, clientResponse -> {
                    log.error("{} supported error code response", clientResponse.statusCode());
                    return Mono.error(new SupportedErrorCodeException(clientResponse.statusCode()));
                })
                .onStatus(HttpStatus.BAD_REQUEST::equals, clientResponse -> {
                    log.warn("bad request");
                    return clientResponse
                            .bodyToMono(ApiErrorResponse.class)
                            .flatMap(error -> Mono.error(new BadRequestException(error)));
                })
                .onStatus(o -> !HttpStatus.OK.equals(o), clientResponse -> {
                    log.error("{} error code response, not supported", clientResponse.statusCode());
                    return Mono.error(new NotSupportedErrorCodeException(clientResponse.statusCode()));
                })
                .bodyToMono(String.class)
                .timeout(timeout)
                .block();

        if (!"updated".equals(response)) {
            log.warn("Something went wrong: {}", response);
        }
    }

    @SuppressWarnings("unused")
    private void fallback(LinkUpdate linkUpdate, @NotNull Exception e) {
        log.warn("Something went wrong: {}, trying fallback to kafka", e.getMessage());
        kafkaBotClient.sendUpdatesNoControl(linkUpdate);
    }
}
