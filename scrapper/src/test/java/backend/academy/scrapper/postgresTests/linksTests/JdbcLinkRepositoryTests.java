package backend.academy.scrapper.postgresTests.linksTests;

import backend.academy.scrapper.repositories.link.JdbcLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.simple.JdbcClient;

@JdbcTest
class JdbcLinkRepositoryTests extends AbstractLinkRepositoryTests {
    @Autowired
    JdbcLinkRepositoryTests(JdbcClient jdbcClient) {
        super(new JdbcLinkRepository(jdbcClient));
    }
}
