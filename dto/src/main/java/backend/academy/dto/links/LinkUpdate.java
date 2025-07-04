package backend.academy.dto.links;

import java.util.Objects;

public record LinkUpdate(String description, long userId) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof LinkUpdate)) {
            return false;
        }

        final LinkUpdate other = (LinkUpdate) o;
        return description.equals(other.description) && userId == other.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, userId);
    }
}
