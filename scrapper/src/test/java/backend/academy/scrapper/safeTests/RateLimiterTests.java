package backend.academy.scrapper.safeTests;

import backend.academy.scrapper.controllers.ChatController;
import backend.academy.scrapper.repositories.user.UserRepository;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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
class RateLimiterTests extends TestcontainersConfiguration {

    @MockitoBean
    @SuppressWarnings("unused")
    private UserRepository userRepository;

    @Autowired
    private ChatController controller;

    @BeforeAll
    static void before() {
        kafka.start();
        postgres.start();
    }

    @AfterAll
    static void after() {
        kafka.stop();
        postgres.stop();
    }

    @Test
    @DisplayName("test rateLimiter: ddos")
    void test1() {
        // Arrange
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Mockito.when(request.getRemoteAddr()).thenReturn("123");

        // Act
        boolean isThrowByRateLimiter = false;
        try {
            IntStream.range(0, 100).parallel().forEach(ignored -> controller.start(123, request));
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
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Mockito.when(request.getRemoteAddr()).thenReturn("456");

        // Act
        boolean isThrowByRateLimiter = false;
        try {
            IntStream.range(0, 3).parallel().forEach(ignored -> controller.start(456, request));
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
        final HttpServletRequest[] mockServlets = new HttpServletRequest[countOfRequests];

        for (int i = 0; i < countOfRequests; i++) {
            mockServlets[i] = Mockito.mock(HttpServletRequest.class);
            Mockito.when(mockServlets[i].getRemoteAddr()).thenReturn(String.valueOf(i));
        }

        // Act
        boolean isThrowByRateLimiter = false;
        try {
            IntStream.range(0, countOfRequests).parallel().forEach(i -> controller.start(i, mockServlets[i]));
        } catch (RequestNotPermitted ignored) {
            isThrowByRateLimiter = true;
        }

        // Assert
        Assertions.assertFalse(isThrowByRateLimiter);
    }
}
