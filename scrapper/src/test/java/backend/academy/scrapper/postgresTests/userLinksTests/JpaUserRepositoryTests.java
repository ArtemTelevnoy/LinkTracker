package backend.academy.scrapper.postgresTests.userLinksTests;

import backend.academy.scrapper.repositories.link.JpaLinkRepository;
import backend.academy.scrapper.repositories.user.JpaUserRepository;
import backend.academy.scrapper.repositories.userLink.JpaUserLinkRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class JpaUserRepositoryTests extends AbstractUserLinkRepositoryTests {
    @Autowired
    JpaUserRepositoryTests(EntityManagerFactory emf) {
        super(new JpaUserLinkRepository(emf), new JpaUserRepository(emf), new JpaLinkRepository(emf));
    }
}
