package backend.academy.bot.repositories;

import backend.academy.bot.stateMachine.State;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class StateRepositoryImpl implements StateRepository {
    private final Map<Long, State> states = new HashMap<>();

    @Override
    public void create(long id) {
        states.putIfAbsent(id, State.DEFAULT);
    }

    @Override
    public void put(long id, State state) {
        states.put(id, state);
    }

    @Override
    public State get(long id) {
        return states.get(id);
    }
}
