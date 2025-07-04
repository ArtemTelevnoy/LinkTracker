package backend.academy.scrapper.postgresTests.tagsTests;

import backend.academy.scrapper.postgresTests.PostgresContainer;
import backend.academy.scrapper.repositories.tag.TagRepository;
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
abstract class AbstractTagRepositoryTests extends PostgresContainer {
    private final TagRepository repository;

    @Test
    @DisplayName("tag repository: add")
    void test1() {
        // Arrange
        final String[] tags1 = new String[] {"tag1", "tag2"};
        final String[] tags2 = new String[] {"tag4", "tag3", "tag2"};

        // Act
        repository.add(1, 10, tags1);
        final String[] user1Tags = repository.get(1, 10);

        boolean isThrowOnDuplicateValues;
        try {
            repository.add(1, 10, tags1);
            isThrowOnDuplicateValues = false;
        } catch (Exception ignored) {
            isThrowOnDuplicateValues = true;
        }

        repository.add(2, 20, tags2);
        final String[] user2Tags = repository.get(2, 20);

        // Assert
        Assertions.assertEquals(set(tags1), set(user1Tags));
        Assertions.assertEquals(set(tags2), set(user2Tags));
        Assertions.assertFalse(isThrowOnDuplicateValues);
    }

    @Test
    @DisplayName("tag repository: delete")
    void test2() {
        // Arrange
        final String[] tags2 = new String[] {"tag4", "tag3", "tag2"};

        // Act
        repository.delete(1, 10);
        final String[] user1Tags = repository.get(1, 10);
        final String[] user2TagsStillExist = repository.get(2, 20);

        repository.delete(2, 20);
        final String[] user2Tags = repository.get(2, 20);

        // Assert
        Assertions.assertEquals(0, user1Tags.length);
        Assertions.assertEquals(set(tags2), set(user2TagsStillExist));
        Assertions.assertEquals(0, user2Tags.length);
    }

    @Test
    @DisplayName("tag repository: getUserTags")
    void test3() {
        // Arrange
        final String[] tags1 = new String[] {"home", "relax"};
        final String[] tags2 = new String[] {"work", "study"};
        final String[] tags3 = new String[] {"work"};

        // Act
        repository.add(1, 1, tags1);
        repository.add(1, 2, tags2);
        repository.add(1, 3, tags3);

        final String[] userTags = repository.getUserTags(1);
        final Set<String> allTags = set(tags1);
        allTags.addAll(List.of(tags2));
        allTags.addAll(List.of(tags3));

        // Assert
        Assertions.assertEquals(allTags, set(userTags));
    }

    @Test
    @DisplayName("tag repository: getLinksIds")
    void test4() {
        // Arrange

        // Act
        final List<Long> workLinks = repository.getLinksIds(1, "work");

        // Assert
        Assertions.assertEquals(set(new Long[] {2L, 3L}), new HashSet<>(workLinks));
    }

    @Test
    @DisplayName("tag repository: deleteByTag")
    void test5() {
        // Arrange

        // Act
        final List<Long> removedWorkLinks = repository.deleteByTag(1, "work");
        final int countWorkLinks = repository.getLinksIds(1, "work").size();

        // Assert
        Assertions.assertEquals(set(new Long[] {2L, 3L}), new HashSet<>(removedWorkLinks));
        Assertions.assertEquals(0, countWorkLinks);
    }

    @Test
    @DisplayName("tag repository: deleteAllUserData")
    void test6() {
        // Arrange
        final String[] tags1 = new String[] {"home", "relax"};
        final String[] tags2 = new String[] {"work", "study"};
        final String[] tags3 = new String[] {"work"};

        // Act
        repository.add(1, 10, tags1);
        repository.add(1, 20, tags2);
        repository.add(2, 20, tags3);

        repository.deleteAllUserData(1);
        final String[] user1Filters1 = repository.get(1, 10);
        final String[] user1Filters2 = repository.get(1, 20);
        final String[] user2Filters3StillExist = repository.get(2, 20);

        // Assert
        Assertions.assertEquals(0, user1Filters1.length);
        Assertions.assertEquals(0, user1Filters2.length);
        Assertions.assertEquals(set(tags3), set(user2Filters3StillExist));
    }

    private static <T> Set<T> set(T[] arr) {
        return new HashSet<>(List.of(arr));
    }
}
