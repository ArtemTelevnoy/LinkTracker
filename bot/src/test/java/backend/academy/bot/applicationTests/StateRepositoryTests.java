package backend.academy.bot.applicationTests;

import backend.academy.bot.repositories.StateRepository;
import backend.academy.bot.repositories.StateRepositoryImpl;
import backend.academy.bot.stateMachine.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StateRepositoryTests {
    @Test
    @DisplayName("state repository: create, put, get")
    void test1() {
        // Arrange
        final StateRepository repository = new StateRepositoryImpl();
        final State state1 = State.ENTER_TAG_FOR_FINDING;
        final State state2 = State.ENTER_FILTERS;

        // Act
        repository.put(1, state1);
        repository.put(2, state2);
        repository.create(3);
        final State state1FromRepo = repository.get(1);
        final State state2FromRepo = repository.get(2);
        final State state3FromRepo = repository.get(3);

        // Assert
        Assertions.assertEquals(state1, state1FromRepo);
        Assertions.assertEquals(state2, state2FromRepo);
        Assertions.assertEquals(State.DEFAULT, state3FromRepo);
    }
}
