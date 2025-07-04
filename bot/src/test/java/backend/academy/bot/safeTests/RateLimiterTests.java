package backend.academy.bot.safeTests;

import backend.academy.bot.controller.HttpController;
import backend.academy.bot.controller.KafkaController;
import backend.academy.bot.service.Bot;
import backend.academy.bot.service.ProcessingUpdates;
import backend.academy.dto.links.LinkUpdate;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestPropertySource(
        properties = {
            "app.endpointsRateLimiter.timeout-duration=0s",
            "app.endpointsRateLimiter.limit-for-period=3",
            "app.endpointsRateLimiter.limit-refresh-period=5s"
        })
@SpringBootTest
class RateLimiterTests {

    @MockitoBean
    @SuppressWarnings("unused")
    private KafkaController kafkaController;

    @MockitoBean
    @SuppressWarnings("unused")
    private Bot bot;

    @Autowired
    private HttpController controller;

    @MockitoBean
    @SuppressWarnings("unused")
    private ProcessingUpdates processingUpdates;

    @Test
    @DisplayName("test rateLimiter: ddos")
    void test1() {
        // Arrange
        final LinkUpdate linkUpdate = new LinkUpdate("desc", 123);
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Mockito.when(request.getRemoteAddr()).thenReturn(String.valueOf(linkUpdate.userId()));

        // Act
        boolean isThrowByRateLimiter = false;
        try {
            IntStream.range(0, 100).parallel().forEach(ignored -> controller.updates(linkUpdate, request));
        } catch (RequestNotPermitted ignored) {
            isThrowByRateLimiter = true;
        }

        // Assert
        Assertions.assertTrue(isThrowByRateLimiter);
    }

    @Test
    @DisplayName("test rateLimiter: no ddos because(small count of requests)")
    void test2() {
        // Arrange
        final LinkUpdate linkUpdate = new LinkUpdate("desc", 456);
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Mockito.when(request.getRemoteAddr()).thenReturn(String.valueOf(linkUpdate.userId()));

        // Act
        boolean isThrowByRateLimiter = false;
        try {
            IntStream.range(0, 3).parallel().forEach(ignored -> controller.updates(linkUpdate, request));
        } catch (RequestNotPermitted ignored) {
            isThrowByRateLimiter = true;
        }

        // Assert
        Assertions.assertFalse(isThrowByRateLimiter);
    }

    @Test
    @DisplayName("test rateLimiter: no ddos because(different ips of requests)")
    void test3() {
        // Arrange
        final int countOfRequests = 100;
        final LinkUpdate[] linkUpdates = new LinkUpdate[countOfRequests];
        final HttpServletRequest[] mockServlets = new HttpServletRequest[countOfRequests];

        for (int i = 0; i < countOfRequests; i++) {
            linkUpdates[i] = new LinkUpdate("desc", i);
            mockServlets[i] = Mockito.mock(HttpServletRequest.class);
            Mockito.when(mockServlets[i].getRemoteAddr()).thenReturn(String.valueOf(i));
        }

        // Act
        boolean isThrowByRateLimiter = false;
        try {
            IntStream.range(0, countOfRequests)
                    .parallel()
                    .forEach(i -> controller.updates(linkUpdates[i], mockServlets[i]));
        } catch (RequestNotPermitted ignored) {
            isThrowByRateLimiter = true;
        }

        // Assert
        Assertions.assertFalse(isThrowByRateLimiter);
    }
}
