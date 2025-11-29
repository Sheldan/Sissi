package dev.sheldan.sissi.module.debra.model.database;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "donation")
@Getter
@Setter
@EqualsAndHashCode
public class Donation {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    // we cant be sure about duplicates
    @Column(name = "count")
    private Integer count;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;
}
