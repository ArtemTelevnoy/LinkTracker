package backend.academy.scrapper.postgresTests.settingsTests;

import backend.academy.scrapper.repositories.settings.JdbcSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.simple.JdbcClient;

@JdbcTest
class JdbcSettingRepositoryTests extends AbstractSettingRepositoryTests {
    @Autowired
    JdbcSettingRepositoryTests(JdbcClient jdbcClient) {
        super(new JdbcSettingRepository(jdbcClient));
    }
}
