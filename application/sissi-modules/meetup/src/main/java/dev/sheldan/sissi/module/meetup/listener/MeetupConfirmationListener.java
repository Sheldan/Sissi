package dev.sheldan.sissi.module.meetup.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadService;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.sissi.module.meetup.config.MeetupFeatureDefinition;
import dev.sheldan.sissi.module.meetup.model.database.Meetup;
import dev.sheldan.sissi.module.meetup.model.database.MeetupDecision;
import dev.sheldan.sissi.module.meetup.model.database.MeetupState;
import dev.sheldan.sissi.module.meetup.model.payload.MeetupDecisionPayload;
import dev.sheldan.sissi.module.meetup.model.payload.MeetupConfirmationPayload;
import dev.sheldan.sissi.module.meetup.model.template.MeetupMessageModel;
import dev.sheldan.sissi.module.meetup.service.MeetupServiceBean;
import dev.sheldan.sissi.module.meetup.service.management.MeetupComponentManagementServiceBean;
import dev.sheldan.sissi.module.meetup.service.management.MeetupManagementServiceBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.sissi.module.meetup.service.MeetupServiceBean.MEETUP_DECISION_BUTTON;

@Component
@Slf4j
public class MeetupConfirmationListener implements ButtonClickedListener {

    @Autowired
    private MeetupManagementServiceBean meetupManagementServiceBean;

    @Autowired
    private MeetupServiceBean meetupServiceBean;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ComponentPayloadService componentPayloadService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private MeetupConfirmationListener self;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private MeetupComponentManagementServiceBean meetupComponentManagementServiceBean;

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        MeetupConfirmationPayload payload = (MeetupConfirmationPayload) model.getDeserializedPayload();
        if(model.getEvent().getUser().getIdLong() != payload.getOrganizerUserId()) {
            return ButtonClickedListenerResult.IGNORED;
        }
        Meetup meetup = meetupManagementServiceBean.getMeetup(payload.getMeetupId(), payload.getGuildId());
        if(model.getEvent().getComponentId().equals(payload.getConfirmationId())) {
            meetup.setState(MeetupState.CONFIRMED);
        } else if(model.getEvent().getComponentId().equals(payload.getCancelId())){
            meetup.setState(MeetupState.CANCELLED);
            messageService.deleteMessage(model.getEvent().getMessage());
            cleanupConfirmationMessagePayloads(payload);
            meetupManagementServiceBean.deleteMeetup(meetup);
            return ButtonClickedListenerResult.ACKNOWLEDGED;
        } else {
            return ButtonClickedListenerResult.IGNORED;
        }
        cleanupConfirmationMessagePayloads(payload);
        String yesButtonId = componentService.generateComponentId();
        String noButtonId = componentService.generateComponentId();
        String maybeButtonId = componentService.generateComponentId();
        String noTimeButtonId = componentService.generateComponentId();
        MeetupMessageModel messageModel = meetupServiceBean.getMeetupMessageModel(meetup);
        messageModel.setYesId(yesButtonId);
        messageModel.setNoId(noButtonId);
        messageModel.setMaybeId(maybeButtonId);
        messageModel.setNoTimeId(noTimeButtonId);
        meetup.setYesButtonId(yesButtonId);
        meetup.setMaybeButtonId(maybeButtonId);
        meetup.setNoTimeButtonId(noTimeButtonId);
        meetup.setNotInterestedButtonId(noButtonId);
        messageModel.setCancelled(false);
        Long meetupId = payload.getMeetupId();
        Long serverId = payload.getGuildId();
        MessageToSend messageToSend = meetupServiceBean.getMeetupMessage(messageModel);
        List<CompletableFuture<Message>> messageFutures = channelService.sendMessageToSendToChannel(messageToSend, model.getEvent().getMessageChannel());
        FutureUtils.toSingleFutureGeneric(messageFutures).thenAccept(unused -> {
            messageService.deleteMessage(model.getEvent().getMessage());
            Message meetupMessage = messageFutures.get(0).join();
            messageService.pinMessage(meetupMessage);
            self.persistPayloads(meetupId, serverId, yesButtonId, noButtonId, maybeButtonId, noTimeButtonId, meetupMessage);
        }).exceptionally(throwable -> {
            log.error("Failed to send meetup message for meetup {}.", meetupId, throwable);
            return null;
        });
        return ButtonClickedListenerResult.ACKNOWLEDGED;
    }

    private void cleanupConfirmationMessagePayloads(MeetupConfirmationPayload payload) {
        componentPayloadManagementService.deletePayload(payload.getCancelId());
        componentPayloadManagementService.deletePayload(payload.getConfirmationId());
    }

    @Transactional
    public void persistPayloads(Long meetupId, Long serverId, String yesButtonId, String noButtonId, String maybeButtonId, String noTimeButtonId, Message meetupMessage) {
        MeetupDecisionPayload decisionPayload = MeetupDecisionPayload
                .builder()
                .meetupId(meetupId)
                .guildId(serverId)
                .componentPayloads(Arrays.asList(yesButtonId, noButtonId, maybeButtonId, noTimeButtonId))
                .build();
        AServer server = serverManagementService.loadServer(serverId);

        decisionPayload.setMeetupDecision(MeetupDecision.YES);
        ComponentPayload yesPayload = componentPayloadService.createButtonPayload(yesButtonId, decisionPayload, MEETUP_DECISION_BUTTON, server);
        decisionPayload.setMeetupDecision(MeetupDecision.NO);
        ComponentPayload noPayload = componentPayloadService.createButtonPayload(noButtonId, decisionPayload, MEETUP_DECISION_BUTTON, server);
        decisionPayload.setMeetupDecision(MeetupDecision.MAYBE);
        ComponentPayload maybePayload = componentPayloadService.createButtonPayload(maybeButtonId, decisionPayload, MEETUP_DECISION_BUTTON, server);
        decisionPayload.setMeetupDecision(MeetupDecision.NO_TIME);
        ComponentPayload noTimePayload = componentPayloadService.createButtonPayload(noTimeButtonId, decisionPayload, MEETUP_DECISION_BUTTON, server);
        Meetup meetup = meetupManagementServiceBean.getMeetup(meetupId, serverId);
        // storing the button IDs, so we can remove them independently
        meetupComponentManagementServiceBean.createComponent(meetup, yesButtonId, yesPayload);
        meetupComponentManagementServiceBean.createComponent(meetup, noButtonId, noPayload);
        meetupComponentManagementServiceBean.createComponent(meetup, maybeButtonId, maybePayload);
        meetupComponentManagementServiceBean.createComponent(meetup, noTimeButtonId, noTimePayload);

        meetupServiceBean.scheduleReminders(meetup);
        meetup.setMessageId(meetupMessage.getIdLong());
        AChannel channel = channelManagementService.loadChannel(meetupMessage.getChannel());
        meetup.setMeetupChannel(channel);
    }

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        return model.getOrigin().equals(MeetupServiceBean.MEETUP_CONFIRMATION_BUTTON) &&
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
