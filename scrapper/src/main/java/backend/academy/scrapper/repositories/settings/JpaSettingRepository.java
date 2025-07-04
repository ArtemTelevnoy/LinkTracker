package backend.academy.scrapper.repositories.settings;

import backend.academy.dto.chats.TimeBody;
import backend.academy.scrapper.repositories.settings.entity.Setting;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "ORM")
public class JpaSettingRepository implements SettingsRepository {
    private final EntityManagerFactory emf;
    private final ReentrantReadWriteLock settingsLock = new ReentrantReadWriteLock();

    @Override
    public void add(long id, TimeBody time) {
        settingsLock.writeLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            try {
                em.merge(new Setting(id, time.hours(), time.minutes()));
                em.getTransaction().commit();
            } catch (Exception ignored) {
                em.getTransaction().rollback();
            }
        } finally {
            settingsLock.writeLock().unlock();
        }
    }

    @Override
    public TimeBody get(long id) {
        settingsLock.readLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            final Setting setting = em.find(Setting.class, id);
            return setting == null ? null : new TimeBody(setting.hoursTime(), setting.minutesTime());
        } finally {
            settingsLock.readLock().unlock();
        }
    }

    @Override
    public void delete(long id) {
        settingsLock.writeLock().lock();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            final CriteriaBuilder cb = emf.getCriteriaBuilder();
            final CriteriaDelete<Setting> cd = cb.createCriteriaDelete(Setting.class);
            final Root<Setting> root = cd.from(Setting.class);

            cd.where(cb.equal(root.get("id"), id));

            try {
                em.createQuery(cd).executeUpdate();
                em.getTransaction().commit();
            } catch (Exception ignored) {
                em.getTransaction().rollback();
            }
        } finally {
            settingsLock.writeLock().unlock();
        }
    }
}
