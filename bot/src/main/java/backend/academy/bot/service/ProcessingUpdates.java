package backend.academy.bot.service;

import backend.academy.dto.links.LinkUpdate;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@AllArgsConstructor
public class ProcessingUpdates {
    private final Bot bot;

    public void updates(@Valid @NotNull LinkUpdate update) {
        log.info("User {} have some updates", update.userId());
        bot.sendMessage(update.userId(), String.format("Some updates:%s", update.description()));
    }
}
