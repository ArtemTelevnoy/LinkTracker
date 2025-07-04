package backend.academy.scrapper.configs;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
        GitHubCredentials github,
        StackOverflowCredentials stackOverflow,
        @NotEmpty String botBaseUrl,
        @Pattern(regexp = "SQL|ORM", message = "invalid access type: only SQL or ORM") String accessType,
        @Pattern(regexp = "HTTP|Kafka", message = "invalid transport type: only HTTP or Kafka") String messageTransport,
        int threadCount,
        int schedulerDelay,
        int metricsUpdaterDelay,
        int batchSize,
        @NotEmpty String updatesTopicName,
        @NotEmpty String dlqTopicName,
        long timeoutDuration,
        @NotNull List<Integer> retryCodes,
        @NotNull RateLimiterCredentials endpointsRateLimiter) {

    public record GitHubCredentials(@NotEmpty String token, @NotEmpty String githubBaseUrl) {}

    public record StackOverflowCredentials(
            @NotEmpty String key, @NotEmpty String accessToken, @NotEmpty String stackBaseUrl) {}

    public record RateLimiterCredentials(Duration timeoutDuration, int limitForPeriod, Duration limitRefreshPeriod) {}
}
