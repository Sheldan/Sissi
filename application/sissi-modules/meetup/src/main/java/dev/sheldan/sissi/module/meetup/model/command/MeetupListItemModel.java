package dev.sheldan.sissi.module.meetup.model.command;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.sissi.module.meetup.model.database.Meetup;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class MeetupListItemModel {
    private String topic;
    private Instant meetupTime;
    private ServerChannelMessage meetupMessage;

    public static MeetupListItemModel fromMeetup(Meetup meetup) {
        ServerChannelMessage message = ServerChannelMessage
                .builder()
                .serverId(meetup.getServer().getId())
                .channelId(meetup.getMeetupChannel().getId())
                .messageId(meetup.getMessageId())
                .build();
        return MeetupListItemModel
                .builder()
                .topic(meetup.getTopic())
                .meetupTime(meetup.getMeetupTime())
                .meetupMessage(message)
                .build();
    }
}
