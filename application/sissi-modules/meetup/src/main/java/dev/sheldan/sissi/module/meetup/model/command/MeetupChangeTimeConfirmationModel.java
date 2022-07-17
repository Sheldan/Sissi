package dev.sheldan.sissi.module.meetup.model.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class MeetupChangeTimeConfirmationModel {
    private Instant meetupTime;
    private String topic;
    private Long userId;
    private String description;
    private Long meetupId;
    private Long guildId;
    private String confirmationId;
    private String cancelId;
}
