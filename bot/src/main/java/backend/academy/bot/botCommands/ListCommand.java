package backend.academy.bot.botCommands;

import static backend.academy.bot.botCommands.CommandUtils.stringArray;
import static java.lang.String.format;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.redis.RedisService;
import backend.academy.bot.stateMachine.StateMachine;
import backend.academy.dto.links.ListLinksResponse;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class ListCommand implements Command {
    private final RedisService redisListService;

    @Getter
    private final String name = "/list";

    @Getter
    private final String hint = "show list of tracking links";

    @Override
    public String work(String message, long id, StateMachine stateMachine, ScrapperClient client) {
        if (!name.equals(message)) {
            return null;
        } else if (stateMachine.isNotRegistered(id)) {
            log.warn("User {} is trying compute \"/list\" on incorrect state", id);
            return "You can't getting links on this state";
        }

        log.info("User {} is trying to get list of links", id);
        String response = redisListService.get(id);
        if (response == null) {
            response = arrayToString(client.getListResponse(id));
            redisListService.save(id, response);
        }

        return response;
    }

    private static String arrayToString(ListLinksResponse body) {
        if (body == null) {
            log.warn("Null body of list response");
            return "Null body of list response";
        } else if (body.size() == 0) {
            return "You don't track any links";
        }

        return format(
                "Your links:%n%s",
                Arrays.stream(body.links())
                        .map(link -> format(
                                "link=%s, tags=%s, filters=%s",
                                link.url(), stringArray(link.tags()), stringArray(link.filters())))
                        .collect(Collectors.joining(format(";%n"))));
    }
}
