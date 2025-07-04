package backend.academy.bot.safeTests;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.controller.KafkaController;
import backend.academy.bot.service.Bot;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestPropertySource(
        properties = {
            "app.timeout-duration=2000",
            "resilience4j.retry.instances.webClientRetry.retry-exceptions=backend.academy.dto.api.SupportedErrorCodeException",
            "resilience4j.circuitbreaker.instances.webClientCircuitBreaker.sliding-window-type=COUNT_BASED",
            "resilience4j.circuitbreaker.instances.webClientCircuitBreaker.sliding-window-size=2",
            "resilience4j.circuitbreaker.instances.webClientCircuitBreaker.minimum-number-of-calls=20",
            "resilience4j.circuitbreaker.instances.webClientCircuitBreaker.failure-rate-threshold=100",
            "resilience4j.circuitbreaker.instances.webClientCircuitBreaker.wait-duration-in-openState=1s",
            "resilience4j.circuitbreaker.instances.webClientCircuitBreaker.permitted-number-of-calls-in-half-open-state=1"
        })
@SpringBootTest
class CircuitBreakerTests {

    @MockitoBean
    @SuppressWarnings("unused")
    private KafkaController kafkaController;

    @MockitoBean
    @SuppressWarnings("unused")
    private Bot bot;

    @Autowired
    private ScrapperClient scrapperClient;

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void before() {
        wireMockServer = new WireMockServer(8081);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8081);
    }

    @AfterAll
    static void after() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("test circuitBreaker: error return before timeout")
    void test1() {
        // Arrange
        final long userId = 1;

        stubFor(post(urlEqualTo(String.format("/tg-chat/%d", userId)))
                .willReturn(aResponse().withStatus(500).withBody("someResponse").withFixedDelay(500)));

        // Act
        final Supplier<String> res = () -> scrapperClient.getStartResponse(userId);

        // Assert
        Assertions.assertThrows(CallNotPermittedException.class, res::get);
    }
}
