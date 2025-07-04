package backend.academy.scrapper.postgresTests.linksTests;

import backend.academy.scrapper.repositories.link.JpaLinkRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class JpaLinkRepositoryTests extends AbstractLinkRepositoryTests {
    @Autowired
    JpaLinkRepositoryTests(EntityManagerFactory emf) {
        super(new JpaLinkRepository(emf));
    }
}
