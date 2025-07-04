package backend.academy.scrapper.link;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

public record ListLinkUpdatesInfo(Instant updatedTime, LinkUpdatesInfo[] updatesInfos) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof ListLinkUpdatesInfo)) {
            return false;
        }

        final ListLinkUpdatesInfo other = (ListLinkUpdatesInfo) o;
        return updatedTime.equals(other.updatedTime) && Arrays.equals(updatesInfos, other.updatesInfos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(updatedTime, Arrays.hashCode(updatesInfos()));
    }
}
