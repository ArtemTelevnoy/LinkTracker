package backend.academy.scrapper.repositories.user;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "SQL")
public class JdbcUserRepository implements UserRepository {
    private final JdbcClient client;
    private final ReentrantReadWriteLock usersLock = new ReentrantReadWriteLock();

    @Override
    public void add(long userId) {
        usersLock.writeLock().lock();

        try {
            client.sql("INSERT INTO users (user_id) VALUES (:user_id) " + "ON CONFLICT(user_id) DO NOTHING")
                    .param("user_id", userId)
                    .update();
        } finally {
            usersLock.writeLock().unlock();
        }
    }

    @Override
    public boolean exist(long userId) {
        usersLock.readLock().lock();

        try {
            return !client.sql("SELECT user_id FROM users WHERE user_id = :user_id")
                    .param("user_id", userId)
                    .query(Long.class)
                    .list()
                    .isEmpty();
        } finally {
            usersLock.readLock().unlock();
        }
    }

    @Override
    public List<Long> getAllUsers() {
        usersLock.readLock().lock();

        try {
            return client.sql("SELECT user_id FROM users").query(Long.class).list();
        } finally {
            usersLock.readLock().unlock();
        }
    }

    @Override
    public void delete(long userId) {
        usersLock.writeLock().lock();

        try {
            client.sql("DELETE FROM users WHERE user_id = :user_id")
                    .param("user_id", userId)
                    .update();
        } finally {
            usersLock.writeLock().unlock();
        }
    }
}
