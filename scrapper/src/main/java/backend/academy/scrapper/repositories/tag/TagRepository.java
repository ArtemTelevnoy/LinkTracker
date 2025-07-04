package backend.academy.scrapper.repositories.tag;

import java.util.List;

public interface TagRepository {
    void add(long userId, long linkId, String[] data);

    String[] get(long userId, long linkId);

    String[] getUserTags(long userId);

    List<Long> getLinksIds(long userId, String tagName);

    void delete(long userId, long linkId);

    void deleteAllUserData(long userId);

    List<Long> deleteByTag(long userId, String tagName);
}
