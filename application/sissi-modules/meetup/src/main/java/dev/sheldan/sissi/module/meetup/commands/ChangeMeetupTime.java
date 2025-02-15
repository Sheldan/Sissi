package dev.sheldan.sissi.module.meetup.commands;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.sissi.module.meetup.config.MeetupFeatureDefinition;
import dev.sheldan.sissi.module.meetup.config.MeetupSlashCommandNames;
import dev.sheldan.sissi.module.meetup.exception.MeetupPastTimeException;
import dev.sheldan.sissi.module.meetup.exception.NotMeetupOrganizerException;
import dev.sheldan.sissi.module.meetup.model.command.MeetupChangeTimeConfirmationModel;
import dev.sheldan.sissi.module.meetup.model.database.Meetup;
import dev.sheldan.sissi.module.meetup.service.MeetupServiceBean;
import dev.sheldan.sissi.module.meetup.service.management.MeetupManagementServiceBean;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ChangeMeetupTime extends AbstractConditionableCommand {

    private static final String CHANGE_MEETUP_TIME_COMMAND = "changeMeetupTime";
    private static final String MEETUP_ID_PARAMETER = "meetupId";
    private static final String MEETUP_NEW_TIMESTAMP_PARAMETER = "newTimeStamp";
    private static final String CHANGE_MEETUP_TIME_CONFIRMATION = "changeMeetupTime_confirmation";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private MeetupManagementServiceBean meetupManagementServiceBean;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private MeetupServiceBean meetupServiceBean;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Long meetupId = (Long) parameters.get(0);
        Meetup meetup = meetupManagementServiceBean.getMeetup(meetupId, commandContext.getGuild().getIdLong());
        if(!meetup.getOrganizer().getUserReference().getId().equals(commandContext.getAuthor().getIdLong())) {
            throw new NotMeetupOrganizerException();
        }
        Long newTimestamp = (Long) parameters.get(1);
        Instant newMeetupTime = Instant.ofEpochSecond(newTimestamp);
        if(newMeetupTime.isBefore(Instant.now())) {
            throw new MeetupPastTimeException();
        }
        String confirmationId = componentService.generateComponentId();
        String cancelId = componentService.generateComponentId();
        MeetupChangeTimeConfirmationModel model = MeetupChangeTimeConfirmationModel
                .builder()
                .meetupTime(newMeetupTime)
                .topic(meetup.getTopic())
                .description(meetup.getDescription())
                .userId(commandContext.getAuthor().getIdLong())
                .guildId(commandContext.getGuild().getIdLong())
                .meetupId(meetupId)
                .confirmationId(confirmationId)
                .cancelId(cancelId)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(CHANGE_MEETUP_TIME_CONFIRMATION, model, commandContext.getGuild().getIdLong());
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                .thenAccept(unused -> meetupServiceBean.storeMeetupChangeTimeConfirmation(model))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long meetupId = slashCommandParameterService.getCommandOption(MEETUP_ID_PARAMETER, event, Integer.class).longValue();
        Meetup meetup = meetupManagementServiceBean.getMeetup(meetupId, event.getGuild().getIdLong());
        if(!meetup.getOrganizer().getUserReference().getId().equals(event.getMember().getIdLong())) {
            throw new NotMeetupOrganizerException();
        }
        Integer time = slashCommandParameterService.getCommandOption(MEETUP_NEW_TIMESTAMP_PARAMETER, event, Long.class, Integer.class);
        Instant meetupTime = Instant.ofEpochSecond(time);
        if(meetupTime.isBefore(Instant.now())) {
            throw new MeetupPastTimeException();
        }
        String confirmationId = componentService.generateComponentId();
        String cancelId = componentService.generateComponentId();
        MeetupChangeTimeConfirmationModel model = MeetupChangeTimeConfirmationModel
                .builder()
                .meetupTime(meetupTime)
                .topic(meetup.getTopic())
                .description(meetup.getDescription())
                .userId(event.getMember().getIdLong())
                .guildId(event.getGuild().getIdLong())
                .meetupId(meetupId)
                .confirmationId(confirmationId)
                .cancelId(cancelId)
                .build();
        return interactionService.replyEmbed(CHANGE_MEETUP_TIME_CONFIRMATION, model, event)
                .thenAccept(interactionHook -> meetupServiceBean.storeMeetupChangeTimeConfirmation(model))
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter meetupIdParameter = Parameter
                .builder()
                .templated(true)
                .name(MEETUP_ID_PARAMETER)
                .type(Long.class)
                .build();

        Parameter newTimeStampParameter = Parameter
                .builder()
                .templated(true)
                .name(MEETUP_NEW_TIMESTAMP_PARAMETER)
                .type(Long.class)
                .build();

        List<Parameter> parameters = Arrays.asList(meetupIdParameter, newTimeStampParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(MeetupSlashCommandNames.MEETUP)
                .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
                .commandName("changeTime")
                .build();

        return CommandConfiguration.builder()
                .name(CHANGE_MEETUP_TIME_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
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
