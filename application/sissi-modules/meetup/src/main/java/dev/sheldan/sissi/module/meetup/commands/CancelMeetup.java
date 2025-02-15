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
import dev.sheldan.abstracto.core.utils.FutureUtils;
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
public class CancelMeetup extends AbstractConditionableCommand {

    private static final String MEETUP_ID_PARAMETER = "meetupId";
    private static final String CANCEL_MEETUP_COMMAND = "cancelMeetup";
    private static final String CANCEL_MEETUP_RESPONSE = "cancelMeetup_response";

    @Autowired
    private MeetupServiceBean meetupServiceBean;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private MeetupManagementServiceBean meetupManagementServiceBean;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        Long meetupId = (Long) commandContext.getParameters().getParameters().get(0);
        Meetup meetup = meetupManagementServiceBean.getMeetup(meetupId, commandContext.getGuild().getIdLong());
        if(!meetup.getOrganizer().getUserReference().getId().equals(commandContext.getAuthor().getIdLong())) {
            throw new NotMeetupOrganizerException();
        }
        return meetupServiceBean.cancelMeetup(meetup)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Long meetupId = slashCommandParameterService.getCommandOption(MEETUP_ID_PARAMETER, event, Integer.class).longValue();
        Meetup meetup = meetupManagementServiceBean.getMeetup(meetupId, event.getGuild().getIdLong());
        if(!meetup.getOrganizer().getUserReference().getId().equals(event.getMember().getIdLong())) {
            throw new NotMeetupOrganizerException();
        }
        return meetupServiceBean.cancelMeetup(meetup)
                .thenCompose(unused -> FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(CANCEL_MEETUP_RESPONSE, new Object(), event.getHook())))
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

        List<Parameter> parameters = Arrays.asList(meetupIdParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(MeetupSlashCommandNames.MEETUP)
                .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
                .commandName("cancel")
                .build();

        return CommandConfiguration.builder()
                .name(CANCEL_MEETUP_COMMAND)
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
