package backend.academy.scrapper.repositories.userLink;

import java.util.List;

public interface UserLinkRepository {
    void add(long userId, long linkId);

    void delete(long userId, long linkId);

    void deleteAllUserData(long userId);

    long[] getChats(long linkId);

    List<Long> getUserLinksIds(long userId);
}
