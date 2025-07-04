package backend.academy.bot.safeTests;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.controller.KafkaController;
import backend.academy.bot.service.Bot;
import backend.academy.dto.api.NotSupportedErrorCodeException;
import backend.academy.dto.api.SupportedErrorCodeException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
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

// We must isolate tests for retry from circuitBreaker(set big sliding-window-size)
@TestPropertySource(
        properties = {
            "resilience4j.retry.instances.webClientRetry.max-attempts=3",
            "resilience4j.retry.instances.webClientRetry.wait-duration=500",
            "resilience4j.retry.instances.webClientRetry.retry-exceptions=backend.academy.dto.api.SupportedErrorCodeException",
            "resilience4j.circuitbreaker.instances.webClientCircuitBreaker.sliding-window-type=COUNT_BASED",
            "resilience4j.circuitbreaker.instances.webClientCircuitBreaker.sliding-window-size=100",
            "resilience4j.circuitbreaker.instances.webClientCircuitBreaker.minimum-number-of-calls=20",
            "resilience4j.circuitbreaker.instances.webClientCircuitBreaker.failure-rate-threshold=100",
            "resilience4j.circuitbreaker.instances.webClientCircuitBreaker.wait-duration-in-openState=1s",
            "resilience4j.circuitbreaker.instances.webClientCircuitBreaker.permitted-number-of-calls-in-half-open-state=1"
        })
@SpringBootTest
class RetryTests {

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
    @DisplayName("test retry: retry some times, not always")
    void test1() {
        // Arrange
        final long[] userIds = new long[] {1, 2, 3};

        stubFor(post(urlEqualTo(String.format("/tg-chat/%d", userIds[0])))
                .willReturn(aResponse().withBody("someResponse")));

        stubFor(post(urlEqualTo(String.format("/tg-chat/%d", userIds[1])))
                .inScenario("scenario")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(500).withBody("someResponse"))
                .willSetStateTo("good response"));

        stubFor(post(urlEqualTo(String.format("/tg-chat/%d", userIds[1])))
                .inScenario("scenario")
                .whenScenarioStateIs("good response")
                .willReturn(aResponse().withStatus(200).withBody("someResponse2")));

        stubFor(post(urlEqualTo(String.format("/tg-chat/%d", userIds[2])))
                .willReturn(aResponse().withStatus(500).withBody("someResponse")));

        // Act
        final Supplier<String> res1 = () -> scrapperClient.getStartResponse(userIds[0]);
        final Supplier<String> res2 = () -> scrapperClient.getStartResponse(userIds[1]);
        final Supplier<String> res3 = () -> scrapperClient.getStartResponse(userIds[2]);

        // Assert
        Assertions.assertDoesNotThrow(res1::get);
        Assertions.assertDoesNotThrow(res2::get);
        Assertions.assertThrows(SupportedErrorCodeException.class, res3::get);
    }

    @Test
    @DisplayName("test retry: just for supported error codes")
    void test2() {
        // Arrange
        final long userId = 1;

        stubFor(post(urlEqualTo(String.format("/tg-chat/%d", userId)))
                .willReturn(aResponse().withStatus(404)));

        // Act
        final Supplier<String> res = () -> scrapperClient.getStartResponse(userId);

        // Assert
        Assertions.assertThrows(NotSupportedErrorCodeException.class, res::get);
    }
}
