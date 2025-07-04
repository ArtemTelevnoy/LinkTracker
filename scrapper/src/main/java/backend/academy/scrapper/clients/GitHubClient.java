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
public class GitHubClient implements SiteClient {
    private final WebClient client;
    private final ClientsUtils clientsUtils;
    private Timer githubScrapTimer;

    @Autowired
    public GitHubClient(ScrapperConfig config, ClientsUtils clientsUtils, MeterRegistry meterRegistry) {
        this(config, clientsUtils);

        githubScrapTimer = Timer.builder("github_scrap_timer")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    public GitHubClient(ScrapperConfig config, ClientsUtils clientsUtils) {
        this.clientsUtils = clientsUtils;

        client = WebClient.builder()
                .defaultHeader(
                        "Authorization",
                        String.format("Bearer %s", config.github().token()))
                .baseUrl(config.github().githubBaseUrl())
                .build();
    }

    @Override
    @Retry(name = "webClientRetry")
    @CircuitBreaker(name = "webClientCircuitBreaker")
    public <T> T get(String uri, Class<T> clazz) {
        return githubScrapTimer.record(() -> getNoTimer(uri, clazz));
    }

    @Override
    public <T> T getNoTimer(String uri, Class<T> clazz) {
        return clientsUtils.get(client, uri, clazz);
    }
}
