package backend.academy.bot.repositories;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class LinkRepository implements TrackPartsRepository<String> {
    private final Map<Long, String> link = new HashMap<>();

    public String get(long id) {
        return link.get(id);
    }

    public void put(long id, String link) {
        this.link.put(id, link);
    }
}
