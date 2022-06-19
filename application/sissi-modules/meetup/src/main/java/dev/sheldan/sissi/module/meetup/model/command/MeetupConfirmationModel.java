package dev.sheldan.sissi.module.meetup.model.command;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class MeetupConfirmationModel {
    private MemberDisplay organizer;
    private Instant meetupTime;
    private Long meetupId;
    private String topic;
    private String description;
    private Long guildId;
    private String confirmationId;
    private String cancelId;
}
