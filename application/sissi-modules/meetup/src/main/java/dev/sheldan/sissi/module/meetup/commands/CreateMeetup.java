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
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.sissi.module.meetup.config.MeetupFeatureDefinition;
import dev.sheldan.sissi.module.meetup.config.MeetupSlashCommandNames;
import dev.sheldan.sissi.module.meetup.model.command.MeetupConfirmationModel;
import dev.sheldan.sissi.module.meetup.model.database.Meetup;
import dev.sheldan.sissi.module.meetup.service.MeetupServiceBean;
import dev.sheldan.sissi.module.meetup.service.management.MeetupManagementServiceBean;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class CreateMeetup extends AbstractConditionableCommand {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private MeetupServiceBean meetupServiceBean;

    @Autowired
    private MeetupManagementServiceBean meetupManagementServiceBean;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    private static final String CREATE_MEETUP_COMMAND = "createMeetup";
    private static final String MEETUP_TIME_PARAMETER = "meetupTime";
    private static final String TOPIC_PARAMETER = "topic";
    private static final String DESCRIPTION_PARAMETER = "description";
    private static final String LOCATION_PARAMETER = "location";
    private static final String CONFIRMATION_TEMPLATE = "createMeetup_confirmation";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Instant meetupTime = (Instant) parameters.get(0);
        String meetupTopic = (String) parameters.get(1);
        String description;
        if(parameters.size() > 2) {
            description = (String) parameters.get(2);
        } else {
            description = "";
        }
        AUserInAServer organizer = userInServerManagementService.loadOrCreateUser(commandContext.getAuthor());
        AChannel meetupChannel = channelManagementService.loadChannel(commandContext.getChannel().getIdLong());
        Meetup meetup = meetupManagementServiceBean.createMeetup(meetupTime, meetupTopic, description, organizer, meetupChannel, null);
        String confirmationId = componentService.generateComponentId();
        String cancelId = componentService.generateComponentId();
        MeetupConfirmationModel model = MeetupConfirmationModel
                .builder()
                .meetupTime(meetupTime)
                .guildId(commandContext.getGuild().getIdLong())
                .description(description)
                .topic(meetupTopic)
                .location(meetup.getLocation())
                .confirmationId(confirmationId)
                .cancelId(cancelId)
                .meetupId(meetup.getId().getId())
                .organizer(MemberDisplay.fromMember(commandContext.getAuthor()))
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(CONFIRMATION_TEMPLATE, model, commandContext.getGuild().getIdLong());
        List<CompletableFuture<Message>> messageFutures = channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel());
        return FutureUtils.toSingleFutureGeneric(messageFutures)
                .thenAccept(unused -> meetupServiceBean.storeMeetupConfirmation(model))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Integer time = slashCommandParameterService.getCommandOption(MEETUP_TIME_PARAMETER, event, Long.class, Integer.class);
        String topic = slashCommandParameterService.getCommandOption(TOPIC_PARAMETER, event, String.class);
        String description;
        if(slashCommandParameterService.hasCommandOption(DESCRIPTION_PARAMETER, event)) {
            description = slashCommandParameterService.getCommandOption(DESCRIPTION_PARAMETER, event, String.class);
        } else {
            description = "";
        }

        String location;
        if(slashCommandParameterService.hasCommandOption(LOCATION_PARAMETER, event)) {
            location = slashCommandParameterService.getCommandOption(LOCATION_PARAMETER, event, String.class);
        } else {
            location = null;
        }
        Instant meetupTime = Instant.ofEpochSecond(time);
        AUserInAServer organizer = userInServerManagementService.loadOrCreateUser(event.getMember());
        AChannel meetupChannel = channelManagementService.loadChannel(event.getChannel().getIdLong());
        Meetup meetup = meetupManagementServiceBean.createMeetup(meetupTime, topic, description, organizer, meetupChannel, location);
        String confirmationId = componentService.generateComponentId();
        String cancelId = componentService.generateComponentId();
        MeetupConfirmationModel model = MeetupConfirmationModel
                .builder()
                .meetupTime(meetupTime)
                .guildId(event.getGuild().getIdLong())
                .description(description)
                .topic(topic)
                .location(meetup.getLocation())
                .confirmationId(confirmationId)
                .cancelId(cancelId)
                .meetupId(meetup.getId().getId())
                .organizer(MemberDisplay.fromMember(event.getMember()))
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(CONFIRMATION_TEMPLATE, model, event.getGuild().getIdLong());
        return interactionService.replyMessageToSend(messageToSend, event)
                .thenAccept(interactionHook -> meetupServiceBean.storeMeetupConfirmation(model))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {

        Parameter timeParameter = Parameter
                .builder()
                .templated(true)
                .name(MEETUP_TIME_PARAMETER)
                .type(Instant.class)
                .build();

        Parameter topicParameter = Parameter
                .builder()
                .templated(true)
                .name(TOPIC_PARAMETER)
                .type(String.class)
                .build();

        Parameter descriptionParameter = Parameter
                .builder()
                .templated(true)
                .name(DESCRIPTION_PARAMETER)
                .remainder(true)
                .optional(true)
                .type(String.class)
                .build();

        Parameter locationParameter = Parameter
                .builder()
                .templated(true)
                .name(LOCATION_PARAMETER)
                .remainder(true)
                .optional(true)
                .slashCommandOnly(true)
                .type(String.class)
                .build();

        List<Parameter> parameters = Arrays.asList(timeParameter, topicParameter, descriptionParameter, locationParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(MeetupSlashCommandNames.MEETUP)
                .commandName("create")
                .build();

        return CommandConfiguration.builder()
                .name(CREATE_MEETUP_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return MeetupFeatureDefinition.MEETUP;
    }
}
