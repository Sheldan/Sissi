package dev.sheldan.sissi.module.meetup.model.database.embed;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MeetupParticipationId implements Serializable {
    @Column(name = "meetup_participator_user_in_server_id")
    private Long participatorId;

    @Column(name = "meetup_id")
    private Long meetupId;

    @Column(name = "server_id")
    private Long serverId;
}
