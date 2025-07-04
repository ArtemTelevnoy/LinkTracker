package backend.academy.scrapper.postgresTests.usersTests;

import backend.academy.scrapper.repositories.user.JpaUserRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class JpaUserRepositoryTests extends AbstractUserRepositoryTests {
    @Autowired
    JpaUserRepositoryTests(EntityManagerFactory emf) {
        super(new JpaUserRepository(emf));
    }
}
