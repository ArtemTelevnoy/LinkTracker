package backend.academy.bot.service;

import backend.academy.bot.botCommands.*;
import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.configs.BotConfig;
import backend.academy.bot.exceptions.BotException;
import backend.academy.bot.metrics.UsersMessageCountMetric;
import backend.academy.bot.stateMachine.StateMachine;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableScheduling
public class Bot {
    private final TelegramBot bot;
    private final StateMachine stateMachine;
    private final ScrapperClient scrapperClient;
    private final List<Command> commands;
    private final UsersMessageCountMetric usersMessageCountMetric;

    @Autowired
    public Bot(
            BotConfig botConfig,
            ScrapperClient scrapperClient,
            StateMachine stateMachine,
            List<Command> commands,
            UsersMessageCountMetric usersMessageCountMetric) {
        this.stateMachine = stateMachine;
        this.commands = commands;
        this.scrapperClient = scrapperClient;
        this.usersMessageCountMetric = usersMessageCountMetric;
        this.bot = new TelegramBot(botConfig.telegramToken());

        bot.execute(new SetMyCommands(commands.stream()
                .map(command -> new BotCommand(command.name(), command.hint()))
                .toArray(BotCommand[]::new)));
        log.info("Telegram bot was created");
    }

    @PostConstruct
    public void start() {
        bot.setUpdatesListener(
                updates -> {
                    updates.forEach(update -> {
                        usersMessageCountMetric.messageCounter().incrementAndGet();
                        final long id = update.message().chat().id();
                        log.info("Starting handle message from user with id: {}", id);
                        stateMachine.addId(id);
                        sendMessage(id, work(update.message().text(), id, stateMachine, scrapperClient, commands));
                        log.info("Ending handle message from user with id: {}", id);
                    });

                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                },
                e -> {
                    if (e.response() != null) {
                        log.error(
                                "Error code: {}, Description: {}",
                                e.response().errorCode(),
                                e.response().description());
                    } else {
                        log.error("Error: {}", e.getMessage());
                    }
                });
    }

    public static String work(
            String message,
            long id,
            StateMachine stateMachine,
            ScrapperClient scrapperClient,
            @NotNull List<Command> commands) {
        try {
            return commands.stream()
                    .map(o -> o.work(message, id, stateMachine, scrapperClient))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse("Unknown command");
        } catch (BotException e) {
            return String.format("Something went wrong: %s", e.getMessage());
        }
    }

    public void sendMessage(long id, String message) {
        bot.execute(new SendMessage(id, message));
    }
}
