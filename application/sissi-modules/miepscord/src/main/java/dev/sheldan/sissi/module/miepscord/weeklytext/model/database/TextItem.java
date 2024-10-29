package dev.sheldan.sissi.module.miepscord.weeklytext.model.database;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "text_item")
@Getter
@Setter
@EqualsAndHashCode
public class TextItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "text")
    private String text;

    @Column(name = "done")
    private Boolean done;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private AUserInAServer creator;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;
}
