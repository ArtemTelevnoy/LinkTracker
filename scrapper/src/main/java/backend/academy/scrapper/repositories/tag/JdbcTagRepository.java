package backend.academy.scrapper.repositories.tag;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "SQL")
public class JdbcTagRepository implements TagRepository {
    private final JdbcClient client;
    private final ReentrantReadWriteLock tagsLock = new ReentrantReadWriteLock();

    @Override
    public void add(long userId, long linkId, String[] tags) {
        tagsLock.writeLock().lock();

        try {
            for (String tag : tags) {
                client.sql("INSERT INTO tags (user_id, tag_name, link_id) VALUES (:user_id, :tag_name, :link_id) "
                                + "ON CONFLICT (user_id, tag_name, link_id) DO NOTHING")
                        .param("user_id", userId)
                        .param("tag_name", tag)
                        .param("link_id", linkId)
                        .update();
            }
        } finally {
            tagsLock.writeLock().unlock();
        }
    }

    @Override
    public String[] get(long userId, long linkId) {
        tagsLock.readLock().lock();

        try {
            return client.sql("SELECT tag_name FROM tags WHERE user_id = :user_id AND link_id = :link_id")
                    .param("user_id", userId)
                    .param("link_id", linkId)
                    .query(String.class)
                    .list()
                    .toArray(String[]::new);
        } finally {
            tagsLock.readLock().unlock();
        }
    }

    @Override
    public String[] getUserTags(long userId) {
        tagsLock.readLock().lock();

        try {
            return client.sql("SELECT tag_name FROM tags WHERE user_id = :user_id")
                    .param("user_id", userId)
                    .query(String.class)
                    .list()
                    .toArray(String[]::new);
        } finally {
            tagsLock.readLock().unlock();
        }
    }

    @Override
    public List<Long> getLinksIds(long userId, String tagName) {
        tagsLock.readLock().lock();

        try {
            return client.sql("SELECT link_id FROM tags WHERE user_id = :user_id AND tag_name = :tag_name")
                    .param("user_id", userId)
                    .param("tag_name", tagName)
                    .query(Long.class)
                    .list();
        } finally {
            tagsLock.readLock().unlock();
        }
    }

    @Override
    public void delete(long userId, long linkId) {
        tagsLock.writeLock().lock();

        try {
            client.sql("DELETE FROM tags WHERE user_id = :user_id AND link_id = :link_id")
                    .param("user_id", userId)
                    .param("link_id", linkId)
                    .update();
        } finally {
            tagsLock.writeLock().unlock();
        }
    }

    @Override
    public void deleteAllUserData(long userId) {
        tagsLock.writeLock().lock();

        try {
            client.sql("DELETE FROM tags WHERE user_id = :user_id")
                    .param("user_id", userId)
                    .update();
        } finally {
            tagsLock.writeLock().unlock();
        }
    }

    @Override
    public List<Long> deleteByTag(long userId, String tagName) {
        tagsLock.writeLock().lock();

        try {
            return client.sql("DELETE FROM tags WHERE user_id = :user_id AND tag_name = :tag_name RETURNING link_id")
                    .param("user_id", userId)
                    .param("tag_name", tagName)
                    .query(Long.class)
                    .list();
        } finally {
            tagsLock.writeLock().unlock();
        }
    }
}
