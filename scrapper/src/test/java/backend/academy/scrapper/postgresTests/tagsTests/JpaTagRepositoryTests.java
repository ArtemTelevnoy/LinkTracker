package backend.academy.scrapper.postgresTests.tagsTests;

import backend.academy.scrapper.repositories.tag.JpaTagRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class JpaTagRepositoryTests extends AbstractTagRepositoryTests {
    @Autowired
    JpaTagRepositoryTests(EntityManagerFactory emf) {
        super(new JpaTagRepository(emf));
    }
}
