package backend.academy.scrapper.repositories.tag;

import backend.academy.scrapper.repositories.tag.entity.*;
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
@AllArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "ORM")
public class JpaTagRepository implements TagRepository {
    private final EntityManagerFactory emf;
    private final ReentrantReadWriteLock tagsLock = new ReentrantReadWriteLock();

    @Override
    public void add(long userId, long linkId, String[] tags) {
        tagsLock.writeLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            for (String tagName : tags) {
                em.getTransaction().begin();

                try {
                    em.persist(new Tag(new TagId(userId, tagName, linkId)));
                    em.getTransaction().commit();
                } catch (Exception ignored) {
                    em.getTransaction().rollback();
                }
            }
        } finally {
            tagsLock.writeLock().unlock();
        }
    }

    @Override
    public String[] get(long userId, long linkId) {
        tagsLock.readLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaQuery<Tag> cq = cb.createQuery(Tag.class);
            final Root<Tag> root = cq.from(Tag.class);
            cq.select(root)
                    .where(
                            cb.equal(root.get("id").get("userId"), userId),
                            cb.equal(root.get("id").get("linkId"), linkId));

            return em.createQuery(cq)
                    .getResultStream()
                    .map(tag -> tag.id().tagName())
                    .toArray(String[]::new);
        } finally {
            tagsLock.readLock().unlock();
        }
    }

    @Override
    public String[] getUserTags(long userId) {
        tagsLock.readLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaQuery<Tag> cq = cb.createQuery(Tag.class);
            final Root<Tag> root = cq.from(Tag.class);
            cq.select(root).where(cb.equal(root.get("id").get("userId"), userId));

            return em.createQuery(cq)
                    .getResultStream()
                    .map(tag -> tag.id().tagName())
                    .toArray(String[]::new);
        } finally {
            tagsLock.readLock().unlock();
        }
    }

    @Override
    public List<Long> getLinksIds(long userId, String tagName) {
        tagsLock.readLock().lock();

        try {
            return getLinksIdsNoLock(userId, tagName);
        } finally {
            tagsLock.readLock().unlock();
        }
    }

    private List<Long> getLinksIdsNoLock(long userId, String tagName) {
        try (EntityManager em = emf.createEntityManager()) {
            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaQuery<Tag> cq = cb.createQuery(Tag.class);
            final Root<Tag> root = cq.from(Tag.class);
            cq.select(root)
                    .where(
                            cb.equal(root.get("id").get("userId"), userId),
                            cb.equal(root.get("id").get("tagName"), tagName));

            return em.createQuery(cq)
                    .getResultStream()
                    .map(tag -> tag.id().linkId())
                    .toList();
        }
    }

    @Override
    public void delete(long userId, long linkId) {
        tagsLock.writeLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaDelete<Tag> cd = cb.createCriteriaDelete(Tag.class);
            final Root<Tag> root = cd.from(Tag.class);

            cd.where(
                    cb.equal(root.get("id").get("userId"), userId),
                    cb.equal(root.get("id").get("linkId"), linkId));

            em.createQuery(cd).executeUpdate();
            em.getTransaction().commit();
        } finally {
            tagsLock.writeLock().unlock();
        }
    }

    @Override
    public void deleteAllUserData(long userId) {
        tagsLock.writeLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaDelete<Tag> cd = cb.createCriteriaDelete(Tag.class);
            final Root<Tag> root = cd.from(Tag.class);

            cd.where(cb.equal(root.get("id").get("userId"), userId));

            em.createQuery(cd).executeUpdate();
            em.getTransaction().commit();
        } finally {
            tagsLock.writeLock().unlock();
        }
    }

    @Override
    public List<Long> deleteByTag(long userId, String tagName) {
        tagsLock.writeLock().lock();

        final List<Long> linkIds;
        try {
            linkIds = getLinksIdsNoLock(userId, tagName);
        } catch (Exception e) {
            tagsLock.writeLock().unlock();
            throw e;
        }

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaDelete<Tag> cd = cb.createCriteriaDelete(Tag.class);
            final Root<Tag> root = cd.from(Tag.class);

            cd.where(
                    cb.equal(root.get("id").get("userId"), userId),
                    cb.equal(root.get("id").get("tagName"), tagName));

            em.createQuery(cd).executeUpdate();
            em.getTransaction().commit();

            return linkIds;
        } finally {
            tagsLock.writeLock().unlock();
        }
    }
}
