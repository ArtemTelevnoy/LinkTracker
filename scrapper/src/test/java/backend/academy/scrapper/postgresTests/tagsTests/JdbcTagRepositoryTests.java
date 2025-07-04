package backend.academy.scrapper.postgresTests.tagsTests;

import backend.academy.scrapper.repositories.tag.JdbcTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.simple.JdbcClient;

@JdbcTest
class JdbcTagRepositoryTests extends AbstractTagRepositoryTests {
    @Autowired
    JdbcTagRepositoryTests(JdbcClient jdbcClient) {
        super(new JdbcTagRepository(jdbcClient));
    }
}
