package backend.academy.bot.botCommands;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.stateMachine.StateMachine;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Component
public class HelpCommand implements Command {
    private final String name = "/help";
    private final String hint = "show hints";

    @Override
    public String work(String message, long id, StateMachine stateMachine, ScrapperClient client) {
        if (name.equals(message)) {
            log.info("User {} wants a hint", id);
            return "You can see all hints in dialogue";
        }

        return null;
    }
}
