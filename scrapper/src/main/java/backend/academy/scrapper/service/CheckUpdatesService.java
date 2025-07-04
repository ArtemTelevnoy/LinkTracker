package backend.academy.scrapper.service;

import backend.academy.scrapper.configs.ScrapperConfig;
import backend.academy.scrapper.link.LinkBody;
import backend.academy.scrapper.link.LinkType;
import backend.academy.scrapper.link.LinkUpdatesInfo;
import backend.academy.scrapper.link.ListLinkUpdatesInfo;
import backend.academy.scrapper.repositories.filter.FilterRepository;
import backend.academy.scrapper.repositories.link.LinkRepository;
import backend.academy.scrapper.repositories.userLink.UserLinkRepository;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class CheckUpdatesService {
    private final LinkRepository linkRepository;
    private final UserLinkRepository userLinkRepository;
    private final FilterRepository filterRepository;
    private final SiteService gitHubService;
    private final SiteService stackService;
    private final ScrapperConfig config;
    private final UserUpdatesService userUpdatesService;
    private ExecutorService executor;

    @PostConstruct
    public void init() {
        executor = Executors.newFixedThreadPool(config.threadCount());
    }

    @Scheduled(fixedRateString = "${app.scheduler-delay}")
    public void monitoringUpdates() {
        int skipCount = 0;
        List<LinkBody> links;

        log.info("Starting checking links updates");
        while (true) {
            links = linkRepository.getLinksForUpdates(config.batchSize(), skipCount);

            if (links.isEmpty()) {
                log.info("Ending checking links updates");
                break;
            }

            skipCount += links.size();
            final List<Future<?>> futures = new ArrayList<>(config.threadCount());

            splitOnThead(links).forEach(batchedLinks -> futures.add(executor.submit(() -> updateLinks(batchedLinks))));

            futures.forEach(future -> {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Something was wrong while checking links: {}", e.getMessage());
                }
            });
        }
    }

    private List<List<LinkBody>> splitOnThead(@NotNull List<LinkBody> links) {
        final List<List<LinkBody>> splitLinks = new ArrayList<>();
        final int segmentSize = (int) Math.ceil((double) links.size() / config.batchSize());

        for (int i = 0; i < links.size(); i += segmentSize) {
            splitLinks.add(links.subList(i, Math.min(i + segmentSize, links.size())));
        }

        return splitLinks;
    }

    private void updateLinks(@NotNull List<LinkBody> links) {
        ListLinkUpdatesInfo updatesList;

        for (LinkBody link : links) {
            updatesList = link.type() == LinkType.GITHUB
                    ? gitHubService.getLinkUpdate(link)
                    : stackService.getLinkUpdate(link);

            if (updatesList == null) {
                continue;
            }

            filterLinks(updatesList.updatesInfos(), link);
            linkRepository.updateLinkTime(link.id(), updatesList.updatedTime());
        }
    }

    private void filterLinks(LinkUpdatesInfo[] updatesInfos, @NotNull LinkBody link) {
        for (long chat : userLinkRepository.getChats(link.id())) {
            final String[] filters = filterRepository.get(chat, link.id());
            final String newUserUpdates = Arrays.stream(updatesInfos)
                    .filter(info -> {
                        for (String filter : filters) {
                            if (info.userName().equals(filter)) {
                                return false;
                            }
                        }

                        return true;
                    })
                    .map(info ->
                            String.format("url %s: %s, userName=%s; ", link.url(), info.description(), info.userName()))
                    .collect(Collectors.joining());

            userUpdatesService.update(chat, newUserUpdates);
        }
    }
}
