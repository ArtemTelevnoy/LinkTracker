package backend.academy.bot.redis;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Getter
@Service
@Qualifier("redisListService")
public class RedisListService extends AbstractRedisService {
    private final String cachePrefix = "list:";

    @Autowired
    public RedisListService(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }
}
