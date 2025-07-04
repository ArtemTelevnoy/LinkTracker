package backend.academy.scrapper.link;

import java.util.Objects;

public record LinkUpdatesInfo(String description, String userName) {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof LinkUpdatesInfo)) {
            return false;
        }

        final LinkUpdatesInfo other = (LinkUpdatesInfo) o;
        return description.equals(other.description) && userName.equals(other.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, userName);
    }
}
