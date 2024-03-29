package dev.sheldan.sissi.module.meetup.model.payload;

import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class MeetupChangeTimeConfirmationPayload implements ButtonPayload {
    private Long organizerUserId;
    private Long meetupId;
    private Long newTime;
    private Long guildId;
    private String confirmationId;
    private String cancelId;
}
