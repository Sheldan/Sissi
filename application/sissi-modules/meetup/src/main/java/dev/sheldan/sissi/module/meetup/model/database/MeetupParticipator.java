package dev.sheldan.sissi.module.meetup.model.database;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.sissi.module.meetup.model.database.embed.MeetupParticipationId;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "meetup_participator")
@Getter
@Setter
@EqualsAndHashCode
public class MeetupParticipator {

    @EmbeddedId
    @Getter
    private MeetupParticipationId id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @MapsId("voterId")
    @JoinColumn(name = "meetup_participator_user_in_server_id", nullable = false)
    private AUserInAServer participator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
            {
                    @JoinColumn(updatable = false, insertable = false, name = "meetup_id", referencedColumnName = "id"),
                    @JoinColumn(updatable = false, insertable = false, name = "server_id", referencedColumnName = "server_id")
            })
    private Meetup meetup;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false)
    private MeetupDecision decision;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;
}
