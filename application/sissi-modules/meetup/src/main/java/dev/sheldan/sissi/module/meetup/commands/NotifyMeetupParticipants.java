package dev.sheldan.sissi.module.meetup.commands;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.sissi.module.meetup.config.MeetupFeatureDefinition;
import dev.sheldan.sissi.module.meetup.config.MeetupSlashCommandNames;
import dev.sheldan.sissi.module.meetup.exception.NotMeetupOrganizerException;
import dev.sheldan.sissi.module.meetup.model.database.Meetup;
import dev.sheldan.sissi.module.meetup.service.MeetupServiceBean;
import dev.sheldan.sissi.module.meetup.service.management.MeetupManagementServiceBean;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    private static final String NOTIFY_MEETUP_PARTICIPANTS_COMMAND = "notifyMeetupParticipants";
    private static final String NOTIFY_MEETUP_PARTICIPANTS_RESPONSE = "notifyMeetupParticipants_response";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Long meetupId = (Long) parameters.get(0);
        Meetup meetup = meetupManagementServiceBean.getMeetup(meetupId, commandContext.getGuild().getIdLong());
        if(!meetup.getOrganizer().getUserReference().getId().equals(commandContext.getAuthor().getIdLong())) {
            throw new NotMeetupOrganizerException();
        }
        String notificationMessage = (String) parameters.get(1);
        return meetupServiceBean.notifyMeetupParticipants(meetup, notificationMessage)
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long meetupId = slashCommandParameterService.getCommandOption(MEETUP_ID_PARAMETER, event, Integer.class).longValue();
        Meetup meetup = meetupManagementServiceBean.getMeetup(meetupId, event.getGuild().getIdLong());
        if(!meetup.getOrganizer().getUserReference().getId().equals(event.getMember().getIdLong())) {
            throw new NotMeetupOrganizerException();
        }
        String notificationMessage = slashCommandParameterService.getCommandOption(NOTIFICATION_MESSAGE_PARAMETER, event, String.class);
        return meetupServiceBean.notifyMeetupParticipants(meetup, notificationMessage)
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

        List<Parameter> parameters = Arrays.asList(meetupIdParameter, notificationMessage);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(MeetupSlashCommandNames.MEETUP)
                .commandName("notify")
                .build();

        return CommandConfiguration.builder()
                .name(NOTIFY_MEETUP_PARTICIPANTS_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
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
