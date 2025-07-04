package backend.academy.scrapper.postgresTests.userLinksTests;

import backend.academy.scrapper.repositories.link.JdbcLinkRepository;
import backend.academy.scrapper.repositories.user.JdbcUserRepository;
import backend.academy.scrapper.repositories.userLink.JdbcUserLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.simple.JdbcClient;

@JdbcTest
class JdbcUserLinkRepositoryTests extends AbstractUserLinkRepositoryTests {
    @Autowired
    JdbcUserLinkRepositoryTests(JdbcClient jdbcClient) {
        super(
                new JdbcUserLinkRepository(jdbcClient),
                new JdbcUserRepository(jdbcClient),
                new JdbcLinkRepository(jdbcClient));
    }
}
