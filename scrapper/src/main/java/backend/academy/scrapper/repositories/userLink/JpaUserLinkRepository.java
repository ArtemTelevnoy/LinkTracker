package backend.academy.scrapper.repositories.userLink;

import backend.academy.scrapper.exceptions.DuplicateLinkException;
import backend.academy.scrapper.exceptions.NoSuchLinkException;
import backend.academy.scrapper.repositories.link.entity.Link;
import backend.academy.scrapper.repositories.user.entity.User;
import backend.academy.scrapper.repositories.userLink.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.RollbackException;
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
@AllArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "ORM")
public class JpaUserLinkRepository implements UserLinkRepository {
    private final EntityManagerFactory emf;
    private final ReentrantReadWriteLock userLinksLock = new ReentrantReadWriteLock();

    @Override
    public void add(long userId, long linkId) {
        userLinksLock.readLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            try {
                em.persist(new UserLink(
                        new UserLinkId(userId, linkId), em.find(User.class, userId), em.find(Link.class, linkId)));
                em.getTransaction().commit();
            } catch (RollbackException e) {
                em.getTransaction().rollback();
                throw new DuplicateLinkException(linkId, e);
            }
        } finally {
            userLinksLock.readLock().unlock();
        }
    }

    @Override
    public void delete(long userId, long linkId) {
        userLinksLock.writeLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            if (em.find(UserLink.class, new UserLinkId(userId, linkId)) == null) {
                throw new NoSuchLinkException(linkId);
            }

            em.getTransaction().begin();

            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaDelete<UserLink> cd = cb.createCriteriaDelete(UserLink.class);
            final Root<UserLink> root = cd.from(UserLink.class);

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
            userLinksLock.writeLock().unlock();
        }
    }

    @Override
    public void deleteAllUserData(long userId) {
        userLinksLock.writeLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaDelete<UserLink> cd = cb.createCriteriaDelete(UserLink.class);
            final Root<UserLink> root = cd.from(UserLink.class);

            cd.where(cb.equal(root.get("id").get("userId"), userId));

            try {
                em.createQuery(cd).executeUpdate();
                em.getTransaction().commit();
            } catch (Exception ignored) {
                em.getTransaction().rollback();
            }
        } finally {
            userLinksLock.writeLock().unlock();
        }
    }

    @Override
    public long[] getChats(long linkId) {
        userLinksLock.readLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaQuery<UserLink> cq = cb.createQuery(UserLink.class);
            final Root<UserLink> root = cq.from(UserLink.class);

            cq.select(root).where(cb.equal(root.get("id").get("linkId"), linkId));

            return em.createQuery(cq)
                    .getResultStream()
                    .map(o -> o.id().userId())
                    .mapToLong(Long::longValue)
                    .toArray();
        } finally {
            userLinksLock.readLock().unlock();
        }
    }

    @Override
    public List<Long> getUserLinksIds(long userId) {
        userLinksLock.readLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaQuery<UserLink> cq = cb.createQuery(UserLink.class);
            final Root<UserLink> root = cq.from(UserLink.class);

            cq.select(root).where(cb.equal(root.get("id").get("userId"), userId));

            return em.createQuery(cq)
                    .getResultStream()
                    .map(o -> o.id().linkId())
                    .toList();
        } finally {
            userLinksLock.readLock().unlock();
        }
    }
}
