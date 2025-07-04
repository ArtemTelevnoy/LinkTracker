package backend.academy.bot.stateMachine;

import static backend.academy.bot.stateMachine.State.*;

import backend.academy.bot.repositories.StateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StateMachine {
    private final StateRepository states;

    public void addId(long id) {
        states.create(id);
    }

    public void put(long id, State state) {
        states.put(id, state);
    }

    public boolean toRegistered(long id) {
        return states.get(id) == DEFAULT;
    }

    public boolean isNotRegistered(long id) {
        return states.get(id) != REGISTERED;
    }

    public boolean toEnterTags(long id) {
        return states.get(id) == ENTER_TRACK_LINK;
    }

    public boolean toEnterFilters(long id) {
        return states.get(id) == ENTER_TAGS;
    }

    public boolean toTrack(long id) {
        return states.get(id) == ENTER_FILTERS;
    }

    public boolean toUntrack(long id) {
        return states.get(id) == ENTER_UNTRACK_LINK;
    }

    public boolean toGetByTag(long id) {
        return states.get(id) == ENTER_TAG_FOR_FINDING;
    }

    public boolean toRemoveByTag(long id) {
        return states.get(id) == ENTER_TAG_FOR_REMOVING;
    }

    public boolean toChangeTime(long id) {
        return states.get(id) == ENTER_TIME;
    }
}
