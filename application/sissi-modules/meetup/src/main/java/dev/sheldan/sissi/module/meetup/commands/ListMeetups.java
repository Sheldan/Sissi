package dev.sheldan.sissi.module.meetup.commands;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.sissi.module.meetup.config.MeetupFeatureDefinition;
import dev.sheldan.sissi.module.meetup.config.MeetupSlashCommandNames;
import dev.sheldan.sissi.module.meetup.model.command.MeetupListItemModel;
import dev.sheldan.sissi.module.meetup.model.command.MeetupListModel;
import dev.sheldan.sissi.module.meetup.model.database.Meetup;
import dev.sheldan.sissi.module.meetup.service.management.MeetupManagementServiceBean;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class ListMeetups extends AbstractConditionableCommand {

    private static final String LIST_MEETUPS_RESPONSE_TEMPLATE = "listMeetups_response";

    @Autowired
    private MeetupManagementServiceBean meetupManagementServiceBean;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        MessageToSend messageToSend = getMessageToSend(commandContext.getGuild().getIdLong());
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        return interactionService.replyMessageToSend(getMessageToSend(event.getGuild().getIdLong()), event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    private MessageToSend getMessageToSend(Long serverId) {
        List<Meetup> meetups = meetupManagementServiceBean.getIncomingMeetups();
        List<MeetupListItemModel> listItems = meetups
                .stream()
                .map(MeetupListItemModel::fromMeetup)
                .sorted(Comparator.comparing(MeetupListItemModel::getMeetupTime))
                .collect(Collectors.toList());
        MeetupListModel model = MeetupListModel
                .builder()
                .meetups(listItems)
                .build();
        return templateService.renderEmbedTemplate(LIST_MEETUPS_RESPONSE_TEMPLATE, model, serverId);
    }

    @Override
    public CommandConfiguration getConfiguration() {

        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(MeetupSlashCommandNames.MEETUP)
                .commandName("list")
                .build();

        return CommandConfiguration.builder()
                .name("listMeetups")
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
