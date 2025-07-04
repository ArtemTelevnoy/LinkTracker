package backend.academy.bot.redis;

public interface RedisService {
    void save(long id, String response);

    String get(long id);

    void delete(long id);
}
