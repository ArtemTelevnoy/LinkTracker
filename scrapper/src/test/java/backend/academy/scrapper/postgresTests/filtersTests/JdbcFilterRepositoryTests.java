package backend.academy.scrapper.postgresTests.filtersTests;

import backend.academy.scrapper.repositories.filter.JdbcFilterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.simple.JdbcClient;

@JdbcTest
class JdbcFilterRepositoryTests extends AbstractFilterRepositoryTests {
    @Autowired
    JdbcFilterRepositoryTests(JdbcClient jdbcClient) {
        super(new JdbcFilterRepository(jdbcClient));
    }
}
