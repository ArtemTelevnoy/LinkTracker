package backend.academy.scrapper.repositories.userLink.entity;

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
public class UserLinkId implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 3123052778804813161L;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "link_id", nullable = false)
    private Long linkId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof UserLinkId)) {
            return false;
        }

        final UserLinkId entity = (UserLinkId) o;
        return Objects.equals(this.linkId, entity.linkId) && Objects.equals(this.userId, entity.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkId, userId);
    }
}
