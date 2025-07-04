package backend.academy.scrapper.repositories.filter;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "SQL")
public class JdbcFilterRepository implements FilterRepository {
    private final JdbcClient client;
    private final ReentrantReadWriteLock filtersLock = new ReentrantReadWriteLock();

    @Override
    public void add(long userId, long linkId, String[] filters) {
        filtersLock.writeLock().lock();

        try {
            for (String filterName : filters) {
                client.sql(
                                "INSERT INTO filters (user_id, link_id, filter_name) VALUES (:user_id, :link_id, :filter_name) "
                                        + "ON CONFLICT (user_id, link_id, filter_name) DO NOTHING")
                        .param("user_id", userId)
                        .param("link_id", linkId)
                        .param("filter_name", filterName)
                        .update();
            }
        } finally {
            filtersLock.writeLock().unlock();
        }
    }

    @Override
    public String[] get(long userId, long linkId) {
        filtersLock.readLock().lock();

        try {
            return client.sql("SELECT filter_name FROM filters WHERE user_id = :user_id AND link_id = :link_id")
                    .param("user_id", userId)
                    .param("link_id", linkId)
                    .query(String.class)
                    .list()
                    .toArray(String[]::new);
        } finally {
            filtersLock.readLock().unlock();
        }
    }

    @Override
    public void delete(long userId, long linkId) {
        filtersLock.writeLock().lock();

        try {
            client.sql("DELETE FROM filters WHERE user_id = :user_id AND link_id = :link_id")
                    .param("user_id", userId)
                    .param("link_id", linkId)
                    .update();
        } finally {
            filtersLock.writeLock().unlock();
        }
    }

    @Override
    public void deleteAllUserData(long userId) {
        filtersLock.writeLock().lock();

        try {
            client.sql("DELETE FROM filters WHERE user_id = :user_id")
                    .param("user_id", userId)
                    .update();
        } finally {
            filtersLock.writeLock().unlock();
        }
    }
}
