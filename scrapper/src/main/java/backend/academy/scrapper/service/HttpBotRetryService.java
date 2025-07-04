package backend.academy.scrapper.service;

import backend.academy.dto.links.LinkUpdate;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HttpBotRetryService {
    @Retry(name = "webClientRetry")
    public void sendUpdatesWitRetry(@NotNull Consumer<LinkUpdate> sup, LinkUpdate linkUpdate) {
        sup.accept(linkUpdate);
    }
}
