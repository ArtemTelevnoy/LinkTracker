package backend.academy.scrapper.postgresTests.settingsTests;

import backend.academy.dto.chats.TimeBody;
import backend.academy.scrapper.postgresTests.PostgresContainer;
import backend.academy.scrapper.repositories.settings.SettingsRepository;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@AllArgsConstructor
abstract class AbstractSettingRepositoryTests extends PostgresContainer {
    private final SettingsRepository repository;

    @Test
    @DisplayName("settings repository: add")
    void test1() {
        // Arrange
        final TimeBody time1 = new TimeBody((short) 1, (short) 1);
        final TimeBody time2 = new TimeBody((short) 1, (short) 2);
        final TimeBody time3 = new TimeBody((short) 2, (short) 1);

        // Act
        repository.add(1, time1);
        repository.add(2, time2);
        final TimeBody timeFromRepo1 = repository.get(1);
        final TimeBody timeFromRepo2 = repository.get(2);

        repository.add(1, time3);
        final TimeBody timeFromRepo3 = repository.get(1);

        // Assert
        Assertions.assertEquals(time1, timeFromRepo1);
        Assertions.assertEquals(time2, timeFromRepo2);
        Assertions.assertEquals(time3, timeFromRepo3);
    }

    @Test
    @DisplayName("settings repository: delete")
    void test2() {
        // Arrange
        final TimeBody time2 = new TimeBody((short) 1, (short) 2);

        // Act
        repository.delete(1);
        final TimeBody timeFromRepo1 = repository.get(1);
        final TimeBody timeFromRepo2StillExist = repository.get(2);

        repository.delete(2);
        final TimeBody timeFromRepo2 = repository.get(2);

        // Assert
        Assertions.assertNull(timeFromRepo1);
        Assertions.assertEquals(time2, timeFromRepo2StillExist);
        Assertions.assertNull(timeFromRepo2);
    }
}
