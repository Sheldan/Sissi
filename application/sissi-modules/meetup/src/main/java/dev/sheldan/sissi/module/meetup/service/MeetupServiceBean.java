package dev.sheldan.sissi.module.meetup.service;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadService;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FileService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.scheduling.model.JobParameters;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import dev.sheldan.sissi.module.meetup.config.MeetupFeatureDefinition;
import dev.sheldan.sissi.module.meetup.config.MeetupFeatureMode;
import dev.sheldan.sissi.module.meetup.model.command.MeetupChangeTimeConfirmationModel;
import dev.sheldan.sissi.module.meetup.model.command.MeetupConfirmationModel;
import dev.sheldan.sissi.module.meetup.model.database.Meetup;
import dev.sheldan.sissi.module.meetup.model.database.MeetupDecision;
import dev.sheldan.sissi.module.meetup.model.database.MeetupParticipant;
import dev.sheldan.sissi.module.meetup.model.database.MeetupState;
import dev.sheldan.sissi.module.meetup.model.payload.MeetupChangeTimeConfirmationPayload;
import dev.sheldan.sissi.module.meetup.model.payload.MeetupConfirmationPayload;
import dev.sheldan.sissi.module.meetup.model.template.MeetupIcsModel;
import dev.sheldan.sissi.module.meetup.model.template.MeetupMessageModel;
import dev.sheldan.sissi.module.meetup.model.template.MeetupNotificationModel;
import dev.sheldan.sissi.module.meetup.model.template.MeetupTimeChangedNotificationModel;
import dev.sheldan.sissi.module.meetup.service.management.MeetupComponentManagementServiceBean;
import dev.sheldan.sissi.module.meetup.service.management.MeetupManagementServiceBean;
import dev.sheldan.sissi.module.meetup.service.management.MeetupParticipatorManagementServiceBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MeetupServiceBean {

    public static final String MEETUP_CONFIRMATION_BUTTON = "MEETUP_CONFIRMATION_BUTTON";
    public static final String MEETUP_CHANGE_TIME_CONFIRMATION_BUTTON = "MEETUP_CHANGE_TIME_CONFIRMATION_BUTTON";
    public static final String MEETUP_DECISION_BUTTON = "MEETUP_DECISION_BUTTON";
    private static final String MEETUP_DISPLAY_TEMPLATE = "meetup_display";
    private static final String MEETUP_CANCELLATION_TEMPLATE = "meetup_cancel_notification";
    private static final String MEETUP_CHANGE_TIME_NOTIFICATION_TEMPLATE = "changeMeetupTime_notification";
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
    private FileService fileService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private MeetupParticipatorManagementServiceBean meetupParticipatorManagementServiceBean;

    @Autowired
    private MeetupComponentManagementServiceBean meetupComponentManagementServiceBean;
    private static final String ICS_TIME_STAMP_FORMAT = "yMMdd'T'kkmmss'Z'";
    private static final DateTimeFormatter ICS_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(ICS_TIME_STAMP_FORMAT);

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

    public void storeMeetupChangeTimeConfirmation(MeetupChangeTimeConfirmationModel model) {
        AServer server = serverManagementService.loadServer(model.getGuildId());
        MeetupChangeTimeConfirmationPayload confirmationPayload = MeetupChangeTimeConfirmationPayload
                .builder()
                .newTime(model.getMeetupTime().getEpochSecond())
                .confirmationId(model.getConfirmationId())
                .meetupId(model.getMeetupId())
                .organizerUserId(model.getUserId())
                .cancelId(model.getCancelId())
                .guildId(model.getGuildId())
                .build();
        componentPayloadService.createButtonPayload(model.getConfirmationId(), confirmationPayload, MEETUP_CHANGE_TIME_CONFIRMATION_BUTTON, server);
        componentPayloadService.createButtonPayload(model.getCancelId(), confirmationPayload, MEETUP_CHANGE_TIME_CONFIRMATION_BUTTON, server);
    }

    private MeetupIcsModel getMeetupICSModel(Meetup meetup) {
        ZonedDateTime startTime = meetup.getMeetupTime().atZone(ZoneId.of("UTC"));
        ZonedDateTime endTime = meetup.getMeetupTime().plus(1, ChronoUnit.HOURS).atZone(ZoneId.of("UTC"));
        String icsFormattedStartTime = startTime.format(ICS_DATE_TIME_FORMATTER);
        String icsFormattedEndTime = endTime.format(ICS_DATE_TIME_FORMATTER);
        String icsFormattedMeetupCreationTime = Instant.now().atZone(ZoneId.of("UTC"))
                .format(ICS_DATE_TIME_FORMATTER);
        boolean attachIcsFile = featureModeService.featureModeActive(MeetupFeatureDefinition.MEETUP, meetup.getServer().getId(), MeetupFeatureMode.ATTACH_ICS_FILE);
        return MeetupIcsModel
                .builder()
                .attachIcsFile(attachIcsFile)
                .icsFormattedCreationTime(icsFormattedMeetupCreationTime)
                .icsFormattedStartTime(icsFormattedStartTime)
                .icsFormattedEndTime(icsFormattedEndTime)
                .build();
    }

    public MeetupMessageModel getMeetupMessageModel(Meetup meetup) {
        List<MeetupParticipant> allParticipants = meetup.getParticipants();
        List<MeetupParticipant> participating = allParticipants
                .stream()
                .filter(meetupParticipator -> meetupParticipator.getDecision().equals(MeetupDecision.YES))
                .collect(Collectors.toList());
        List<MeetupParticipant> maybe = allParticipants
                .stream()
                .filter(meetupParticipator -> meetupParticipator.getDecision().equals(MeetupDecision.MAYBE))
                .collect(Collectors.toList());
        List<MeetupParticipant> notParticipating = allParticipants
                .stream()
                .filter(meetupParticipator -> meetupParticipator.getDecision().equals(MeetupDecision.NO))
                .collect(Collectors.toList());
        List<MeetupParticipant> notTimeParticipating = allParticipants
                .stream()
                .filter(meetupParticipator -> meetupParticipator.getDecision().equals(MeetupDecision.NO_TIME))
                .collect(Collectors.toList());
        String rawLocation = java.net.URLDecoder.decode(meetup.getLocation(), StandardCharsets.UTF_8);
        return MeetupMessageModel
                .builder()
                .description(meetup.getDescription())
                .topic(meetup.getTopic())
                .location(meetup.getLocation())
                .decodedLocation(rawLocation)
                .noTimeId(meetup.getNoTimeButtonId())
                .yesId(meetup.getYesButtonId())
                .maybeId(meetup.getMaybeButtonId())
                .noId(meetup.getNotInterestedButtonId())
                .meetupTime(meetup.getMeetupTime())
                .meetupId(meetup.getId().getId())
                .participants(getMemberDisplays(participating))
                .declinedParticipants(getMemberDisplays(notParticipating))
                .noTimeParticipants(getMemberDisplays(notTimeParticipating))
                .maybeParticipants(getMemberDisplays(maybe))
                .cancelled(meetup.getState().equals(MeetupState.CANCELLED))
                .organizer(MemberDisplay.fromAUserInAServer(meetup.getOrganizer()))
                .meetupIcsModel(getMeetupICSModel(meetup))
                .build();
    }

    private List<MemberDisplay> getMemberDisplays(List<MeetupParticipant> participants) {
        return participants
                .stream()
                .map(meetupParticipator -> MemberDisplay.fromAUserInAServer(meetupParticipator.getParticipator()))
                .collect(Collectors.toList());
    }

    public MessageToSend getMeetupMessage(MeetupMessageModel model, Long serverId) {
        return templateService.renderEmbedTemplate(MEETUP_DISPLAY_TEMPLATE, model, serverId);
    }

    public CompletableFuture<Void> cancelMeetup(Meetup meetup) {
        Long serverId = meetup.getServer().getId();
        Long meetupId = meetup.getId().getId();
        GuildMessageChannel channel = channelService.getMessageChannelFromServer(serverId, meetup.getMeetupChannel().getId());
        MeetupMessageModel model = getMeetupMessageModel(meetup);
        List<String> componentPayloads = meetup
                .getMeetupComponents()
                .stream()
                .map(meetupComponent -> meetupComponent.getId().getComponentId())
                .collect(Collectors.toList());
        model.setCancelled(true);
        MessageToSend meetupMessage = getMeetupMessage(model, serverId);
        return messageService.editMessageInChannel(channel, meetupMessage, meetup.getMessageId())
                .thenAccept(unused -> self.notifyParticipants(meetupId, serverId))
                .thenAccept(unused -> self.cleanupMeetup(meetupId, serverId, componentPayloads));
    }

    @Transactional
    public void notifyParticipants(Long meetupId, Long serverId) {
        Meetup meetup = meetupManagementServiceBean.getMeetup(meetupId, serverId);
        MeetupMessageModel model = getMeetupMessageModel(meetup);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(MEETUP_CANCELLATION_TEMPLATE, model, serverId);
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
                            .thenAccept(message -> log.info("Notified user {} about cancellation of meetup {} in server {}.", userId, meetupId, serverId))
                            .exceptionally(throwable -> {
                                log.warn("Failed to notify user {} about cancellation of meetup {} in server {}.", userId, meetupId, serverId);
                                return null;
                            });
                });
    }

    @Transactional
    public void cleanupMeetup(Long meetupId, Long serverId, List<String> componentPayloads) {
        Meetup meetup = meetupManagementServiceBean.getMeetup(meetupId, serverId);
        meetup.setState(MeetupState.CANCELLED);
        meetupComponentManagementServiceBean.deleteAllComponents(meetup);
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

    public CompletableFuture<Void> notifyMeetupParticipants(Meetup meetup, String message, List<MeetupDecision> toNotify) {
        List<MeetupDecision> decisionsToBeNotified = toNotify == null || toNotify.isEmpty() ? Arrays.asList(MeetupDecision.MAYBE, MeetupDecision.YES) : toNotify;
        List<MemberDisplay> participants = meetup
                .getParticipants()
                .stream()
                .filter(meetupParticipator -> decisionsToBeNotified.contains(meetupParticipator.getDecision()))
                .map(meetupParticipator -> MemberDisplay.fromAUserInAServer(meetupParticipator.getParticipator()))
                .collect(Collectors.toList());

        MeetupNotificationModel model = MeetupNotificationModel
                .builder()
                .notificationMessage(message)
                .meetupId(meetup.getId().getId())
                .meetupMessageId(meetup.getMessageId())
                .meetupTopic(meetup.getTopic())
                .participants(participants)
                .build();
        MessageChannel channel = channelService.getMessageChannelFromServer(meetup.getServer().getId(), meetup.getMeetupChannel().getId());
        MessageToSend messageToSend = templateService.renderEmbedTemplate("notifyMeetupParticipants_notification_message", model, meetup.getServer().getId());
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, channel));
    }

    @Transactional
    public void remindParticipants(Long meetupId, Long serverId) {
        Meetup meetup = meetupManagementServiceBean.getMeetup(meetupId, serverId);
        MeetupMessageModel model = getMeetupMessageModel(meetup);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(MEETUP_REMINDER_TEMPLATE, model, serverId);
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
        List<Meetup> oldMeetups = meetupManagementServiceBean.getMeetupsOlderThan(time);
        log.info("Deleting {} old meetups.", oldMeetups.size());
        deleteMeetups(oldMeetups);
        List<Meetup> cancelledMeetups = meetupManagementServiceBean.findCancelledMeetups();
        log.info("Deleting {} cancelled meetups.", cancelledMeetups.size());
        deleteMeetups(cancelledMeetups);
    }

    private void deleteMeetups(List<Meetup> oldMeetups) {
        oldMeetups.forEach(meetup -> {
            if(meetup.getMessageId() != null) {
                Long messageId = meetup.getMessageId();
                Long meetupId = meetup.getId().getId();
                Long serverId = meetup.getServer().getId();
                messageService.deleteMessageInChannelInServer(meetup.getServer().getId(), meetup.getMeetupChannel().getId(), meetup.getMessageId())
                        .exceptionally(throwable -> {
                            log.error("Failed to delete message {} for meetup {} in server {}.", messageId, meetupId, serverId);
                            return null;
                        });
            }
            meetupComponentManagementServiceBean.deleteAllComponents(meetup);
        });
        meetupManagementServiceBean.deleteMeetups(oldMeetups);
    }

    public CompletableFuture<Void> changeMeetupTimeAndNotifyParticipants(Meetup meetup, Instant newTime) {
        List<MeetupDecision> decisions = Arrays.asList(MeetupDecision.MAYBE, MeetupDecision.NO_TIME, MeetupDecision.YES);
        List<MeetupParticipant> participants = meetup
                .getParticipants()
                .stream()
                .filter(meetupParticipator -> decisions.contains(meetupParticipator.getDecision()))
                .collect(Collectors.toList());
        List<Long> userIdsToNotify = participants
                .stream()
                .map(meetupParticipator -> meetupParticipator.getParticipator().getUserReference().getId())
                .toList();

        Long serverId = meetup.getServer().getId();

        ServerChannelMessage meetupMessage = ServerChannelMessage
                .builder()
                .serverId(serverId)
                .channelId(meetup.getMeetupChannel().getId())
                .messageId(meetup.getMessageId())
                .build();
        MeetupTimeChangedNotificationModel notificationModel = MeetupTimeChangedNotificationModel
                .builder()
                .meetupDescription(meetup.getDescription())
                .meetupTopic(meetup.getTopic())
                .meetupMessage(meetupMessage)
                .newDate(newTime)
                .oldDate(meetup.getMeetupTime())
                .build();

        if(meetup.getEarlyReminderJobTriggerKey() != null) {
            schedulerService.stopTrigger(meetup.getEarlyReminderJobTriggerKey());
        }
        if(meetup.getLateReminderJobTriggerKey() != null) {
            schedulerService.stopTrigger(meetup.getLateReminderJobTriggerKey());
        }
        // set the new time here, so that we can use it in schedule
        meetup.setMeetupTime(newTime);
        scheduleReminders(meetup);

        MessageToSend messageToSend = templateService.renderEmbedTemplate(MEETUP_CHANGE_TIME_NOTIFICATION_TEMPLATE, notificationModel, serverId);
        Long meetupId = meetup.getId().getId();
        userIdsToNotify.forEach(userId -> {
            userService.retrieveUserForId(userId).thenCompose(user -> messageService.sendMessageToSendToUser(user, messageToSend)
                    .exceptionally(throwable -> {
                        log.warn("Failed to notify user {} about changed time of meetup {} in server {}.", userId, meetupId, serverId);
                        return null;
                    }));
        });

        meetupParticipatorManagementServiceBean.deleteParticipants(participants);
        List<Long> userInServerIds = participants
                .stream()
                .map(meetupParticipant -> meetupParticipant.getParticipator().getUserInServerId())
                .toList();
        meetup
                .getParticipants().removeIf(meetupParticipant -> userInServerIds.contains(meetupParticipant.getParticipator().getUserInServerId()));
        MeetupMessageModel meetupMessageModel = getMeetupMessageModel(meetup);
        meetupMessageModel.setParticipants(new ArrayList<>());
        meetupMessageModel.setMaybeParticipants(new ArrayList<>());
        meetupMessageModel.setNoTimeParticipants(new ArrayList<>());

        MessageToSend updatedMeetupMessage = getMeetupMessage(meetupMessageModel, serverId);
        GuildMessageChannel meetupChannel = channelService.getMessageChannelFromServer(serverId, meetup.getMeetupChannel().getId());
        return channelService.editMessageInAChannelFuture(updatedMeetupMessage, meetupChannel, meetup.getMessageId())
                .thenAccept(message -> log.info("Updated message of meetup {} in channel {} in server {}.", meetupId, meetup.getMeetupChannel().getId(), serverId))
                .thenAccept(unused -> fileService.safeDeleteIgnoreException(updatedMeetupMessage.getAttachedFiles().get(0).getFile()))
                .exceptionally(throwable -> {
                    log.info("Failed to update message of meetup {} in channel {} in server {}.", meetupId, meetup.getMeetupChannel().getId(), serverId, throwable);
                    return null;
                });
    }

    public CompletableFuture<Void> changeMeetupDescription(Meetup meetup, String newDescription) {
        meetup.setDescription(newDescription);
        return updateMeetupMessage(meetup);
    }

    public CompletableFuture<Void> changeMeetupLocation(Meetup meetup, String newLocation) {
        try {
            meetup.setLocation(URLEncoder.encode(newLocation, StandardCharsets.UTF_8.toString()));
        } catch (UnsupportedEncodingException e) {
            throw new AbstractoRunTimeException(e);
        }
        return updateMeetupMessage(meetup);
    }

    public CompletableFuture<Void> changeMeetupTopic(Meetup meetup, String newTopic) {
        meetup.setTopic(newTopic);
        return updateMeetupMessage(meetup);
    }

    private CompletableFuture<Void> updateMeetupMessage(Meetup meetup) {
        Long meetupId = meetup.getId().getId();
        Long serverId = meetup.getId().getServerId();
        MeetupMessageModel meetupMessageModel = getMeetupMessageModel(meetup);
        MessageToSend updatedMeetupMessage = getMeetupMessage(meetupMessageModel, serverId);
        GuildMessageChannel meetupChannel = channelService.getMessageChannelFromServer(serverId, meetup.getMeetupChannel().getId());
        return channelService.editMessageInAChannelFuture(updatedMeetupMessage, meetupChannel, meetup.getMessageId())
                .thenAccept(message -> log.info("Updated message of meetup {} in channel {} in server {}.", meetupId, meetup.getMeetupChannel().getId(), serverId))
                .thenAccept(unused -> fileService.safeDeleteIgnoreException(updatedMeetupMessage.getAttachedFiles().get(0).getFile()))
                .exceptionally(throwable -> {
                    log.info("Failed to update message of meetup {} in channel {} in server {}.", meetupId, meetup.getMeetupChannel().getId(), serverId, throwable);
                    return null;
                });
    }
}
