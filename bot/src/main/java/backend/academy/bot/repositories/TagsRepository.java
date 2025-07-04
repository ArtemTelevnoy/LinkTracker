package backend.academy.bot.repositories;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class TagsRepository implements TrackPartsRepository<String[]> {
    private final Map<Long, String[]> tags = new HashMap<>();

    public String[] get(long id) {
        return tags.get(id);
    }

    public void put(long id, String[] tags) {
        this.tags.put(id, tags);
    }
}
