package backend.academy.scrapper.repositories.filter;

public interface FilterRepository {
    void add(long userId, long linkId, String[] filters);

    String[] get(long userId, long linkId);

    void delete(long userId, long linkId);

    void deleteAllUserData(long userId);
}
