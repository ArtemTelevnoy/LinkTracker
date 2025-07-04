package backend.academy.bot.repositories;

import backend.academy.bot.stateMachine.State;

public interface StateRepository {
    void create(long id);

    void put(long id, State state);

    State get(long id);
}
