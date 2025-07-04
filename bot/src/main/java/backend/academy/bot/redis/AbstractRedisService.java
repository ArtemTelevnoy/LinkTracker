package backend.academy.bot.redis;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

@AllArgsConstructor
abstract class AbstractRedisService implements RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void save(long id, String response) {
        redisTemplate.opsForValue().set(cachePrefix() + id, response);
    }

    @Override
    public String get(long id) {
        return redisTemplate.opsForValue().get(cachePrefix() + id);
    }

    @Override
    public void delete(long id) {
        redisTemplate.delete(cachePrefix() + id);
    }

    abstract String cachePrefix();
}
