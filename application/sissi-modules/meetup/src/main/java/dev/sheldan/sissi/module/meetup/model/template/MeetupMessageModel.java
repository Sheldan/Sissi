package dev.sheldan.sissi.module.meetup.model.template;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@Setter
public class MeetupMessageModel {
    private String topic;
    private String description;
    private Instant meetupTime;
    private MemberDisplay organizer;
    private Long meetupId;
    private String yesId;
    private String noId;
    private String maybeId;
    private String noTimeId;
    private Boolean cancelled;
    private List<MemberDisplay> participants;
    private List<MemberDisplay> maybeParticipants;
    private List<MemberDisplay> noTimeParticipants;
    private List<MemberDisplay> declinedParticipants;
}
