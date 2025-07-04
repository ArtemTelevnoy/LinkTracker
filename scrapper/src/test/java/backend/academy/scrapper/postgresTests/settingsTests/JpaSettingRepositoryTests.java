package backend.academy.scrapper.postgresTests.settingsTests;

import backend.academy.scrapper.repositories.settings.JpaSettingRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class JpaSettingRepositoryTests extends AbstractSettingRepositoryTests {
    @Autowired
    JpaSettingRepositoryTests(EntityManagerFactory emf) {
        super(new JpaSettingRepository(emf));
    }
}
