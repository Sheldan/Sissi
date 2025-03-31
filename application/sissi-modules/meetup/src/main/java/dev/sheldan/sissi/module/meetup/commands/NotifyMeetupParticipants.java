package dev.sheldan.sissi.module.meetup.commands;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.sissi.module.meetup.config.MeetupFeatureDefinition;
import dev.sheldan.sissi.module.meetup.config.MeetupSlashCommandNames;
import dev.sheldan.sissi.module.meetup.exception.NotMeetupOrganizerException;
import dev.sheldan.sissi.module.meetup.model.database.Meetup;
import dev.sheldan.sissi.module.meetup.model.database.MeetupDecision;
import dev.sheldan.sissi.module.meetup.service.MeetupServiceBean;
import dev.sheldan.sissi.module.meetup.service.management.MeetupManagementServiceBean;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class NotifyMeetupParticipants extends AbstractConditionableCommand {

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private MeetupManagementServiceBean meetupManagementServiceBean;

    @Autowired
    private MeetupServiceBean meetupServiceBean;

    @Autowired
    private InteractionService interactionService;

    private static final String MEETUP_ID_PARAMETER = "meetupId";
    private static final String NOTIFICATION_MESSAGE_PARAMETER = "notificationMessage";
    private static final String NOTIFICATION_MEETUP_DECISION = "decision";
    private static final String NOTIFY_MEETUP_PARTICIPANTS_COMMAND = "notifyMeetupParticipants";
    private static final String NOTIFY_MEETUP_PARTICIPANTS_RESPONSE = "notifyMeetupParticipants_response";

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long meetupId = slashCommandParameterService.getCommandOption(MEETUP_ID_PARAMETER, event, Integer.class).longValue();
        Meetup meetup = meetupManagementServiceBean.getMeetup(meetupId, event.getGuild().getIdLong());
        if(!meetup.getOrganizer().getUserReference().getId().equals(event.getMember().getIdLong())) {
            throw new NotMeetupOrganizerException();
        }
        List<MeetupDecision> decisionsToNotify = new ArrayList<>();
        for (int i = 0; i < MeetupDecision.values().length; i++) {
            if(slashCommandParameterService.hasCommandOption(NOTIFICATION_MEETUP_DECISION + "_" + i, event)) {
                String choice = slashCommandParameterService.getCommandOption(NOTIFICATION_MEETUP_DECISION + "_" + i, event, String.class);
                decisionsToNotify.add(MeetupDecision.valueOf(choice));
            }
        }
        String notificationMessage = slashCommandParameterService.getCommandOption(NOTIFICATION_MESSAGE_PARAMETER, event, String.class);
        return meetupServiceBean.notifyMeetupParticipants(meetup, notificationMessage, decisionsToNotify)
                .thenCompose(unused -> interactionService.replyEmbed(NOTIFY_MEETUP_PARTICIPANTS_RESPONSE, event))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter meetupIdParameter = Parameter
                .builder()
                .templated(true)
                .name(MEETUP_ID_PARAMETER)
                .type(Long.class)
                .build();

        Parameter notificationMessage = Parameter
                .builder()
                .templated(true)
                .name(NOTIFICATION_MESSAGE_PARAMETER)
                .type(String.class)
                .remainder(true)
                .build();

        List<String> meetupDecisions = Arrays
                .stream(MeetupDecision.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        Parameter meetupDecisionChoice = Parameter
                .builder()
                .templated(true)
                .name(NOTIFICATION_MEETUP_DECISION)
                .type(String.class)
                .listSize(MeetupDecision.values().length)
                .isListParam(true)
                .optional(true)
                .choices(meetupDecisions)
                .slashCommandOnly(true)
                .build();

        List<Parameter> parameters = Arrays.asList(meetupIdParameter, notificationMessage, meetupDecisionChoice);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(MeetupSlashCommandNames.MEETUP)
                .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
                .commandName("notify")
                .build();

        return CommandConfiguration.builder()
                .name(NOTIFY_MEETUP_PARTICIPANTS_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .slashCommandOnly(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return MeetupFeatureDefinition.MEETUP;
    }
}
