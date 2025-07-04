package backend.academy.scrapper.service;

import backend.academy.dto.links.LinkResponse;
import backend.academy.dto.links.ListLinksResponse;
import backend.academy.dto.tags.*;
import backend.academy.scrapper.configs.ScrapperConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IpRateLimiterService {
    private final RateLimiterRegistry registry;
    private final ConcurrentMap<String, RateLimiter> ipRateLimiters = new ConcurrentHashMap<>();

    @Autowired
    public IpRateLimiterService(ScrapperConfig config) {
        final RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                .timeoutDuration(config.endpointsRateLimiter().timeoutDuration())
                .limitForPeriod(config.endpointsRateLimiter().limitForPeriod())
                .limitRefreshPeriod(config.endpointsRateLimiter().limitRefreshPeriod())
                .build();

        registry = RateLimiterRegistry.of(rateLimiterConfig);
    }

    public String startEndpoint(String ip, Supplier<String> sup) {
        return getRateLimiter(ip, "start").executeSupplier(sup);
    }

    public String deleteEndpoint(String ip, Supplier<String> sup) {
        return getRateLimiter(ip, "delete").executeSupplier(sup);
    }

    public ListLinksResponse listEndpoint(String ip, Supplier<ListLinksResponse> sup) {
        return getRateLimiter(ip, "list").executeSupplier(sup);
    }

    public LinkResponse trackEndpoint(String ip, Supplier<LinkResponse> sup) {
        return getRateLimiter(ip, "track").executeSupplier(sup);
    }

    public LinkResponse untrackEndpoint(String ip, Supplier<LinkResponse> sup) {
        return getRateLimiter(ip, "untrack").executeSupplier(sup);
    }

    public TagsResponse tagsEndpoint(String ip, Supplier<TagsResponse> sup) {
        return getRateLimiter(ip, "tags").executeSupplier(sup);
    }

    public LinksByTagResponse getByTagEndpoint(String ip, Supplier<LinksByTagResponse> sup) {
        return getRateLimiter(ip, "getByTag").executeSupplier(sup);
    }

    public RemoveLinksByTagResponse deleteByTagEndpoint(String ip, Supplier<RemoveLinksByTagResponse> sup) {
        return getRateLimiter(ip, "deleteByTag").executeSupplier(sup);
    }

    public String deleteTimeEndpoint(String ip, Supplier<String> sup) {
        return getRateLimiter(ip, "deleteTime").executeSupplier(sup);
    }

    public String updateTimeEndpoint(String ip, Supplier<String> sup) {
        return getRateLimiter(ip, "updateTime").executeSupplier(sup);
    }

    private RateLimiter getRateLimiter(String ip, String nameEndpoint) {
        final String key = String.format("ip:%s.%s", ip, nameEndpoint);
        ipRateLimiters.computeIfAbsent(key, registry::rateLimiter);
        return ipRateLimiters.get(key);
    }
}
