package dev.sheldan.sissi.module.meetup.model.database;

import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import dev.sheldan.sissi.module.meetup.model.database.embed.MeetupComponentId;
import lombok.*;

import jakarta.persistence.*;
import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "meetup_component")
@Getter
@Setter
@EqualsAndHashCode
public class MeetupComponent {

    @EmbeddedId
    @Getter
    private MeetupComponentId id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @MapsId("componentId")
    @JoinColumn(name = "component_id", nullable = false)
    private ComponentPayload payload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
            {
                    @JoinColumn(updatable = false, insertable = false, name = "meetup_id", referencedColumnName = "id"),
                    @JoinColumn(updatable = false, insertable = false, name = "server_id", referencedColumnName = "server_id")
            })
    private Meetup meetup;

    @Column(name = "created")
    private Instant created;

}
