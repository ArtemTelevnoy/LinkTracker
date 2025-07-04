package backend.academy.scrapper.repositories.link;

import static backend.academy.scrapper.link.LinkType.*;

import backend.academy.scrapper.exceptions.NoSuchLinkException;
import backend.academy.scrapper.link.LinkBody;
import backend.academy.scrapper.link.LinkInfo;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "SQL")
public class JdbcLinkRepository implements LinkRepository {
    private final JdbcClient client;
    private final ReentrantReadWriteLock linksLock = new ReentrantReadWriteLock();

    @Override
    public long add(@NotNull LinkInfo linkInfo) {
        linksLock.writeLock().lock();

        try {
            final long linkId = getIdByUrl(linkInfo.url());
            if (linkId != -1) {
                return linkId;
            }

            return client.sql("INSERT INTO links (url, update_time, is_github) VALUES (:url, :update_time, :is_github) "
                            + "RETURNING link_id")
                    .param("url", linkInfo.url())
                    .param("update_time", Timestamp.from(linkInfo.updateTime()))
                    .param("is_github", linkInfo.isGithub())
                    .query(Long.class)
                    .single();
        } finally {
            linksLock.writeLock().unlock();
        }
    }

    @Override
    public LinkInfo get(String url) {
        linksLock.readLock().lock();

        try {
            return client.sql("SELECT link_id, url, update_time, is_github FROM links WHERE url = :url")
                    .param("url", url)
                    .query(LinkInfo.class)
                    .single();
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchLinkException(url, e);
        } finally {
            linksLock.readLock().unlock();
        }
    }

    private long getIdByUrl(String url) {
        try {
            return client.sql("SELECT link_id FROM links WHERE url = :url")
                    .param("url", url)
                    .query(Long.class)
                    .single();
        } catch (EmptyResultDataAccessException ignored) {
            return -1;
        }
    }

    @Override
    public String getUrl(long linkId) {
        linksLock.readLock().lock();

        try {
            return client.sql("SELECT url FROM links WHERE link_id = :link_id")
                    .param("link_id", linkId)
                    .query(String.class)
                    .single();
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchLinkException(linkId, e);
        } finally {
            linksLock.readLock().unlock();
        }
    }

    @Override
    public long getId(String url) {
        return LinkRepositoryUtils.getId(linksLock, this::getIdByUrl, url);
    }

    @Override
    public List<LinkBody> getLinksForUpdates(int batchSize, int skipCount) {
        linksLock.readLock().lock();

        try {
            return client.sql("SELECT * FROM links ORDER BY link_id LIMIT :batch_size OFFSET :skip_count")
                    .param("batch_size", batchSize)
                    .param("skip_count", skipCount)
                    .query((rs, ignored) -> new LinkBody(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getTimestamp(3).toInstant(),
                            rs.getBoolean(4) ? GITHUB : STACKOVERFLOW))
                    .list();
        } finally {
            linksLock.readLock().unlock();
        }
    }

    @Override
    public void updateLinkTime(long linkId, Instant updatedTime) {
        getUrl(linkId);
        linksLock.writeLock().lock();

        try {
            client.sql("UPDATE links SET update_time = :update_time WHERE link_id = :link_id")
                    .param("update_time", Timestamp.from(updatedTime))
                    .param("link_id", linkId)
                    .update();
        } finally {
            linksLock.writeLock().unlock();
        }
    }

    @Override
    public int countActiveGithubLinks() {
        return countActiveLinks(true);
    }

    @Override
    public int countActiveStackLinks() {
        return countActiveLinks(false);
    }

    private int countActiveLinks(boolean isGithub) {
        linksLock.readLock().lock();

        try {
            return client.sql("SELECT link_id FROM links WHERE is_github = :is_github")
                    .param("is_github", isGithub)
                    .query(Long.class)
                    .list()
                    .size();
        } finally {
            linksLock.readLock().unlock();
        }
    }
}
