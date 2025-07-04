package backend.academy.scrapper.repositories.userLink;

import backend.academy.scrapper.exceptions.DuplicateLinkException;
import backend.academy.scrapper.exceptions.NoSuchLinkException;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "SQL")
public class JdbcUserLinkRepository implements UserLinkRepository {
    private final JdbcClient client;
    private final ReentrantReadWriteLock userLinksLock = new ReentrantReadWriteLock();

    @Override
    public void add(long userId, long linkId) {
        userLinksLock.writeLock().lock();

        try {
            client.sql("INSERT INTO user_links (user_id, link_id) VALUES(:user_id, :link_id)")
                    .param("user_id", userId)
                    .param("link_id", linkId)
                    .update();
        } catch (DuplicateKeyException e) {
            throw new DuplicateLinkException(linkId, e);
        } finally {
            userLinksLock.writeLock().unlock();
        }
    }

    @Override
    public void delete(long userId, long linkId) {
        userLinksLock.writeLock().lock();

        try {
            final boolean isExist = client.sql(
                            "SELECT EXISTS (SELECT 1 FROM user_links WHERE user_id = :user_id AND link_id = :link_id)")
                    .param("user_id", userId)
                    .param("link_id", linkId)
                    .query(Boolean.class)
                    .single();

            if (!isExist) {
                throw new NoSuchLinkException(linkId);
            }

            client.sql("DELETE FROM user_links WHERE user_id = :user_id AND link_id = :link_id")
                    .param("user_id", userId)
                    .param("link_id", linkId)
                    .update();
        } finally {
            userLinksLock.writeLock().unlock();
        }
    }

    @Override
    public void deleteAllUserData(long userId) {
        userLinksLock.writeLock().lock();

        try {
            client.sql("DELETE FROM user_links WHERE user_id = :user_id")
                    .param("user_id", userId)
                    .update();
        } finally {
            userLinksLock.writeLock().unlock();
        }
    }

    @Override
    public long[] getChats(long linkId) {
        userLinksLock.readLock().lock();

        try {
            return client
                    .sql("SELECT user_id FROM user_links WHERE link_id = :link_id")
                    .param("link_id", linkId)
                    .query(Long.class)
                    .stream()
                    .mapToLong(Long::longValue)
                    .toArray();
        } finally {
            userLinksLock.readLock().unlock();
        }
    }

    @Override
    public List<Long> getUserLinksIds(long userId) {
        userLinksLock.readLock().lock();

        try {
            return client.sql("SELECT link_id FROM user_links WHERE user_id = :user_id")
                    .param("user_id", userId)
                    .query(Long.class)
                    .list();
        } finally {
            userLinksLock.readLock().unlock();
        }
    }
}
