package backend.academy.scrapper.repositories.link;

import backend.academy.scrapper.exceptions.NoSuchLinkException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
class LinkRepositoryUtils {
    long getId(@NotNull ReentrantReadWriteLock linksLock, @NotNull Function<String, Long> getIdByUrl, String url) {
        linksLock.readLock().lock();

        try {
            final long linkId = getIdByUrl.apply(url);

            if (linkId == -1) {
                throw new NoSuchLinkException(url);
            }

            return linkId;
        } finally {
            linksLock.readLock().unlock();
        }
    }
}
