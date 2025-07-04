package backend.academy.bot.redis;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Getter
@Service
@Qualifier("redisGetByTagService")
public class RedisGetByTagService extends AbstractRedisService {
    private final String cachePrefix = "getByTag:";

    @Autowired
    public RedisGetByTagService(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }
}
