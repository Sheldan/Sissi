package dev.sheldan.sissi.module.meetup.model.database.embed;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MeetupComponentId implements Serializable {
    @Column(name = "component_id")
    private String componentId;

    @Column(name = "meetup_id")
    private Long meetupId;

    @Column(name = "server_id")
    private Long serverId;
}
