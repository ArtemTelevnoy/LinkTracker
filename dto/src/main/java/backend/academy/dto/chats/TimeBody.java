package backend.academy.dto.chats;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Objects;

public record TimeBody(@Min(0) @Max(23) short hours, @Min(0) @Max(59) short minutes) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof TimeBody)) {
            return false;
        }

        final TimeBody other = (TimeBody) o;
        return hours == other.hours && minutes == other.minutes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hours, minutes);
    }
}
