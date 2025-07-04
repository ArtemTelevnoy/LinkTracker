package backend.academy.scrapper.postgresTests.userLinksTests;

import backend.academy.scrapper.exceptions.DuplicateLinkException;
import backend.academy.scrapper.exceptions.NoSuchLinkException;
import backend.academy.scrapper.link.LinkInfo;
import backend.academy.scrapper.postgresTests.PostgresContainer;
import backend.academy.scrapper.repositories.link.LinkRepository;
import backend.academy.scrapper.repositories.user.UserRepository;
import backend.academy.scrapper.repositories.userLink.UserLinkRepository;
import java.time.Instant;
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
abstract class AbstractUserLinkRepositoryTests extends PostgresContainer {
    private final UserLinkRepository userLinkRepository;
    private final UserRepository userRepository;
    private final LinkRepository linkRepository;

    @Test
    @DisplayName("userlink repository: add, getUserLinksIds")
    void test1() {
        // Arrange
        final LinkInfo linkInfo1 = new LinkInfo("url1", Instant.parse("2025-10-01T10:15:30Z"), true);
        final LinkInfo linkInfo2 = new LinkInfo("url2", Instant.parse("2025-11-01T10:15:30Z"), false);

        // Act
        userRepository.add(1);
        userRepository.add(2);
        linkRepository.add(linkInfo1);
        linkRepository.add(linkInfo2);

        userLinkRepository.add(1, linkRepository.getId(linkInfo1.url()));
        final List<Long> user1LinksIds = userLinkRepository.getUserLinksIds(1);

        userLinkRepository.add(2, linkRepository.getId(linkInfo2.url()));
        final List<Long> user2LinksIds = userLinkRepository.getUserLinksIds(2);

        boolean isThrowOnDuplicate;
        try {
            userLinkRepository.add(1, linkRepository.getId(linkInfo1.url()));
            isThrowOnDuplicate = false;
        } catch (DuplicateLinkException ignored) {
            isThrowOnDuplicate = true;
        } catch (Exception ignored) {
            isThrowOnDuplicate = false;
        }

        // Assert
        Assertions.assertEquals(List.of(1L), user1LinksIds);
        Assertions.assertEquals(List.of(2L), user2LinksIds);
        Assertions.assertTrue(isThrowOnDuplicate);
    }

    @Test
    @DisplayName("userlink repository: delete")
    void test2() {
        // Arrange

        // Act
        userLinkRepository.delete(1, 1);
        final List<Long> user1LinksIds = userLinkRepository.getUserLinksIds(1);
        final List<Long> user12LinksIdsStillExist = userLinkRepository.getUserLinksIds(2);

        userLinkRepository.delete(2, 2);
        final List<Long> user2LinksIds = userLinkRepository.getUserLinksIds(2);

        boolean isThrowWhenNoUserLink;
        try {
            userLinkRepository.delete(1, 123);
            isThrowWhenNoUserLink = false;
        } catch (NoSuchLinkException ignored) {
            isThrowWhenNoUserLink = true;
        } catch (Exception ignored) {
            isThrowWhenNoUserLink = false;
        }

        // Assert
        Assertions.assertTrue(user1LinksIds.isEmpty());
        Assertions.assertTrue(user2LinksIds.isEmpty());
        Assertions.assertEquals(List.of(2L), user12LinksIdsStillExist);
        Assertions.assertTrue(isThrowWhenNoUserLink);
    }

    @Test
    @DisplayName("userlink repository: deleteAllUserData")
    void test3() {
        // Arrange
        final LinkInfo linkInfo1 = new LinkInfo("url1", Instant.parse("2025-10-01T10:15:30Z"), true);
        final LinkInfo linkInfo2 = new LinkInfo("url2", Instant.parse("2025-11-01T10:15:30Z"), false);

        // Act
        userLinkRepository.add(1, linkRepository.getId(linkInfo1.url()));
        userLinkRepository.add(1, linkRepository.getId(linkInfo2.url()));
        userLinkRepository.deleteAllUserData(1);
        final List<Long> userLinksIds = userLinkRepository.getUserLinksIds(1);

        // Assert
        Assertions.assertTrue(userLinksIds.isEmpty());
    }

    @Test
    @DisplayName("userLink repository: getChats")
    void test4() {
        // Arrange
        userLinkRepository.add(1, 2);
        userLinkRepository.add(1, 1);
        userLinkRepository.add(2, 2);

        // Act
        final long[] link1Chats = userLinkRepository.getChats(1);
        final long[] link2Chats = userLinkRepository.getChats(2);

        // Assert
        Assertions.assertEquals(set(new long[] {1L}), set(link1Chats));
        Assertions.assertEquals(set(new long[] {1L, 2L}), set(link2Chats));
    }

    private static Set<Long> set(long[] array) {
        final Set<Long> set = new HashSet<>();
        for (long el : array) {
            set.add(el);
        }

        return set;
    }
}
