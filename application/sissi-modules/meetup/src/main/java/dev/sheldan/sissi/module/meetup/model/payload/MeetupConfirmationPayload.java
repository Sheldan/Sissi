package dev.sheldan.sissi.module.meetup.model.payload;

import dev.sheldan.abstracto.core.models.template.button.ButtonPayload;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class MeetupConfirmationPayload implements ButtonPayload {
    private Long organizerUserId;
    private Long meetupId;
    private Long guildId;
    private String confirmationId;
    private String cancelId;
}
