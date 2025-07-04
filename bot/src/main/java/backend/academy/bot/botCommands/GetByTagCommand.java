package backend.academy.bot.botCommands;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.redis.RedisService;
import backend.academy.bot.stateMachine.State;
import backend.academy.bot.stateMachine.StateMachine;
import backend.academy.dto.tags.LinksByTagResponse;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class GetByTagCommand implements Command {
    private final RedisService redisGetByTagService;

    @Getter
    private final String name = "/getByTag";

    @Getter
    private final String hint = "get all links by tag";

    @Override
    public String work(String message, long id, StateMachine stateMachine, ScrapperClient client) {
        if (name.equals(message)) {
            if (stateMachine.isNotRegistered(id)) {
                log.warn("User {} is trying getting links by tag before registration", id);
                return "You must register before getting";
            }

            log.info("User {} want getting links by tag", id);
            stateMachine.put(id, State.ENTER_TAG_FOR_FINDING);
            return "enter tag";
        } else if (!stateMachine.toGetByTag(id)) {
            return null;
        }

        stateMachine.put(id, State.REGISTERED);
        log.info("User {} is trying to get links by tag {}", id, message);
        String response = redisGetByTagService.get(id);
        if (response == null) {
            response = responseToString(client.getByTagResponse(id, message), message);
            redisGetByTagService.save(id, response);
        }

        return response;
    }

    private static String responseToString(LinksByTagResponse response, String tag) {
        if (response == null) {
            log.warn("Null body of links by tag response");
            return "Null body of links by tag response";
        } else if (response.size() == 0) {
            return "You don't have any links by this tag";
        }

        return String.format("Links by tag=%s: %s", tag, Arrays.toString(response.urls()));
    }
}
