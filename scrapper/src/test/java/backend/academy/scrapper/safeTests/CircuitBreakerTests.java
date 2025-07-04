package backend.academy.scrapper.safeTests;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import backend.academy.dto.links.LinkUpdate;
import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.configs.ScrapperConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
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
class CircuitBreakerTests extends TestcontainersConfiguration {

    @MockitoBean
    @SuppressWarnings("unused")
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ScrapperConfig config;

    @Autowired
    private BotClient botClient;

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void before() {
        kafka.start();
        postgres.start();
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8080);
    }

    @AfterAll
    static void after() {
        kafka.stop();
        postgres.stop();
        wireMockServer.stop();
    }

    @Test
    @DisplayName("test circuitBreaker: error return before timeout")
    void test1() throws JsonProcessingException {
        // Arrange
        final LinkUpdate linkUpdate = new LinkUpdate("desc1", 1);

        final ObjectMapper objectMapper = new ObjectMapper();
        final String stringLinkUpdate = objectMapper.writeValueAsString(linkUpdate);

        stubFor(post(urlEqualTo("/updates"))
                .willReturn(aResponse().withStatus(500).withBody("someResponse").withFixedDelay(500)));

        // Act
        final Executable res = () -> botClient.sendUpdatesWithControl(linkUpdate);

        // Assert
        Assertions.assertDoesNotThrow(res);

        Mockito.verify(kafkaTemplate, Mockito.times(1)).send(config.updatesTopicName(), stringLinkUpdate);
    }
}
