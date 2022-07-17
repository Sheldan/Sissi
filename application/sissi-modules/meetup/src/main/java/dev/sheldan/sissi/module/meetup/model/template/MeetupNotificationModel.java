package dev.sheldan.sissi.module.meetup.model.template;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MeetupNotificationModel {
    private List<MemberDisplay> participants;
    private String notificationMessage;
}
