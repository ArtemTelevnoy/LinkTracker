package backend.academy.scrapper.repositories.link;

import backend.academy.scrapper.link.LinkBody;
import backend.academy.scrapper.link.LinkInfo;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public interface LinkRepository {
    long add(@NotNull LinkInfo linkInfo);

    LinkInfo get(String url);

    String getUrl(long linkId);

    long getId(String url);

    List<LinkBody> getLinksForUpdates(int batchSize, int skipCount);

    void updateLinkTime(long linkId, Instant updatedTime);

    int countActiveGithubLinks();

    int countActiveStackLinks();
}
