package backend.academy.scrapper.link;

import java.time.Instant;
import java.util.Objects;

public record LinkInfo(String url, Instant updateTime, boolean isGithub) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof LinkInfo)) {
            return false;
        }

        final LinkInfo other = (LinkInfo) o;
        return url.equals(other.url) && isGithub == other.isGithub && updateTime.compareTo(other.updateTime) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, updateTime, isGithub);
    }
}
