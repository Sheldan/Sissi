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
import dev.sheldan.sissi.module.meetup.service.MeetupServiceBean;
import dev.sheldan.sissi.module.meetup.service.management.MeetupManagementServiceBean;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class ChangeMeetup extends AbstractConditionableCommand {

    private static final String CHANGE_MEETUP_COMMAND = "changeMeetup";
    private static final String MEETUP_ID_PARAMETER = "meetupId";
    private static final String MEETUP_NEW_VALUE_PARAMETER = "newValue";
    private static final String MEETUP_PROPERTY_PARAMETER = "property";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private MeetupManagementServiceBean meetupManagementServiceBean;

    @Autowired
    private MeetupServiceBean meetupServiceBean;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Long meetupId = (Long) parameters.get(0);
        Meetup meetup = meetupManagementServiceBean.getMeetup(meetupId, commandContext.getGuild().getIdLong());
        if(!meetup.getOrganizer().getUserReference().getId().equals(commandContext.getAuthor().getIdLong())) {
            throw new NotMeetupOrganizerException();
        }
        String property = (String) parameters.get(1);
        MeetupProperty propertyEnum = MeetupProperty.valueOf(property);
        String newValue = (String) parameters.get(2);
        return updateMeetup(meetup, propertyEnum, newValue).thenApply(unused -> CommandResult.fromSuccess());
    }

    private CompletableFuture<Void> updateMeetup(Meetup meetup, MeetupProperty propertyEnum, String newValue) {
        CompletableFuture<Void> future;
        switch (propertyEnum) {
            case TOPIC:
                future = meetupServiceBean.changeMeetupTopic(meetup, newValue);
                break;
            case LOCATION:
                future = meetupServiceBean.changeMeetupLocation(meetup, newValue);
                break;
            default:
                case DESCRIPTION:
                future = meetupServiceBean.changeMeetupDescription(meetup, newValue);
                break;
        }
        return future;
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long meetupId = slashCommandParameterService.getCommandOption(MEETUP_ID_PARAMETER, event, Integer.class).longValue();
        Meetup meetup = meetupManagementServiceBean.getMeetup(meetupId, event.getGuild().getIdLong());
        if(!meetup.getOrganizer().getUserReference().getId().equals(event.getMember().getIdLong())) {
            throw new NotMeetupOrganizerException();
        }
        String newValue = slashCommandParameterService.getCommandOption(MEETUP_NEW_VALUE_PARAMETER, event, String.class);
        String property = slashCommandParameterService.getCommandOption(MEETUP_PROPERTY_PARAMETER, event, String.class);
        MeetupProperty propertyEnum = MeetupProperty.valueOf(property);
        return updateMeetup(meetup, propertyEnum, newValue)
                .thenCompose(commandResult -> interactionService.replyEmbed("changeMeetup_response", event))
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

        List<String> meetupProperties = Arrays
                .stream(MeetupProperty.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        Parameter meetupPropertyParameter = Parameter
                .builder()
                .templated(true)
                .name(MEETUP_PROPERTY_PARAMETER)
                .type(String.class)
                .choices(meetupProperties)
                .build();

        Parameter newValueParameter = Parameter
                .builder()
                .templated(true)
                .name(MEETUP_NEW_VALUE_PARAMETER)
                .type(String.class)
                .build();

        List<Parameter> parameters = Arrays.asList(meetupIdParameter, meetupPropertyParameter, newValueParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(MeetupSlashCommandNames.MEETUP)
                .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
                .commandName("changeMeetup")
                .build();

        return CommandConfiguration.builder()
                .name(CHANGE_MEETUP_COMMAND)
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

    public enum MeetupProperty {
        DESCRIPTION, TOPIC, LOCATION
    }
}
