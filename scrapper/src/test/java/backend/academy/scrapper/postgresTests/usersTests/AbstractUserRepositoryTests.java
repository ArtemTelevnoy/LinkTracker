package backend.academy.scrapper.postgresTests.usersTests;

import backend.academy.scrapper.postgresTests.PostgresContainer;
import backend.academy.scrapper.repositories.user.UserRepository;
import java.util.HashSet;
import java.util.List;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AllArgsConstructor
abstract class AbstractUserRepositoryTests extends PostgresContainer {
    private final UserRepository repository;

    @Test
    @DisplayName("user repository: add")
    void test1() {
        // Arrange
        final long user1Id = 1;
        final long user2Id = 2;

        // Act
        repository.add(user1Id);
        final boolean isUser1Exist = repository.exist(user1Id);

        boolean isThrowOnDuplicateValues;
        try {
            repository.add(user1Id);
            isThrowOnDuplicateValues = false;
        } catch (Exception ignored) {
            isThrowOnDuplicateValues = true;
        }

        repository.add(user2Id);
        final boolean isUser2Exist = repository.exist(user2Id);

        // Assert
        Assertions.assertTrue(isUser1Exist);
        Assertions.assertTrue(isUser2Exist);
        Assertions.assertFalse(isThrowOnDuplicateValues);
    }

    @Test
    @DisplayName("user repository: getAllUsers")
    void test2() {
        // Arrange

        // Act
        final List<Long> allUsers = repository.getAllUsers();

        // Assert
        Assertions.assertEquals(new HashSet<>(List.of(1L, 2L)), new HashSet<>(allUsers));
    }

    @Test
    @DisplayName("user repository: delete")
    void test3() {
        // Arrange
        final long user1Id = 1;
        final long user2Id = 2;

        // Act
        repository.delete(user1Id);
        final boolean user1NotExist = repository.exist(user1Id);
        final boolean user2StillExist = repository.exist(user2Id);

        repository.delete(user2Id);
        final boolean user2NotExist = repository.exist(user2Id);

        // Assert
        Assertions.assertFalse(user1NotExist);
        Assertions.assertTrue(user2StillExist);
        Assertions.assertFalse(user2NotExist);
    }
}
