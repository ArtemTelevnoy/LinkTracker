package backend.academy.scrapper.applicationTests;

import backend.academy.scrapper.repositories.filter.FilterRepository;
import backend.academy.scrapper.repositories.filter.JpaFilterRepository;
import backend.academy.scrapper.repositories.link.JpaLinkRepository;
import backend.academy.scrapper.repositories.link.LinkRepository;
import backend.academy.scrapper.repositories.settings.JpaSettingRepository;
import backend.academy.scrapper.repositories.settings.SettingsRepository;
import backend.academy.scrapper.repositories.tag.JpaTagRepository;
import backend.academy.scrapper.repositories.tag.TagRepository;
import backend.academy.scrapper.repositories.user.JpaUserRepository;
import backend.academy.scrapper.repositories.user.UserRepository;
import backend.academy.scrapper.repositories.userLink.JpaUserLinkRepository;
import backend.academy.scrapper.repositories.userLink.UserLinkRepository;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@AllArgsConstructor
@TestPropertySource(properties = "app.access-type=ORM")
class JpaConfigTest extends TestcontainersConfiguration {
    private final ApplicationContext context;

    @BeforeAll
    static void before() {
        redis.start();
        kafka.start();
        postgres.start();
    }

    @Test
    @DisplayName("orm mode")
    void test() {
        // Arrange

        // Act
        final var filterClass = context.getBean(FilterRepository.class);
        final var tagClass = context.getBean(TagRepository.class);
        final var linkClass = context.getBean(LinkRepository.class);
        final var userClass = context.getBean(UserRepository.class);
        final var userLinkClass = context.getBean(UserLinkRepository.class);
        final var settingsClass = context.getBean(SettingsRepository.class);

        // Assert
        Assertions.assertInstanceOf(JpaFilterRepository.class, filterClass);
        Assertions.assertInstanceOf(JpaTagRepository.class, tagClass);
        Assertions.assertInstanceOf(JpaLinkRepository.class, linkClass);
        Assertions.assertInstanceOf(JpaUserRepository.class, userClass);
        Assertions.assertInstanceOf(JpaUserLinkRepository.class, userLinkClass);
        Assertions.assertInstanceOf(JpaSettingRepository.class, settingsClass);
    }
}
