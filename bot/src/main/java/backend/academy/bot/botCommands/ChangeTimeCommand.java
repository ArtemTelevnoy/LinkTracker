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
public class ChangeTimeCommand implements Command {
    private final String name = "/setTime";
    private final String hint = "enter time in format \"hh:tt\" for taking notifications in this time "
            + "or \"im\" for sending notifications immediately(default=im)";

    @Override
    public String work(String message, long id, StateMachine stateMachine, ScrapperClient client) {
        if (name.equals(message)) {
            if (stateMachine.isNotRegistered(id)) {
                log.warn("User {} is trying setting time before registration", id);
                return "You must register before setting time";
            }

            log.info("User {} want changing time", id);
            stateMachine.put(id, State.ENTER_TIME);
            return "enter time(format hh:tt or im):";
        } else if (!stateMachine.toChangeTime(id)) {
            return null;
        }

        log.info("User {} is trying to changing time on {}", id, message);
        final String response = getResponse(id, message, client);
        stateMachine.put(id, State.REGISTERED);
        return response == null ? "Invalid time format" : response;
    }

    private static String getResponse(long id, String time, ScrapperClient client) {
        if ("im".equals(time)) {
            return client.getChangeTimeResponse(id);
        }

        final String[] splitTime = time.split(":");
        if (splitTime.length != 2) {
            return null;
        }

        final short hours;
        final short minutes;
        try {
            hours = Short.parseShort(splitTime[0]);
            minutes = Short.parseShort(splitTime[1]);
        } catch (NumberFormatException ignored) {
            return null;
        }

        return client.getChangeTimeResponse(id, hours, minutes);
    }
}
