package backend.academy.bot.configs;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(
        @NotEmpty String telegramToken,
        @NotEmpty String scrapperBaseUrl,
        @Pattern(regexp = "HTTP|Kafka", message = "invalid transport type: only HTTP or Kafka") String messageTransport,
        @NotEmpty String updatesTopicName,
        @NotEmpty String dlqTopicName,
        long timeOutDuration,
        @NotNull List<Integer> retryCodes,
        @NotNull RateLimiterCredentials endpointsRateLimiter) {

    public record RateLimiterCredentials(Duration timeoutDuration, int limitForPeriod, Duration limitRefreshPeriod) {}
}
