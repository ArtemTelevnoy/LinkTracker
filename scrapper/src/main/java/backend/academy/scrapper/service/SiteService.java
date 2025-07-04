package backend.academy.scrapper.service;

import backend.academy.scrapper.link.LinkBody;
import backend.academy.scrapper.link.ListLinkUpdatesInfo;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public interface SiteService {
    Instant getLastUpdateOrNow(String url);

    ListLinkUpdatesInfo getLinkUpdate(@NotNull LinkBody link);

    default String previewPart(@NotNull String preview) {
        return preview.length() < 200 ? preview : preview.substring(0, 200);
    }
}
