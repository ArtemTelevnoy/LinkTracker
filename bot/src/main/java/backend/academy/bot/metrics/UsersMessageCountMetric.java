package backend.academy.bot.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Getter
@Component
@EnableScheduling
public class UsersMessageCountMetric {
    private final AtomicInteger messageCounter;

    @Autowired
    public UsersMessageCountMetric(MeterRegistry meterRegistry) {
        messageCounter = meterRegistry.gauge("users_message_count_per_second", new AtomicInteger(0));
    }

    @Scheduled(fixedRate = 1000)
    public void zeroCounter() {
        messageCounter.set(0);
    }
}
