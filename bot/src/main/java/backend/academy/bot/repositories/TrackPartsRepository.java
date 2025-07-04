package backend.academy.bot.repositories;

public interface TrackPartsRepository<T> {
    T get(long id);

    void put(long id, T t);
}
