package backend.academy.scrapper.repositories.filter.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "filters")
@NoArgsConstructor
@AllArgsConstructor
public class Filter {
    @EmbeddedId
    private FilterId id;
}
