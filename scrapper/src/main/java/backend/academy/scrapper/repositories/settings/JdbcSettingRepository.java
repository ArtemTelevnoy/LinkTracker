package backend.academy.scrapper.repositories.settings;

import backend.academy.dto.chats.TimeBody;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "SQL")
public class JdbcSettingRepository implements SettingsRepository {
    private final JdbcClient client;
    private final ReentrantReadWriteLock settingsLock = new ReentrantReadWriteLock();

    @Override
    public void add(long id, TimeBody time) {
        settingsLock.writeLock().lock();

        try {
            client.sql("INSERT INTO settings (user_id, hours_time, minutes_time) "
                            + "VALUES(:user_id, :hours_time, :minutes_time) "
                            + "ON CONFLICT(user_id) "
                            + "DO UPDATE SET hours_time = excluded.hours_time, minutes_time = excluded.minutes_time")
                    .param("user_id", id)
                    .param("hours_time", time.hours())
                    .param("minutes_time", time.minutes())
                    .update();
        } finally {
            settingsLock.writeLock().unlock();
        }
    }

    @Override
    public TimeBody get(long id) {
        settingsLock.readLock().lock();

        try {
            return client.sql("SELECT * FROM settings WHERE user_id = :user_id")
                    .param("user_id", id)
                    .query((rs, ignored) -> new TimeBody(rs.getShort(2), rs.getShort(3)))
                    .single();
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        } finally {
            settingsLock.readLock().unlock();
        }
    }

    @Override
    public void delete(long id) {
        settingsLock.writeLock().lock();

        try {
            client.sql("DELETE FROM settings WHERE user_id = :user_id")
                    .param("user_id", id)
                    .update();
        } finally {
            settingsLock.writeLock().unlock();
        }
    }
}
