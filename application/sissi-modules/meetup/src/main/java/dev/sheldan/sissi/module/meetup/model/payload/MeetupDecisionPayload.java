package dev.sheldan.sissi.module.meetup.model.payload;

import dev.sheldan.abstracto.core.models.template.button.ButtonPayload;
import dev.sheldan.sissi.module.meetup.model.database.MeetupDecision;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class MeetupDecisionPayload implements ButtonPayload {
    private Long meetupId;
    private Long guildId;
    private MeetupDecision meetupDecision;
    private List<String> componentPayloads;
}
