package backend.academy.bot.botCommands;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.redis.RedisService;
import backend.academy.bot.stateMachine.State;
import backend.academy.bot.stateMachine.StateMachine;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class UnTrackCommand implements Command {
    private final RedisService redisListService;
    private final RedisService redisTagsService;
    private final RedisService redisGetByTagService;

    @Getter
    private final String name = "/untrack";

    @Getter
    private final String hint = "delete link from tracking links";

    @Override
    public String work(String message, long id, StateMachine stateMachine, ScrapperClient client) {
        if (name.equals(message)) {
            if (stateMachine.isNotRegistered(id)) {
                log.warn("User {} is trying untracking link before registration", id);
                return "You must register before untracking";
            }

            log.info("User {} want untracking some link", id);
            stateMachine.put(id, State.ENTER_UNTRACK_LINK);
            return "enter link";
        } else if (!stateMachine.toUntrack(id)) {
            return null;
        }

        stateMachine.put(id, State.REGISTERED);
        log.info("User {} is trying to untrack link {}", id, message);
        redisListService.delete(id);
        redisTagsService.delete(id);
        redisGetByTagService.delete(id);
        return CommandUtils.linkResponseToString(client.getUntrackResponse(id, message));
    }
}
