package backend.academy.bot.botCommands;

import backend.academy.dto.links.LinkResponse;
import java.util.Arrays;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
class CommandUtils {
    static String stringArray(String[] arr) {
        return arr.length == 0 ? "No any items" : Arrays.toString(arr);
    }

    static String linkResponseToString(@NotNull LinkResponse linkResponse) {
        return String.format(
                "Tracking: url=%s, tags=%s, filters=%s",
                linkResponse.url(), stringArray(linkResponse.tags()), stringArray(linkResponse.filters()));
    }
}
