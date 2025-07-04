package backend.academy.scrapper.repositories.filter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class FilterId implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 441704726012656630L;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "link_id", nullable = false)
    private Long linkId;

    @NotNull
    @Column(name = "filter_name", nullable = false, length = Integer.MAX_VALUE)
    private String filterName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof FilterId)) {
            return false;
        }

        final FilterId entity = (FilterId) o;
        return Objects.equals(this.linkId, entity.linkId)
                && Objects.equals(this.filterName, entity.filterName)
                && Objects.equals(this.userId, entity.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkId, filterName, userId);
    }
}
