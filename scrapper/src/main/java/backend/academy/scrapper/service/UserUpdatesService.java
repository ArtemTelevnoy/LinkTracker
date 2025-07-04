package backend.academy.scrapper.service;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserUpdatesService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final String cachePrefix = "updates:";

    @SuppressWarnings("ConstantConditions")
    public void update(long userId, String newUpdates) {
        lock.writeLock().lock();

        try {
            String response = getNoLock(userId);
            if (response == null) { // this suppressed. Redis return null if no key
                response = "";
            }

            redisTemplate.opsForValue().set(cachePrefix + userId, response + newUpdates);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String get(long userId) {
        lock.readLock().lock();

        try {
            return getNoLock(userId);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void delete(long userId) {
        lock.writeLock().lock();

        try {
            redisTemplate.delete(cachePrefix + userId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private String getNoLock(long userId) {
        return redisTemplate.opsForValue().get(cachePrefix + userId);
    }
}
