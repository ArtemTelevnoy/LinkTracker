package backend.academy.bot.clients;

import backend.academy.bot.configs.BotConfig;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

@Component
public class ClientUtils {
    private final List<HttpStatusCode> retryableHttpStatuses;

    @Autowired
    public ClientUtils(BotConfig config) {
        retryableHttpStatuses =
                config.retryCodes().stream().map(HttpStatusCode::valueOf).toList();
    }

    boolean isSupportHttp(@NotNull HttpStatusCode statusCode) {
        return retryableHttpStatuses.contains(statusCode);
    }
}
