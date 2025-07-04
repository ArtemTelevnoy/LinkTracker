package backend.academy.scrapper.postgresTests.usersTests;

import backend.academy.scrapper.repositories.user.JdbcUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.simple.JdbcClient;

@JdbcTest
class JdbcUserRepositoryTests extends AbstractUserRepositoryTests {
    @Autowired
    JdbcUserRepositoryTests(JdbcClient jdbcClient) {
        super(new JdbcUserRepository(jdbcClient));
    }
}
