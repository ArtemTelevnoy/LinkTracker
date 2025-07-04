package backend.academy.scrapper.postgresTests.linksTests;

import backend.academy.scrapper.exceptions.NoSuchLinkException;
import backend.academy.scrapper.link.LinkBody;
import backend.academy.scrapper.link.LinkInfo;
import backend.academy.scrapper.link.LinkType;
import backend.academy.scrapper.postgresTests.PostgresContainer;
import backend.academy.scrapper.repositories.link.LinkRepository;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AllArgsConstructor
abstract class AbstractLinkRepositoryTests extends PostgresContainer {
    private final LinkRepository repository;

    @Test
    @DisplayName("link repository: add")
    void test1() {
        // Arrange
        final LinkInfo linkInfo1 = new LinkInfo("url1", Instant.parse("2025-10-01T10:15:30Z"), true);
        final LinkInfo linkInfo2 = new LinkInfo("url2", Instant.parse("2025-11-01T10:15:30Z"), false);

        // Act
        boolean isThrowWhenNoLink;
        try {
            repository.get(linkInfo1.url());
            isThrowWhenNoLink = false;
        } catch (NoSuchLinkException ignored) {
            isThrowWhenNoLink = true;
        } catch (Exception ignored) {
            isThrowWhenNoLink = false;
        }

        repository.add(linkInfo1);
        final LinkInfo linkInfo1FromRepo = repository.get(linkInfo1.url());

        boolean isThrowOnDuplicateValues;
        try {
            repository.add(linkInfo1);
            isThrowOnDuplicateValues = false;
        } catch (Exception ignored) {
            isThrowOnDuplicateValues = true;
        }

        repository.add(linkInfo2);
        final LinkInfo linkInfo2FromRepo = repository.get(linkInfo2.url());

        // Assert
        Assertions.assertTrue(isThrowWhenNoLink);
        Assertions.assertEquals(linkInfo1, linkInfo1FromRepo);
        Assertions.assertEquals(linkInfo2, linkInfo2FromRepo);
        Assertions.assertFalse(isThrowOnDuplicateValues);
    }

    @Test
    @DisplayName("link repository: getId")
    void test2() {
        // Arrange

        // Act
        final long link1Id = repository.getId("url1");
        final long link2Id = repository.getId("url2");

        boolean isThrowWhenNoLink;
        try {
            repository.getId("not exist url");
            isThrowWhenNoLink = false;
        } catch (NoSuchLinkException ignored) {
            isThrowWhenNoLink = true;
        } catch (Exception ignored) {
            isThrowWhenNoLink = false;
        }

        // Assert
        Assertions.assertEquals(1, link1Id);
        Assertions.assertEquals(2, link2Id);
        Assertions.assertTrue(isThrowWhenNoLink);
    }

    @Test
    @DisplayName("link repository: getUrl")
    void test3() {
        // Arrange

        // Act
        final String url1 = repository.getUrl(1);
        final String url2 = repository.getUrl(2);

        boolean isThrowWhenNoLink;
        try {
            repository.getUrl(123);
            isThrowWhenNoLink = false;
        } catch (NoSuchLinkException ignored) {
            isThrowWhenNoLink = true;
        } catch (Exception ignored) {
            isThrowWhenNoLink = false;
        }

        // Assert
        Assertions.assertEquals("url1", url1);
        Assertions.assertEquals("url2", url2);
        Assertions.assertTrue(isThrowWhenNoLink);
    }

    @Test
    @DisplayName("link repository: getLinksForUpdates")
    void test4() {
        // Arrange
        final LinkInfo linkInfo1 = new LinkInfo("url1", Instant.parse("2025-10-01T10:15:30Z"), true);
        final LinkInfo linkInfo2 = new LinkInfo("url2", Instant.parse("2025-11-01T10:15:30Z"), false);
        final LinkInfo linkInfo3 = new LinkInfo("url3", Instant.parse("2025-12-01T10:15:30Z"), false);

        final LinkBody linkBody1 = new LinkBody(1, linkInfo1.url(), linkInfo1.updateTime(), LinkType.GITHUB);
        final LinkBody linkBody2 = new LinkBody(2, linkInfo2.url(), linkInfo2.updateTime(), LinkType.STACKOVERFLOW);
        final LinkBody linkBody3 = new LinkBody(3, linkInfo3.url(), linkInfo3.updateTime(), LinkType.STACKOVERFLOW);

        // Act
        repository.add(linkInfo3);

        final var response1 = repository.getLinksForUpdates(3, 0);
        final var response2 = repository.getLinksForUpdates(2, 1);
        final var response3 = repository.getLinksForUpdates(2, 2);

        // Assert
        Assertions.assertEquals(List.of(linkBody1, linkBody2, linkBody3), response1);
        Assertions.assertEquals(List.of(linkBody2, linkBody3), response2);
        Assertions.assertEquals(List.of(linkBody3), response3);
    }

    @Test
    @DisplayName("link repository: updateLinkTime")
    void test5() {
        // Arrange
        final Instant time = Instant.parse("2025-10-01T10:15:30Z");
        final Instant updateTime = Instant.parse("2025-12-01T10:15:30Z");

        // Act
        final Instant timeFromRepo1 = repository.get("url1").updateTime();
        repository.updateLinkTime(1, updateTime);
        final Instant timeFromRepo2 = repository.get("url1").updateTime();

        boolean isThrowWhenNoLink;
        try {
            repository.updateLinkTime(123, updateTime);
            isThrowWhenNoLink = false;
        } catch (NoSuchLinkException ignored) {
            isThrowWhenNoLink = true;
        } catch (Exception ignored) {
            isThrowWhenNoLink = false;
        }

        // Assert
        Assertions.assertEquals(time, timeFromRepo1);
        Assertions.assertEquals(updateTime, timeFromRepo2);
        Assertions.assertTrue(isThrowWhenNoLink);
    }

    @Test
    @DisplayName("link repository: getActiveLinks")
    void test6() {
        // Arrange

        // Act
        final int countGithubLinks = repository.countActiveGithubLinks();
        final int countStackLinks = repository.countActiveStackLinks();

        // Assert
        Assertions.assertEquals(1, countGithubLinks);
        Assertions.assertEquals(2, countStackLinks);
    }
}
