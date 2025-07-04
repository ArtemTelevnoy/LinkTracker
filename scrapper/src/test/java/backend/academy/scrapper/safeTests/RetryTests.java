package backend.academy.scrapper.safeTests;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import backend.academy.dto.api.NotSupportedErrorCodeException;
import backend.academy.dto.api.SupportedErrorCodeException;
import backend.academy.dto.links.LinkUpdate;
import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.configs.ScrapperConfig;
import backend.academy.scrapper.service.HttpBotRetryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class RetryTests extends TestcontainersConfiguration {

    @MockitoBean
    @SuppressWarnings("unused")
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private BotClient botClient;

    @Autowired
    private ScrapperConfig config;

    @Autowired
    private HttpBotRetryService retryService;

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
    @DisplayName("test retry: retry some times, not always")
    void test1() {
        // Arrange
        final LinkUpdate linkUpdate1 = new LinkUpdate("desc1", 1);
        final LinkUpdate linkUpdate2 = new LinkUpdate("desc2", 2);
        final LinkUpdate linkUpdate3 = new LinkUpdate("desc3", 3);

        stubFor(post(urlEqualTo("/updates"))
                .inScenario("scenario")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withBody("updated"))
                .willSetStateTo("fail one time"));

        stubFor(post(urlEqualTo("/updates"))
                .inScenario("scenario")
                .whenScenarioStateIs("fail one time")
                .willReturn(aResponse().withStatus(500).withBody("not updated"))
                .willSetStateTo("good one time"));

        stubFor(post(urlEqualTo("/updates"))
                .inScenario("scenario")
                .whenScenarioStateIs("good one time")
                .willReturn(aResponse().withStatus(200).withBody("updated"))
                .willSetStateTo("always fail"));

        stubFor(post(urlEqualTo("/updates"))
                .inScenario("scenario")
                .whenScenarioStateIs("always fail")
                .willReturn(aResponse().withStatus(500).withBody("someResponse")));

        // Act
        final Executable res1 = () -> retryService.sendUpdatesWitRetry(botClient::sendUpdatesNoControl, linkUpdate1);
        final Executable res2 = () -> retryService.sendUpdatesWitRetry(botClient::sendUpdatesNoControl, linkUpdate2);
        final Executable res3 = () -> retryService.sendUpdatesWitRetry(botClient::sendUpdatesNoControl, linkUpdate3);

        // Assert
        Assertions.assertDoesNotThrow(res1);
        Assertions.assertDoesNotThrow(res2);
        Assertions.assertThrows(SupportedErrorCodeException.class, res3);
    }

    @Test
    @DisplayName("test retry: retry just for supported error codes")
    void test2() {
        // Arrange
        final LinkUpdate linkUpdate1 = new LinkUpdate("desc1", 1);

        stubFor(post(urlEqualTo("/updates")).willReturn(aResponse().withStatus(404)));

        // Act
        final Executable res = () -> retryService.sendUpdatesWitRetry(botClient::sendUpdatesNoControl, linkUpdate1);

        // Assert
        Assertions.assertThrows(NotSupportedErrorCodeException.class, res);
    }

    @Test
    @DisplayName("test retry: fallback")
    void test3() throws JsonProcessingException {
        // Arrange
        final LinkUpdate linkUpdate3 = new LinkUpdate("desc3", 3);

        final ObjectMapper objectMapper = new ObjectMapper();
        final String stringLinkUpdate3 = objectMapper.writeValueAsString(linkUpdate3);

        stubFor(post(urlEqualTo("/updates"))
                .willReturn(aResponse().withStatus(500).withBody("someResponse")));

        // Act
        final Executable res3 = () -> botClient.sendUpdatesWithControl(linkUpdate3);

        // Assert
        Assertions.assertDoesNotThrow(res3);

        Mockito.verify(kafkaTemplate, Mockito.times(1)).send(config.updatesTopicName(), stringLinkUpdate3);
    }
}
