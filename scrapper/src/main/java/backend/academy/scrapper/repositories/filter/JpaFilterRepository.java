package backend.academy.scrapper.repositories.filter;

import backend.academy.scrapper.repositories.filter.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "ORM")
public class JpaFilterRepository implements FilterRepository {
    private final EntityManagerFactory emf;
    private final ReentrantReadWriteLock filtersLock = new ReentrantReadWriteLock();

    @Override
    public void add(long userId, long linkId, String[] filters) {
        filtersLock.writeLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            for (String filterName : filters) {
                em.getTransaction().begin();

                try {
                    em.persist(new Filter(new FilterId(userId, linkId, filterName)));
                    em.getTransaction().commit();
                } catch (Exception ignored) {
                    em.getTransaction().rollback();
                }
            }
        } finally {
            filtersLock.writeLock().unlock();
        }
    }

    @Override
    public String[] get(long userId, long linkId) {
        filtersLock.readLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaQuery<Filter> cq = cb.createQuery(Filter.class);
            final Root<Filter> root = cq.from(Filter.class);

            cq.select(root)
                    .where(
                            cb.equal(root.get("id").get("userId"), userId),
                            cb.equal(root.get("id").get("linkId"), linkId));

            return em.createQuery(cq)
                    .getResultStream()
                    .map(filter -> filter.id().filterName())
                    .toArray(String[]::new);
        } finally {
            filtersLock.readLock().unlock();
        }
    }

    @Override
    public void delete(long userId, long linkId) {
        filtersLock.writeLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaDelete<Filter> cd = cb.createCriteriaDelete(Filter.class);
            final Root<Filter> root = cd.from(Filter.class);

            cd.where(
                    cb.equal(root.get("id").get("userId"), userId),
                    cb.equal(root.get("id").get("linkId"), linkId));

            try {
                em.createQuery(cd).executeUpdate();
                em.getTransaction().commit();
            } catch (Exception ignored) {
                em.getTransaction().rollback();
            }
        } finally {
            filtersLock.writeLock().unlock();
        }
    }

    @Override
    public void deleteAllUserData(long userId) {
        filtersLock.writeLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaDelete<Filter> cd = cb.createCriteriaDelete(Filter.class);
            final Root<Filter> root = cd.from(Filter.class);

            cd.where(cb.equal(root.get("id").get("userId"), userId));

            try {
                em.createQuery(cd).executeUpdate();
                em.getTransaction().commit();
            } catch (Exception ignored) {
                em.getTransaction().rollback();
            }
        } finally {
            filtersLock.writeLock().unlock();
        }
    }
}
