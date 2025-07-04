package backend.academy.scrapper.applicationTests;

import backend.academy.scrapper.apiRecords.git.PullRequest;
import backend.academy.scrapper.apiRecords.stack.Answers;
import backend.academy.scrapper.clients.ClientsUtils;
import backend.academy.scrapper.clients.GitHubClient;
import backend.academy.scrapper.clients.StackClient;
import backend.academy.scrapper.link.LinkInfo;
import backend.academy.scrapper.repositories.link.LinkRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.search.MeterNotFoundException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class MetricsTests extends TestcontainersConfiguration {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private LinkRepository linkRepository;

    @MockitoBean
    @SuppressWarnings("unused")
    private ClientsUtils clientsUtils;

    @Autowired
    private GitHubClient gitHubClient;

    @Autowired
    private StackClient stackClient;

    @Value("${app.metrics-updater-delay}")
    private int countLinkDelay;

    @BeforeAll
    static void before() {
        postgres.start();
        kafka.start();
        redis.start();
    }

    @AfterAll
    static void after() {
        postgres.stop();
        kafka.stop();
        redis.stop();
    }

    @Test
    @DisplayName("all custom metrics exist")
    void test1() {
        // Arrange

        // Act
        boolean allMetricsExist = true;
        try {
            meterRegistry.get("github_links_counter").meter();
            meterRegistry.get("stack_links_counter").meter();
            meterRegistry.get("github_scrap_timer").meter();
            meterRegistry.get("stack_scrap_timer").meter();
        } catch (MeterNotFoundException ignored) {
            allMetricsExist = false;
        }

        // Assert
        Assertions.assertTrue(allMetricsExist);
    }

    @Test
    @SneakyThrows
    @DisplayName("custom metrics update: gauges")
    void test2() {
        // Arrange
        final Gauge githubLinksCounter =
                meterRegistry.get("github_links_counter").gauge();
        final Gauge stackLinksCounter = meterRegistry.get("stack_links_counter").gauge();

        final Instant time = Instant.parse("2025-10-01T10:15:30Z");

        // Act
        final double zeroGitLinks = githubLinksCounter.value();
        final double zeroStackLinks = stackLinksCounter.value();

        for (int i = 0; i < 5; i++) {
            linkRepository.add(new LinkInfo(String.format("gitUrl%d", i), time, true));
        }
        Thread.sleep(countLinkDelay);

        final double fiveGitLinks = githubLinksCounter.value();
        final double stillZeroStackLinks = stackLinksCounter.value();

        for (int i = 0; i < 3; i++) {
            linkRepository.add(new LinkInfo(String.format("stackUrl%d", i), time, false));
        }
        Thread.sleep(countLinkDelay);

        final double stillFiveGitLinks = githubLinksCounter.value();
        final double threeStackLinks = stackLinksCounter.value();

        // Assert
        Assertions.assertEquals(0, zeroGitLinks);
        Assertions.assertEquals(0, zeroStackLinks);
        Assertions.assertEquals(5, fiveGitLinks);
        Assertions.assertEquals(0, stillZeroStackLinks);
        Assertions.assertEquals(5, stillFiveGitLinks);
        Assertions.assertEquals(3, threeStackLinks);
    }

    @Test
    @SneakyThrows
    @DisplayName("custom metrics update: timers")
    void test3() {
        // Arrange
        final Timer githubScrapTimer = meterRegistry.get("github_scrap_timer").timer();
        final Timer stackScrapTimer = meterRegistry.get("stack_scrap_timer").timer();

        // Act
        final double zeroGitTimer = githubScrapTimer.totalTime(TimeUnit.NANOSECONDS);
        final double zeroStackTimer = githubScrapTimer.totalTime(TimeUnit.NANOSECONDS);

        gitHubClient.get("gitUrl", PullRequest[].class);
        stackClient.get("stackUrl", Answers.class);

        final double nonZeroGitTimer = githubScrapTimer.totalTime(TimeUnit.NANOSECONDS);
        final double nonZeroStackTimer = stackScrapTimer.totalTime(TimeUnit.NANOSECONDS);

        // Assert
        Assertions.assertEquals(0, zeroGitTimer);
        Assertions.assertEquals(0, zeroStackTimer);
        Assertions.assertTrue(0 < nonZeroGitTimer);
        Assertions.assertTrue(0 < nonZeroStackTimer);
    }
}
