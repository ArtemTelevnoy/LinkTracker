package backend.academy.bot.botCommands;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.redis.RedisService;
import backend.academy.bot.stateMachine.State;
import backend.academy.bot.stateMachine.StateMachine;
import backend.academy.dto.tags.RemoveLinksByTagResponse;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class DeleteByTagCommand implements Command {
    private final RedisService redisListService;
    private final RedisService redisTagsService;
    private final RedisService redisGetByTagService;

    @Getter
    private final String name = "/deleteByTag";

    @Getter
    private final String hint = "removes links by tag";

    @Override
    public String work(String message, long id, StateMachine stateMachine, ScrapperClient client) {
        if (name.equals(message)) {
            if (stateMachine.isNotRegistered(id)) {
                log.warn("User {} is trying removing links by tag before registration", id);
                return "You must register before removing";
            }

            log.info("User {} want removing links by tag", id);
            stateMachine.put(id, State.ENTER_TAG_FOR_REMOVING);
            return "enter tag";
        } else if (!stateMachine.toRemoveByTag(id)) {
            return null;
        }

        stateMachine.put(id, State.REGISTERED);
        log.info("User {} is trying to remove links by tag {}", id, message);

        redisListService.delete(id);
        redisTagsService.delete(id);
        redisGetByTagService.delete(id);
        final RemoveLinksByTagResponse response = client.removeByTagResponse(id, message);

        if (response == null) {
            log.warn("Null body of links response");
            return "Null body of links response";
        } else if (response.size() == 0) {
            return "You don't have any links by this tag";
        }

        return String.format("Removing links by tag=%s: %s", message, Arrays.toString(response.urls()));
    }
}
