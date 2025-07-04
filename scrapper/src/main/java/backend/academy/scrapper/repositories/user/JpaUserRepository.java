package backend.academy.scrapper.repositories.user;

import backend.academy.scrapper.repositories.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "ORM")
@AllArgsConstructor
public class JpaUserRepository implements UserRepository {
    private final EntityManagerFactory emf;
    private final ReentrantReadWriteLock usersLock = new ReentrantReadWriteLock();

    @Override
    public void add(long userId) {
        usersLock.writeLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            try {
                em.persist(new User(userId));
                em.getTransaction().commit();
            } catch (Exception ignored) {
                em.getTransaction().rollback();
            }
        } finally {
            usersLock.writeLock().unlock();
        }
    }

    @Override
    public boolean exist(long userId) {
        usersLock.readLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            return em.find(User.class, userId) != null;
        } finally {
            usersLock.readLock().unlock();
        }
    }

    @Override
    public List<Long> getAllUsers() {
        usersLock.readLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaQuery<User> cq = cb.createQuery(User.class);
            final Root<User> root = cq.from(User.class);

            cq.select(root);
            return em.createQuery(cq).getResultStream().map(User::id).toList();
        } finally {
            usersLock.readLock().unlock();
        }
    }

    @Override
    public void delete(long userId) {
        usersLock.writeLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaDelete<User> cd = cb.createCriteriaDelete(User.class);
            final Root<User> root = cd.from(User.class);

            cd.where(cb.equal(root.get("id"), userId));

            try {
                em.createQuery(cd).executeUpdate();
                em.getTransaction().commit();
            } catch (Exception ignored) {
                em.getTransaction().rollback();
            }
        } finally {
            usersLock.writeLock().unlock();
        }
    }
}
