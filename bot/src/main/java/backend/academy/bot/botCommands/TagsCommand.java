package backend.academy.bot.botCommands;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.redis.RedisService;
import backend.academy.bot.stateMachine.StateMachine;
import backend.academy.dto.tags.TagsResponse;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class TagsCommand implements Command {
    private final RedisService redisTagsService;

    @Getter
    private final String name = "/tags";

    @Getter
    private final String hint = "get all tags";

    @Override
    public String work(String message, long id, StateMachine stateMachine, ScrapperClient client) {
        if (!name.equals(message)) {
            return null;
        } else if (stateMachine.isNotRegistered(id)) {
            log.warn("User {} is trying compute \"/tags\" on incorrect state", id);
            return "You can't getting tags on this state";
        }

        log.info("User {} is trying to get list of tags", id);

        String response = redisTagsService.get(id);
        if (response == null) {
            response = responseToString(client.getTagsResponse(id));
            redisTagsService.save(id, response);
        }

        return response;
    }

    private static String responseToString(TagsResponse response) {
        if (response == null) {
            log.warn("Null body of tags response");
            return "Null body of tags response";
        } else if (response.size() == 0) {
            return "You don't have any tags";
        }

        return String.format("Tags: %s", Arrays.toString(response.tags()));
    }
}
