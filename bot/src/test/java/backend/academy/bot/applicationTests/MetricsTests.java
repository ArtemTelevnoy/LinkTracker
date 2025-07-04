package backend.academy.bot.applicationTests;

import backend.academy.bot.metrics.UsersMessageCountMetric;
import backend.academy.bot.service.Bot;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.MeterNotFoundException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class MetricsTests extends TestcontainersConfiguration {

    @MockitoBean
    @SuppressWarnings("unused")
    private Bot bot;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private UsersMessageCountMetric usersMessageCountMetric;

    @BeforeAll
    static void before() {
        redis.start();
        kafka.start();
    }

    @AfterAll
    static void after() {
        redis.stop();
        kafka.stop();
    }

    @Test
    @DisplayName("custom metrics exist")
    void test1() {
        // Arrange

        // Act
        boolean metricsExist = true;
        try {
            meterRegistry.get("users_message_count_per_second").gauge();
        } catch (MeterNotFoundException ignored) {
            metricsExist = false;
        }

        // Assert
        Assertions.assertTrue(metricsExist);
    }

    @Test
    @SneakyThrows
    @DisplayName("custom metrics update")
    void test2() {
        // Arrange
        final Gauge messageCount =
                meterRegistry.get("users_message_count_per_second").gauge();

        // Act
        final double zeroCount = messageCount.value();

        usersMessageCountMetric.messageCounter().incrementAndGet();
        final double nonZeroCount = messageCount.value();

        Thread.sleep(1000);
        final double zeroCountAfterPeriod = messageCount.value();

        // Assert
        Assertions.assertEquals(0, zeroCount);
        Assertions.assertNotEquals(0, nonZeroCount);
        Assertions.assertEquals(0, zeroCountAfterPeriod);
    }
}
