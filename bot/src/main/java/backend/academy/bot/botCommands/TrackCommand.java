package backend.academy.bot.botCommands;

import static backend.academy.bot.botCommands.CommandUtils.linkResponseToString;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.redis.RedisService;
import backend.academy.bot.repositories.LinkRepository;
import backend.academy.bot.repositories.TagsRepository;
import backend.academy.bot.stateMachine.State;
import backend.academy.bot.stateMachine.StateMachine;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class TrackCommand implements Command {
    private final TagsRepository tags;
    private final LinkRepository links;
    private final RedisService redisListService;
    private final RedisService redisTagsService;
    private final RedisService redisGetByTagService;

    @Getter
    private final String name = "/track";

    @Getter
    private final String hint = "add link for tracking";

    @Override
    public String work(String message, long id, StateMachine stateMachine, ScrapperClient client) {
        if (name.equals(message)) {
            if (stateMachine.isNotRegistered(id)) {
                log.warn("User {} can't track link on this state", id);
                return "You can't track link on this state";
            }

            log.info("User {} want tracking some link", id);
            stateMachine.put(id, State.ENTER_TRACK_LINK);
            return "enter link";
        } else if (stateMachine.toEnterTags(id)) {
            log.info("User {} entered link", id);
            stateMachine.put(id, State.ENTER_TAGS);
            links.put(id, message);
            return "enter tags (optional, enter \"-\" else)";
        } else if (stateMachine.toEnterFilters(id)) {
            log.info("User {} entered tags", id);
            tags.put(id, parseArr(message));
            stateMachine.put(id, State.ENTER_FILTERS);
            return "enter filters for ignoring some user's updates: user=... user=... (optional, enter \"-\" else)";
        } else if (!stateMachine.toTrack(id)) {
            return null;
        }

        log.info("User {} entered filters", id);
        stateMachine.put(id, State.REGISTERED);
        log.info("User {} is trying to track link {}", id, message);
        redisListService.delete(id);
        redisTagsService.delete(id);
        redisGetByTagService.delete(id);
        return linkResponseToString(client.getTrackResponse(id, links.get(id), tags.get(id), parseFiltersArr(message)));
    }

    private static String[] parseArr(String message) {
        return "-".equals(message) ? new String[0] : message.trim().split(" +");
    }

    private static String[] parseFiltersArr(String message) {
        return Arrays.stream(parseArr(message))
                .filter(o -> o.startsWith("user="))
                .map(o -> o.replaceFirst("^user=", ""))
                .toArray(String[]::new);
    }
}
