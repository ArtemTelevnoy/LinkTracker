package backend.academy.scrapper.repositories.user;

import java.util.List;

public interface UserRepository {
    void add(long userId);

    boolean exist(long userId);

    List<Long> getAllUsers();

    void delete(long userId);
}
