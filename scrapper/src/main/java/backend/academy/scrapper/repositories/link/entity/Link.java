package backend.academy.scrapper.repositories.link.entity;

import backend.academy.scrapper.link.LinkInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "links")
@NoArgsConstructor
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "links_id_gen")
    @SequenceGenerator(name = "links_id_gen", sequenceName = "links_link_id_seq", allocationSize = 1)
    @Column(name = "link_id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "url", nullable = false, length = Integer.MAX_VALUE)
    private String url;

    @NotNull
    @Column(name = "update_time", nullable = false)
    private Instant updateTime;

    @NotNull
    @Column(name = "is_github", nullable = false)
    private Boolean isGithub = false;

    public Link(@NotNull LinkInfo linkInfo) {
        this.url = linkInfo.url();
        this.updateTime = linkInfo.updateTime();
        this.isGithub = linkInfo.isGithub();
    }
}
