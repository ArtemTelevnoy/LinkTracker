package backend.academy.scrapper.repositories.link;

import static backend.academy.scrapper.link.LinkType.*;

import backend.academy.scrapper.exceptions.NoSuchLinkException;
import backend.academy.scrapper.link.LinkBody;
import backend.academy.scrapper.link.LinkInfo;
import backend.academy.scrapper.repositories.link.entity.Link;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "ORM")
public class JpaLinkRepository implements LinkRepository {
    private final EntityManagerFactory emf;
    private final ReentrantReadWriteLock linksLock = new ReentrantReadWriteLock();

    @Override
    public long add(@NotNull LinkInfo linkInfo) {
        linksLock.writeLock().lock();

        try {
            final long linkId = getIdByUrl(linkInfo.url());

            if (linkId != -1) {
                return linkId;
            }

            try (EntityManager em = emf.createEntityManager()) {
                em.getTransaction().begin();

                try {
                    em.persist(new Link(linkInfo));
                    em.getTransaction().commit();
                } catch (Exception ignored) {
                    em.getTransaction().rollback();
                }

                return getIdByUrl(linkInfo.url());
            }
        } finally {
            linksLock.writeLock().unlock();
        }
    }

    @Override
    public LinkInfo get(String url) {
        linksLock.readLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaQuery<Link> cq = cb.createQuery(Link.class);
            final Root<Link> root = cq.from(Link.class);

            cq.select(root).where(cb.equal(root.get("url"), url));

            try {
                final Link link = em.createQuery(cq).getSingleResult();
                return new LinkInfo(link.url(), link.updateTime(), link.isGithub());
            } catch (NoResultException e) {
                throw new NoSuchLinkException(url, e);
            }
        } finally {
            linksLock.readLock().unlock();
        }
    }

    private long getIdByUrl(String url) {
        try (EntityManager em = emf.createEntityManager()) {
            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaQuery<Link> cq = cb.createQuery(Link.class);
            final Root<Link> root = cq.from(Link.class);

            cq.select(root).where(cb.equal(root.get("url"), url));

            try {
                return em.createQuery(cq).getSingleResult().id();
            } catch (NoResultException ignored) {
                return -1;
            }
        }
    }

    @Override
    public String getUrl(long linkId) {
        linksLock.readLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            final Link result = em.find(Link.class, linkId);

            if (result == null) {
                throw new NoSuchLinkException(linkId);
            }

            return result.url();
        } finally {
            linksLock.readLock().unlock();
        }
    }

    @Override
    public long getId(String url) {
        return LinkRepositoryUtils.getId(linksLock, this::getIdByUrl, url);
    }

    @Override
    public List<LinkBody> getLinksForUpdates(int batchSize, int skipCount) {
        linksLock.readLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaQuery<Link> cq = cb.createQuery(Link.class);
            final Root<Link> root = cq.from(Link.class);

            cq.select(root);
            final TypedQuery<Link> query = em.createQuery(cq);
            query.setFirstResult(skipCount);
            query.setMaxResults(batchSize);

            return query.getResultStream()
                    .map(link -> new LinkBody(
                            link.id(), link.url(), link.updateTime(), link.isGithub() ? GITHUB : STACKOVERFLOW))
                    .toList();
        } finally {
            linksLock.readLock().unlock();
        }
    }

    @Override
    public void updateLinkTime(long linkId, Instant updatedTime) {
        getUrl(linkId);
        linksLock.writeLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaUpdate<Link> cu = cb.createCriteriaUpdate(Link.class);
            final Root<Link> root = cu.from(Link.class);

            cu.set(root.get("updateTime"), updatedTime);
            cu.where(cb.equal(root.get("id"), linkId));

            try {
                em.createQuery(cu).executeUpdate();
                em.getTransaction().commit();
            } catch (Exception ignored) {
                em.getTransaction().rollback();
            }
        } finally {
            linksLock.writeLock().unlock();
        }
    }

    @Override
    public int countActiveGithubLinks() {
        return countActiveLinks(true);
    }

    @Override
    public int countActiveStackLinks() {
        return countActiveLinks(false);
    }

    private int countActiveLinks(boolean isGithub) {
        linksLock.readLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaQuery<Link> cq = cb.createQuery(Link.class);
            final Root<Link> root = cq.from(Link.class);

            cq.select(root).where(cb.equal(root.get("isGithub"), isGithub));

            return em.createQuery(cq).getResultList().size();
        } finally {
            linksLock.readLock().unlock();
        }
    }
}
