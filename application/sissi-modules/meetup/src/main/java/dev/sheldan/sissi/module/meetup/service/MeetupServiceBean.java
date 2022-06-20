package dev.sheldan.sissi.module.meetup.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.scheduling.model.JobParameters;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import dev.sheldan.sissi.module.meetup.model.command.MeetupConfirmationModel;
import dev.sheldan.sissi.module.meetup.model.database.Meetup;
import dev.sheldan.sissi.module.meetup.model.database.MeetupDecision;
import dev.sheldan.sissi.module.meetup.model.database.MeetupParticipator;
import dev.sheldan.sissi.module.meetup.model.database.MeetupState;
import dev.sheldan.sissi.module.meetup.model.payload.MeetupConfirmationPayload;
import dev.sheldan.sissi.module.meetup.model.template.MeetupMessageModel;
import dev.sheldan.sissi.module.meetup.service.management.MeetupManagementServiceBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MeetupServiceBean {

    public static final String MEETUP_CONFIRMATION_BUTTON = "MEETUP_CONFIRMATION_BUTTON";
    public static final String MEETUP_DECISION_BUTTON = "MEETUP_DECISION_BUTTON";
    private static final String MEETUP_DISPLAY_TEMPLATE = "meetup_display";
    private static final String MEETUP_CANCELLATION_TEMPLATE = "meetup_cancel_notification";
    private static final String MEETUP_REMINDER_TEMPLATE = "meetup_reminder_notification";
    private static final String MEETUP_LATE_REMINDER_CONFIG_KEY = "meetupLateReminderSeconds";
    private static final String MEETUP_EARLY_REMINDER_CONFIG_KEY = "meetupEarlyReminderSeconds";
    public static final String MEETUP_REMINDER_JOB_NAME = "meetupReminderJob";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ComponentPayloadService componentPayloadService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private MeetupManagementServiceBean meetupManagementServiceBean;

    @Autowired
    private MeetupServiceBean self;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private SchedulerService schedulerService;

    public void storeMeetupConfirmation(MeetupConfirmationModel model) {
        AServer server = serverManagementService.loadServer(model.getGuildId());
        MeetupConfirmationPayload confirmationPayload = MeetupConfirmationPayload
                .builder()
                .confirmationId(model.getConfirmationId())
                .meetupId(model.getMeetupId())
                .cancelId(model.getCancelId())
                .organizerUserId(model.getOrganizer().getUserId())
                .guildId(model.getGuildId())
                .build();
        componentPayloadService.createButtonPayload(model.getConfirmationId(), confirmationPayload, MEETUP_CONFIRMATION_BUTTON, server);
        componentPayloadService.createButtonPayload(model.getCancelId(), confirmationPayload, MEETUP_CONFIRMATION_BUTTON, server);
    }


    public MeetupMessageModel getMeetupMessageModel(Meetup meetup) {
        List<MeetupParticipator> allParticipants = meetup.getParticipants();
        List<MeetupParticipator> participating = allParticipants
                .stream()
                .filter(meetupParticipator -> meetupParticipator.getDecision().equals(MeetupDecision.YES))
                .collect(Collectors.toList());
        List<MeetupParticipator> maybe = allParticipants
                .stream()
                .filter(meetupParticipator -> meetupParticipator.getDecision().equals(MeetupDecision.MAYBE))
                .collect(Collectors.toList());
        List<MeetupParticipator> notParticipating = allParticipants
                .stream()
                .filter(meetupParticipator -> meetupParticipator.getDecision().equals(MeetupDecision.NO))
                .collect(Collectors.toList());
        return MeetupMessageModel
                .builder()
                .description(meetup.getDescription())
                .topic(meetup.getTopic())
                .meetupTime(meetup.getMeetupTime())
                .participants(getMemberDisplays(participating))
                .declinedParticipants(getMemberDisplays(notParticipating))
                .maybeParticipants(getMemberDisplays(maybe))
                .cancelled(meetup.getState().equals(MeetupState.CANCELLED))
                .organizer(MemberDisplay.fromAUserInAServer(meetup.getOrganizer()))
                .build();
    }

    private List<MemberDisplay> getMemberDisplays(List<MeetupParticipator> participants) {
        return participants
                .stream()
                .map(meetupParticipator -> MemberDisplay.fromAUserInAServer(meetupParticipator.getParticipator()))
                .collect(Collectors.toList());
    }

    public MessageToSend getMeetupMessage(MeetupMessageModel model) {
        return templateService.renderEmbedTemplate(MEETUP_DISPLAY_TEMPLATE, model);
    }

    public CompletableFuture<Void> cancelMeetup(Meetup meetup, List<String> componentPayloads) {
        Long serverId = meetup.getServer().getId();
        Long meetupId = meetup.getId().getId();
        GuildMessageChannel channel = channelService.getMessageChannelFromServer(serverId, meetup.getMeetupChannel().getId());
        MeetupMessageModel model = getMeetupMessageModel(meetup);
        model.setCancelled(true);
        MessageToSend meetupMessage = getMeetupMessage(model);
        return messageService.editMessageInChannel(channel, meetupMessage, meetup.getMessageId())
                .thenAccept(unused -> self.notifyParticipants(meetupId, serverId))
                .thenAccept(unused -> self.cleanupMeetup(meetupId, serverId, componentPayloads));
    }

    @Transactional
    public void notifyParticipants(Long meetupId, Long serverId) {
        Meetup meetup = meetupManagementServiceBean.getMeetup(meetupId, serverId);
        MeetupMessageModel model = getMeetupMessageModel(meetup);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(MEETUP_CANCELLATION_TEMPLATE, model);
        meetup
                .getParticipants()
                .stream()
                .filter(meetupParticipator ->
                                meetupParticipator.getDecision().equals(MeetupDecision.MAYBE) ||
                                meetupParticipator.getDecision().equals(MeetupDecision.YES))
                .forEach(meetupParticipator -> {
                    Long userId = meetupParticipator.getParticipator().getUserReference().getId();
                    userService.retrieveUserForId(userId)
                            .thenCompose(user -> messageService.sendMessageToSendToUser(user, messageToSend))
                            .thenAccept(message -> log.info("Notified user {} about cancellation of meetup {} in server {}.", userId, meetupId, serverId));
                });
    }

    @Transactional
    public void cleanupMeetup(Long meetupId, Long serverId, List<String> componentPayloads) {
        Meetup meetup = meetupManagementServiceBean.getMeetup(meetupId, serverId);
        meetup.setState(MeetupState.CANCELLED);
        log.info("Cleanup meetup {} in server {}.", meetup, serverId);
        if(meetup.getEarlyReminderJobTriggerKey() != null) {
            schedulerService.stopTrigger(meetup.getEarlyReminderJobTriggerKey());
        }
        if(meetup.getLateReminderJobTriggerKey() != null) {
            schedulerService.stopTrigger(meetup.getLateReminderJobTriggerKey());
        }
        componentPayloads.forEach(s -> componentPayloadManagementService.deletePayload(s));
    }

    public void scheduleReminders(Meetup meetup) {
        Long serverId = meetup.getServer().getId();
        Long meetupId = meetup.getId().getId();
        Long earlyReminderSeconds = configService.getLongValueOrConfigDefault(MEETUP_EARLY_REMINDER_CONFIG_KEY, serverId);
        Long lateReminderSeconds = configService.getLongValueOrConfigDefault(MEETUP_LATE_REMINDER_CONFIG_KEY, serverId);

        HashMap<Object, Object> parameters = new HashMap<>();
        parameters.put("meetupId", meetupId.toString());
        parameters.put("serverId", serverId.toString());
        JobParameters jobParameters = JobParameters
                .builder()
                .parameters(parameters)
                .build();
        Instant meetupDate = meetup.getMeetupTime();
        Instant earlyDate = meetupDate.minus(earlyReminderSeconds, ChronoUnit.SECONDS);
        if(earlyDate.isAfter(Instant.now())) {
            log.info("Scheduling early reminder job for meetup {} in server {} at {}.", meetupId, serverId, earlyDate);
            String earlyTriggerKey = schedulerService.executeJobWithParametersOnce(MEETUP_REMINDER_JOB_NAME, "meetup", jobParameters, Date.from(earlyDate));
            meetup.setEarlyReminderJobTriggerKey(earlyTriggerKey);
        }
        Instant lateDate = meetupDate.minus(lateReminderSeconds, ChronoUnit.SECONDS);
        if(lateDate.isAfter(Instant.now())) {
            log.info("Scheduling late reminder job for meetup {} in server {} at {}.", meetupId, serverId, lateDate);
            String lateTriggerKey = schedulerService.executeJobWithParametersOnce(MEETUP_REMINDER_JOB_NAME, "meetup", jobParameters, Date.from(lateDate));
            meetup.setLateReminderJobTriggerKey(lateTriggerKey);
        }
    }

    @Transactional
    public void remindParticipants(Long meetupId, Long serverId) {
        Meetup meetup = meetupManagementServiceBean.getMeetup(meetupId, serverId);
        MeetupMessageModel model = getMeetupMessageModel(meetup);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(MEETUP_REMINDER_TEMPLATE, model);
        meetup
                .getParticipants()
                .stream()
                .filter(meetupParticipator ->
                        meetupParticipator.getDecision().equals(MeetupDecision.MAYBE) ||
                        meetupParticipator.getDecision().equals(MeetupDecision.YES))
                .forEach(meetupParticipator -> {
                    Long userId = meetupParticipator.getParticipator().getUserReference().getId();
                    userService.retrieveUserForId(userId)
                            .thenCompose(user -> messageService.sendMessageToSendToUser(user, messageToSend))
                            .thenAccept(message -> log.info("Notified user {} about incoming meetup {} in server {}.", userId, meetupId, serverId));
                });
    }

    @Transactional
    public void cleanupMeetups() {
        Instant time = Instant.now().minus(1, ChronoUnit.DAYS);
        List<Meetup> oldMeetups = meetupManagementServiceBean.getMeetupsOlderThan(time)
                .stream()
                .filter(meetup -> meetup.getMessageId() != null)
                .collect(Collectors.toList());
        oldMeetups.forEach(meetup -> messageService.deleteMessageInChannelInServer(meetup.getServer().getId(), meetup.getMeetupChannel().getId(), meetup.getMessageId()));
        meetupManagementServiceBean.deleteMeetups(oldMeetups);
    }
}
