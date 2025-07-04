package backend.academy.scrapper.postgresTests.filtersTests;

import backend.academy.scrapper.repositories.filter.JpaFilterRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class JpaFilterRepositoryTests extends AbstractFilterRepositoryTests {
    @Autowired
    JpaFilterRepositoryTests(EntityManagerFactory emf) {
        super(new JpaFilterRepository(emf));
    }
}
