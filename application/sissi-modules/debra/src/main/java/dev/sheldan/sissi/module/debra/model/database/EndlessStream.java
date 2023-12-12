package dev.sheldan.sissi.module.debra.model.database;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "endless_stream")
@Getter
@Setter
@EqualsAndHashCode
public class EndlessStream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;
}
