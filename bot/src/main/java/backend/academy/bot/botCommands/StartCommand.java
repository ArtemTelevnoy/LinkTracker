package backend.academy.bot.botCommands;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.stateMachine.State;
import backend.academy.bot.stateMachine.StateMachine;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Component
public class StartCommand implements Command {
    private final String name = "/start";
    private final String hint = "starting your dialogue";

    @Override
    public String work(String message, long id, StateMachine stateMachine, ScrapperClient client) {
        if (!name.equals(message)) {
            return null;
        } else if (!stateMachine.toRegistered(id)) {
            log.warn("User {} is trying registering second time", id);
            return "Your id was already registered";
        }

        log.info("User {} is trying to register", id);
        final String res = client.getStartResponse(id);
        stateMachine.put(id, State.REGISTERED);
        log.info("User {} was successfully registered", id);
        return res;
    }
}
