package backend.academy.scrapper.metrics;

import backend.academy.scrapper.repositories.link.LinkRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ActiveLinksCountMetric {
    private final LinkRepository linkRepository;
    private final AtomicInteger githubLinksCounter;
    private final AtomicInteger stackLinksCounter;

    @Autowired
    public ActiveLinksCountMetric(MeterRegistry meterRegistry, LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
        githubLinksCounter =
                meterRegistry.gauge("github_links_counter", new AtomicInteger(linkRepository.countActiveGithubLinks()));
        stackLinksCounter =
                meterRegistry.gauge("stack_links_counter", new AtomicInteger(linkRepository.countActiveStackLinks()));
    }

    @Scheduled(fixedRateString = "${app.metrics-updater-delay}")
    public void checkCountOfActiveLinks() {
        githubLinksCounter.set(linkRepository.countActiveGithubLinks());
        stackLinksCounter.set(linkRepository.countActiveStackLinks());
    }
}
