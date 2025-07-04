package backend.academy.scrapper.repositories.tag.entity;

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
@AllArgsConstructor
@NoArgsConstructor
public class TagId implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 2427794592597653536L;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "tag_name", nullable = false, length = Integer.MAX_VALUE)
    private String tagName;

    @NotNull
    @Column(name = "link_id", nullable = false)
    private Long linkId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof TagId)) {
            return false;
        }

        final TagId entity = (TagId) o;
        return Objects.equals(this.linkId, entity.linkId)
                && Objects.equals(this.tagName, entity.tagName)
                && Objects.equals(this.userId, entity.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkId, tagName, userId);
    }
}
