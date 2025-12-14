package dev.sheldan.sissi.module.meetup.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.sissi.module.meetup.config.MeetupFeatureDefinition;
import dev.sheldan.sissi.module.meetup.model.database.Meetup;
import dev.sheldan.sissi.module.meetup.model.database.MeetupDecision;
import dev.sheldan.sissi.module.meetup.model.database.MeetupParticipant;
import dev.sheldan.sissi.module.meetup.model.payload.MeetupDecisionPayload;
import dev.sheldan.sissi.module.meetup.model.template.MeetupMessageModel;
import dev.sheldan.sissi.module.meetup.service.MeetupServiceBean;
import dev.sheldan.sissi.module.meetup.service.management.MeetupManagementServiceBean;
import dev.sheldan.sissi.module.meetup.service.management.MeetupParticipatorManagementServiceBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class MeetupDecisionListener implements ButtonClickedListener {

    @Autowired
    private MeetupParticipatorManagementServiceBean meetupParticipatorManagementServiceBean;

    @Autowired
    private MeetupManagementServiceBean meetupManagementServiceBean;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private MeetupServiceBean meetupServiceBean;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private MeetupDecisionListener self;

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        MeetupDecisionPayload payload = (MeetupDecisionPayload) model.getDeserializedPayload();
        Meetup meetup = meetupManagementServiceBean.getMeetup(payload.getMeetupId(),  payload.getGuildId());
        Member member = model.getEvent().getMember();
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(member);

        Optional<MeetupParticipant> participationOptional = meetupParticipatorManagementServiceBean.getParticipation(meetup, userInAServer);
        if(participationOptional.isPresent()) {
            participationOptional.get().setDecision(payload.getMeetupDecision());
        } else {
            meetupParticipatorManagementServiceBean.createParticipation(meetup, userInAServer, payload.getMeetupDecision());
        }
        Long meetupMessageId = meetup.getMessageId();
        Long meetupId = meetup.getId().getId();
        Long meetupServerId = meetup.getServer().getId();
        meetupServiceBean.getMeetupMessageModel(meetup).thenAccept(meetupMessageModel -> {
            self.updateMeetupMessage(model, meetupMessageModel, member, payload, meetupMessageId, meetupId, meetupServerId);
        });
        return ButtonClickedListenerResult.ACKNOWLEDGED;
    }

    @Transactional
    public void updateMeetupMessage(ButtonClickedListenerModel model, MeetupMessageModel meetupMessageModel, Member member, MeetupDecisionPayload payload, Long meetupMessageId, Long meetupId, Long meetupServerId) {
        addParticipationToModel(meetupMessageModel, member, payload.getMeetupDecision());
        MessageToSend messageToSend = meetupServiceBean.getMeetupMessage(meetupMessageModel, model.getServerId());
        channelService.editMessageInAChannelFuture(messageToSend, model.getEvent().getChannel(), meetupMessageId)
                .thenAccept(message -> log.info("Updated message of meetup {} in channel {} in server {}.", meetupId, meetupMessageId, meetupServerId))
                .exceptionally(throwable -> {
                    log.info("Failed to update message of meetup {} in channel {} in server {}.", meetupId, meetupMessageId, meetupServerId, throwable);
                    return null;
                });
    }

    private void addParticipationToModel(MeetupMessageModel model, Member aUserInAServer, MeetupDecision decision) {
        if(decision.equals(MeetupDecision.NO)) {
            addIfMissing(model.getDeclinedParticipants(), aUserInAServer);
        } else if(decision.equals(MeetupDecision.YES)) {
            addIfMissing(model.getParticipants(), aUserInAServer);
        } else if(decision.equals(MeetupDecision.MAYBE)) {
            addIfMissing(model.getMaybeParticipants(), aUserInAServer);
        } else if(decision.equals(MeetupDecision.NO_TIME))
            addIfMissing(model.getNoTimeParticipants(), aUserInAServer);
    }

    private void addIfMissing(List<MemberDisplay> list, Member aUserInAServer) {
        if(list.stream().noneMatch(memberDisplay -> memberDisplay.getUserId().equals(aUserInAServer.getIdLong()))) {
            list.add(MemberDisplay.fromMember(aUserInAServer));
        }
    }

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        return model.getOrigin().equals(MeetupServiceBean.MEETUP_DECISION_BUTTON) && model.getEvent().isFromGuild();
    }

    @Override
    public FeatureDefinition getFeature() {
        return MeetupFeatureDefinition.MEETUP;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }
}
