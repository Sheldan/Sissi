package dev.sheldan.sissi.module.meetup.model.template;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class MeetupTimeChangedNotificationModel {
    private String meetupTopic;
    private String meetupDescription;
    private Instant oldDate;
    private Instant newDate;
    private ServerChannelMessage meetupMessage;
}
