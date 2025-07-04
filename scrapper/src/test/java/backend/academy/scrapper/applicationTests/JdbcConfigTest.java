package backend.academy.scrapper.applicationTests;

import backend.academy.scrapper.repositories.filter.FilterRepository;
import backend.academy.scrapper.repositories.filter.JdbcFilterRepository;
import backend.academy.scrapper.repositories.link.JdbcLinkRepository;
import backend.academy.scrapper.repositories.link.LinkRepository;
import backend.academy.scrapper.repositories.settings.JdbcSettingRepository;
import backend.academy.scrapper.repositories.settings.SettingsRepository;
import backend.academy.scrapper.repositories.tag.JdbcTagRepository;
import backend.academy.scrapper.repositories.tag.TagRepository;
import backend.academy.scrapper.repositories.user.JdbcUserRepository;
import backend.academy.scrapper.repositories.user.UserRepository;
import backend.academy.scrapper.repositories.userLink.JdbcUserLinkRepository;
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
@TestPropertySource(properties = "app.access-type=SQL")
class JdbcConfigTest extends TestcontainersConfiguration {
    private final ApplicationContext context;

    @BeforeAll
    static void before() {
        redis.start();
        kafka.start();
        postgres.start();
    }

    @Test
    @DisplayName("sql mode")
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
        Assertions.assertInstanceOf(JdbcFilterRepository.class, filterClass);
        Assertions.assertInstanceOf(JdbcTagRepository.class, tagClass);
        Assertions.assertInstanceOf(JdbcLinkRepository.class, linkClass);
        Assertions.assertInstanceOf(JdbcUserRepository.class, userClass);
        Assertions.assertInstanceOf(JdbcUserLinkRepository.class, userLinkClass);
        Assertions.assertInstanceOf(JdbcSettingRepository.class, settingsClass);
    }
}
