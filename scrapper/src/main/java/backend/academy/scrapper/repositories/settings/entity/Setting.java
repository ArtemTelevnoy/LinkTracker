package backend.academy.scrapper.repositories.settings.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "settings")
@NoArgsConstructor
@AllArgsConstructor
public class Setting {
    @Id
    @Column(name = "user_id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "hours_time", nullable = false)
    private Short hoursTime;

    @NotNull
    @Column(name = "minutes_time", nullable = false)
    private Short minutesTime;
}
