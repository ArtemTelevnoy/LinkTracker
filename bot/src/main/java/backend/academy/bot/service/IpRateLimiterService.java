package backend.academy.bot.service;

import backend.academy.bot.configs.BotConfig;
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
    public IpRateLimiterService(BotConfig config) {
        final RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                .timeoutDuration(config.endpointsRateLimiter().timeoutDuration())
                .limitForPeriod(config.endpointsRateLimiter().limitForPeriod())
                .limitRefreshPeriod(config.endpointsRateLimiter().limitRefreshPeriod())
                .build();

        registry = RateLimiterRegistry.of(rateLimiterConfig);
    }

    public String updatesEndpoint(String ip, Supplier<String> sup) {
        return getRateLimiter(ip).executeSupplier(sup);
    }

    private RateLimiter getRateLimiter(String ip) {
        ipRateLimiters.computeIfAbsent(ip, registry::rateLimiter);
        return ipRateLimiters.get(ip);
    }
}
