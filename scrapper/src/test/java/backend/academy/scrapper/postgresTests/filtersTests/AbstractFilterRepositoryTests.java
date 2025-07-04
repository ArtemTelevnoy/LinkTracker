package backend.academy.scrapper.postgresTests.filtersTests;

import backend.academy.scrapper.postgresTests.PostgresContainer;
import backend.academy.scrapper.repositories.filter.FilterRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AllArgsConstructor
abstract class AbstractFilterRepositoryTests extends PostgresContainer {
    private final FilterRepository repository;

    @Test
    @DisplayName("filter repository: add")
    void test1() {
        // Arrange
        final String[] filters1 = new String[] {"filter1", "filter2"};
        final String[] filters2 = new String[] {"filter4", "filter3", "filter2"};

        // Act
        repository.add(1, 10, filters1);
        final String[] user1Filters = repository.get(1, 10);

        boolean isThrowOnDuplicateValues;
        try {
            repository.add(1, 10, filters1);
            isThrowOnDuplicateValues = false;
        } catch (Exception ignored) {
            isThrowOnDuplicateValues = true;
        }

        repository.add(2, 20, filters2);
        final String[] user2Filters = repository.get(2, 20);

        // Assert
        Assertions.assertEquals(set(filters1), set(user1Filters));
        Assertions.assertEquals(set(filters2), set(user2Filters));
        Assertions.assertFalse(isThrowOnDuplicateValues);
    }

    @Test
    @DisplayName("filter repository: delete")
    void test2() {
        // Arrange
        final String[] filters2 = new String[] {"filter4", "filter3", "filter2"};

        // Act
        repository.delete(1, 10);
        final String[] user1Filters = repository.get(1, 10);
        final String[] user2FiltersStillExist = repository.get(2, 20);

        repository.delete(2, 20);
        final String[] user2Filters = repository.get(2, 20);

        // Assert
        Assertions.assertEquals(0, user1Filters.length);
        Assertions.assertEquals(set(filters2), set(user2FiltersStillExist));
        Assertions.assertEquals(0, user2Filters.length);
    }

    @Test
    @DisplayName("filter repository: deleteAllUserData")
    void test3() {
        // Arrange
        final String[] filters1 = new String[] {"filter1", "filter2"};
        final String[] filters2 = new String[] {"filter4", "filter3", "filter2"};
        final String[] filters3 = new String[] {"filter5", "filter6", "filter7"};

        // Act
        repository.add(1, 10, filters1);
        repository.add(1, 20, filters2);
        repository.add(2, 20, filters3);

        repository.deleteAllUserData(1);
        final String[] user1Filters1 = repository.get(1, 10);
        final String[] user1Filters2 = repository.get(1, 20);
        final String[] user2Filters3StillExist = repository.get(2, 20);

        // Assert
        Assertions.assertEquals(0, user1Filters1.length);
        Assertions.assertEquals(0, user1Filters2.length);
        Assertions.assertEquals(set(filters3), set(user2Filters3StillExist));
    }

    private static Set<String> set(String[] arr) {
        return new HashSet<>(List.of(arr));
    }
}
