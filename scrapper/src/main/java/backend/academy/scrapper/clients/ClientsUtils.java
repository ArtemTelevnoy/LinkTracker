package backend.academy.scrapper.clients;

import backend.academy.dto.api.SupportedErrorCodeException;
import backend.academy.scrapper.configs.ScrapperConfig;
import backend.academy.scrapper.exceptions.InvalidResponseBodyException;
import backend.academy.scrapper.exceptions.NotExistApiException;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ClientsUtils {
    private final Duration timeout;
    private final List<HttpStatusCode> retryableHttpStatuses;

    @Autowired
    public ClientsUtils(ScrapperConfig config) {
        timeout = Duration.ofMillis(config.timeoutDuration());
        retryableHttpStatuses =
                config.retryCodes().stream().map(HttpStatusCode::valueOf).toList();
    }

    <T> T get(@NotNull WebClient client, String uri, Class<T> clazz) {
        return client.get()
                .uri(uri)
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().equals(HttpStatus.OK)) {
                        return clientResponse.bodyToMono(clazz).onErrorResume(DecodingException.class, e -> {
                            log.error("Invalid response body: {}", e.getMessage());
                            return Mono.error(new InvalidResponseBodyException(uri, e));
                        });
                    } else if (isSupportHttp(clientResponse.statusCode())) {
                        log.error("{} error code response", clientResponse.statusCode());
                        return Mono.error(new SupportedErrorCodeException(clientResponse.statusCode()));
                    }

                    return Mono.error(new NotExistApiException(uri));
                })
                .timeout(timeout)
                .block();
    }

    boolean isSupportHttp(@NotNull HttpStatusCode code) {
        return retryableHttpStatuses.contains(code);
    }
}
