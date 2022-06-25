package dev.sheldan.sissi.module.meetup.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.sissi.module.meetup.config.MeetupFeatureDefinition;
import dev.sheldan.sissi.module.meetup.model.database.Meetup;
import dev.sheldan.sissi.module.meetup.model.payload.MeetupChangeTimeConfirmationPayload;
import dev.sheldan.sissi.module.meetup.service.MeetupServiceBean;
import dev.sheldan.sissi.module.meetup.service.management.MeetupManagementServiceBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
public class MeetupChangeTimeConfirmationListener implements ButtonClickedListener {

    @Autowired
    private MeetupManagementServiceBean meetupManagementServiceBean;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private MeetupServiceBean meetupServiceBean;

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        MeetupChangeTimeConfirmationPayload payload = (MeetupChangeTimeConfirmationPayload) model.getDeserializedPayload();
        if(model.getEvent().getUser().getIdLong() != payload.getOrganizerUserId()) {
            return ButtonClickedListenerResult.IGNORED;
        }
        if(model.getEvent().getComponentId().equals(payload.getConfirmationId())) {
            Meetup meetup = meetupManagementServiceBean.getMeetup(payload.getMeetupId(), payload.getGuildId());
            meetupServiceBean.changeMeetupTimeAndNotifyParticipants(meetup, Instant.ofEpochSecond(payload.getNewTime()));
            messageService.deleteMessage(model.getEvent().getMessage());
            cleanupConfirmationMessagePayloads(payload);
        } else if(model.getEvent().getComponentId().equals(payload.getCancelId())) {
            messageService.deleteMessage(model.getEvent().getMessage());
            cleanupConfirmationMessagePayloads(payload);
            return ButtonClickedListenerResult.ACKNOWLEDGED;
        } else {
            return ButtonClickedListenerResult.IGNORED;
        }
        return ButtonClickedListenerResult.IGNORED;
    }

    private void cleanupConfirmationMessagePayloads(MeetupChangeTimeConfirmationPayload payload) {
        componentPayloadManagementService.deletePayload(payload.getCancelId());
        componentPayloadManagementService.deletePayload(payload.getConfirmationId());
    }

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        return model.getOrigin().equals(MeetupServiceBean.MEETUP_CHANGE_TIME_CONFIRMATION_BUTTON) &&
                model.getEvent().isFromGuild();
    }

    @Override
    public FeatureDefinition getFeature() {
        return MeetupFeatureDefinition.MEETUP;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.LOWEST;
    }
}
