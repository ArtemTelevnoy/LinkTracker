package backend.academy.scrapper.clients;

import backend.academy.scrapper.configs.ScrapperConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class StackClient implements SiteClient {
    private final WebClient client;
    private final ClientsUtils clientsUtils;
    private Timer stackScrapTimer;

    @Autowired
    public StackClient(ScrapperConfig config, ClientsUtils clientsUtils, MeterRegistry meterRegistry) {
        this(config, clientsUtils);

        stackScrapTimer = Timer.builder("stack_scrap_timer")
                .publishPercentiles(0.5, 0.9, 0.99)
                .register(meterRegistry);
    }

    public StackClient(ScrapperConfig config, ClientsUtils clientsUtils) {
        this.clientsUtils = clientsUtils;

        client = WebClient.builder()
                .baseUrl(config.stackOverflow().stackBaseUrl())
                .defaultHeader("access_token", config.stackOverflow().accessToken())
                .defaultHeader("key", config.stackOverflow().key())
                .build();
    }

    @Override
    @Retry(name = "webClientRetry")
    @CircuitBreaker(name = "webClientCircuitBreaker")
    public <T> T get(String uri, Class<T> clazz) {
        return stackScrapTimer.record(() -> getNoTimer(uri, clazz));
    }

    @Override
    public <T> T getNoTimer(String uri, Class<T> clazz) {
        return clientsUtils.get(client, uri, clazz);
    }
}
