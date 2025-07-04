package backend.academy.scrapper.applicationTests;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import backend.academy.scrapper.apiRecords.git.PullRequest;
import backend.academy.scrapper.apiRecords.stack.Question;
import backend.academy.scrapper.clients.ClientsUtils;
import backend.academy.scrapper.clients.GitHubClient;
import backend.academy.scrapper.clients.StackClient;
import backend.academy.scrapper.configs.ScrapperConfig;
import backend.academy.scrapper.exceptions.InvalidResponseBodyException;
import backend.academy.scrapper.exceptions.NotExistApiException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HttpClientsTests {
    private static GitHubClient gitHubClient;
    private static StackClient stackClient;
    private static WireMockServer server;

    @BeforeAll
    static void beforeAll() {
        server = new WireMockServer();
        WireMock.configureFor("localhost", 8080);
        server.start();

        final var gitHubCredentials = Mockito.mock(ScrapperConfig.GitHubCredentials.class);
        Mockito.when(gitHubCredentials.token()).thenReturn("mock_token");
        Mockito.when(gitHubCredentials.githubBaseUrl()).thenReturn("http://localhost:8080");

        final var stackOverflowCredentials = Mockito.mock(ScrapperConfig.StackOverflowCredentials.class);
        Mockito.when(stackOverflowCredentials.accessToken()).thenReturn("mock_token");
        Mockito.when(stackOverflowCredentials.key()).thenReturn("mock_key");
        Mockito.when(stackOverflowCredentials.stackBaseUrl()).thenReturn("http://localhost:8080");

        final var config = Mockito.mock(ScrapperConfig.class);
        Mockito.when(config.github()).thenReturn(gitHubCredentials);
        Mockito.when(config.stackOverflow()).thenReturn(stackOverflowCredentials);
        Mockito.when(config.timeoutDuration()).thenReturn(3000L);
        Mockito.when(config.retryCodes())
                .thenReturn(List.of(429, 500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511));

        final var utils = new ClientsUtils(config);

        gitHubClient = new GitHubClient(config, utils);
        stackClient = new StackClient(config, utils);
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    @DisplayName("git clint: throw NotExistApiException on error code or bad body")
    void test1() {
        // Arrange
        final String repoName1 = "repoName1";
        final String repoName2 = "repoName2";
        final String repoName3 = "repoName3";
        final String userName = "userName";
        final String repoUrl1 = String.format("/repos/%s/%s", userName, repoName1);
        final String repoUrl2 = String.format("/repos/%s/%s", userName, repoName2);
        final String repoUrl3 = String.format("/repos/%s/%s", userName, repoName3);

        stubFor(
                get(urlEqualTo(repoUrl1))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json; charset=utf-8")
                                        .withBody(
                                                """
                    [
                        {
                            "title": title1,
                            "user": {"login": login1},
                            "body": body1,
                            "created_at": 2025-04-27T10:50:38.727561500Z
                        }
                    ]
                    """)));

        stubFor(get(urlEqualTo(repoUrl2)).willReturn(aResponse().withStatus(400)));

        stubFor(get(urlEqualTo(repoUrl3))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("random text, not valid body")));

        // Act
        boolean isThrowOnUrl1 = false;
        try {
            gitHubClient.getNoTimer(repoUrl1, PullRequest[].class);
        } catch (NotExistApiException ignored) {
            isThrowOnUrl1 = true;
        } catch (Exception ignored) {
        }

        boolean isThrowOnUrl2 = false;
        try {
            gitHubClient.getNoTimer(repoUrl2, PullRequest[].class);
        } catch (NotExistApiException ignored) {
            isThrowOnUrl2 = true;
        } catch (Exception ignored) {
        }

        boolean isThrowOnUrl3 = false;
        try {
            gitHubClient.getNoTimer(repoUrl3, PullRequest[].class);
        } catch (InvalidResponseBodyException ignored) {
            isThrowOnUrl3 = true;
        } catch (Exception ignored) {
        }

        // Assert
        Assertions.assertFalse(isThrowOnUrl1);
        Assertions.assertTrue(isThrowOnUrl2);
        Assertions.assertTrue(isThrowOnUrl3);
    }

    @Test
    @DisplayName("stack clint: throw NotExistApiException on error code or bad body")
    void test2() {
        // Arrange
        final long questionId1 = 12345;
        final long questionId2 = 123;
        final long questionId3 = 456;
        final String repoUrl1 = String.format("/2.3/questions/%d?site=stackoverflow", questionId1);
        final String repoUrl2 = String.format("/2.3/questions/%d?site=stackoverflow", questionId2);
        final String repoUrl3 = String.format("/2.3/questions/%d?site=stackoverflow", questionId3);

        stubFor(
                get(urlEqualTo(repoUrl1))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json; charset=utf-8")
                                        .withBody(
                                                """
                    {
                        "items": [
                            "title": title1
                        ]
                    }
                    """)));

        stubFor(get(urlEqualTo(repoUrl2)).willReturn(aResponse().withStatus(400)));

        stubFor(get(urlEqualTo(repoUrl3))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("random text, not valid body")));

        // Act
        boolean isThrowOnUrl1 = false;
        try {
            stackClient.getNoTimer(repoUrl1, Question.class);
        } catch (NotExistApiException ignored) {
            isThrowOnUrl1 = true;
        } catch (Exception ignored) {
        }

        boolean isThrowOnUrl2 = false;
        try {
            stackClient.getNoTimer(repoUrl2, Question.class);
        } catch (NotExistApiException ignored) {
            isThrowOnUrl2 = true;
        } catch (Exception ignored) {
        }

        boolean isThrowOnUrl3 = false;
        try {
            gitHubClient.getNoTimer(repoUrl3, PullRequest[].class);
        } catch (InvalidResponseBodyException ignored) {
            isThrowOnUrl3 = true;
        } catch (Exception ignored) {
        }

        // Assert
        Assertions.assertFalse(isThrowOnUrl1);
        Assertions.assertTrue(isThrowOnUrl2);
        Assertions.assertTrue(isThrowOnUrl3);
    }
}
