package dev.sheldan.sissi.module.meetup.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.ButtonClickedListener;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.listener.ButtonClickedListenerModel;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        MeetupDecisionPayload payload = (MeetupDecisionPayload) model.getDeserializedPayload();
        Meetup meetup = meetupManagementServiceBean.getMeetup(payload.getMeetupId(),  payload.getGuildId());
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(model.getEvent().getMember());

        Optional<MeetupParticipant> participationOptional = meetupParticipatorManagementServiceBean.getParticipation(meetup, userInAServer);
        if(participationOptional.isPresent()) {
            participationOptional.get().setDecision(payload.getMeetupDecision());
        } else {
            meetupParticipatorManagementServiceBean.createParticipation(meetup, userInAServer, payload.getMeetupDecision());
        }
        MeetupMessageModel meetupMessageModel = meetupServiceBean.getMeetupMessageModel(meetup);
        addParticipationToModel(meetupMessageModel, userInAServer, payload.getMeetupDecision());
        MessageToSend messageToSend = meetupServiceBean.getMeetupMessage(meetupMessageModel);
        channelService.editEmbedMessageInAChannel(messageToSend.getEmbeds().get(0), model.getEvent().getChannel(), meetup.getMessageId())
                .thenAccept(message -> log.info("Updated message of meetup {} in channel {} in server {}.", meetup.getId().getId(), meetup.getMeetupChannel().getId(), meetup.getServer().getId()))
                .exceptionally(throwable -> {
                    log.info("Failed to update message of meetup {} in channel {} in server {}.", meetup.getId().getId(), meetup.getMeetupChannel().getId(), meetup.getServer().getId(), throwable);
                    return null;
                });
        return ButtonClickedListenerResult.ACKNOWLEDGED;
    }

    private void addParticipationToModel(MeetupMessageModel model, AUserInAServer aUserInAServer, MeetupDecision decision) {
        if(decision.equals(MeetupDecision.NO)) {
            addIfMissing(model.getDeclinedParticipants(), aUserInAServer);
        } else if(decision.equals(MeetupDecision.YES)) {
            addIfMissing(model.getParticipants(), aUserInAServer);
        } else if(decision.equals(MeetupDecision.MAYBE)) {
            addIfMissing(model.getMaybeParticipants(), aUserInAServer);
        } else if(decision.equals(MeetupDecision.NO_TIME))
            addIfMissing(model.getNoTimeParticipants(), aUserInAServer);
    }

    private void addIfMissing(List<MemberDisplay> list, AUserInAServer aUserInAServer) {
        if(list.stream().noneMatch(memberDisplay -> memberDisplay.getUserId().equals(aUserInAServer.getUserReference().getId()))) {
            list.add(MemberDisplay.fromAUserInAServer(aUserInAServer));
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
