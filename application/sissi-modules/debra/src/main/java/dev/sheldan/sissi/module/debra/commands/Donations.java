package dev.sheldan.sissi.module.debra.commands;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.sissi.module.debra.config.DebraFeatureDefinition;
import dev.sheldan.sissi.module.debra.config.DebraSlashCommandNames;
import dev.sheldan.sissi.module.debra.converter.DonationConverter;
import dev.sheldan.sissi.module.debra.model.api.DonationsResponse;
import dev.sheldan.sissi.module.debra.model.commands.DonationsModel;
import dev.sheldan.sissi.module.debra.service.DonationService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Component
public class Donations extends AbstractConditionableCommand {

    private static final String DONATIONS_COMMAND_NAME = "donations";
    private static final String DONATIONS_RESPONSE_TEMPLATE_KEY = "donations_response";
    private static final String SELECTION_PARAMETER = "type";
    private static final String SELECTION_VALUE_PARAMETER = "parametervalue";

    @Autowired
    private ChannelService channelService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private DonationService donationService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private DonationConverter donationConverter;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        MessageToSend messageToSend;
        if(parameters.isEmpty()) {
            messageToSend = getDonationMessageToSend(commandContext.getGuild().getIdLong(), null, null);
        } else {
            String type = (String) parameters.get(0);
            Integer selectionValue = (Integer) parameters.get(1);
            Integer top = null;
            Integer latest = null;
            switch (type) {
                case "top": top = selectionValue; break;
                default:
                case "latest" :
                    latest = selectionValue; break;
            }
            messageToSend = getDonationMessageToSend(commandContext.getGuild().getIdLong(), top, latest);
        }
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String selectionType = null;
        if(slashCommandParameterService.hasCommandOption(SELECTION_PARAMETER, event)) {
            selectionType = slashCommandParameterService.getCommandOption(SELECTION_PARAMETER, event, String.class);
        }
        Integer selectionValue = 5;
        if(slashCommandParameterService.hasCommandOption(SELECTION_VALUE_PARAMETER, event)) {
            selectionValue = slashCommandParameterService.getCommandOption(SELECTION_VALUE_PARAMETER, event, Integer.class);
        }
        if(selectionValue > 20) {
            selectionValue = 5;
        }
        Integer top = null;
        Integer latest = null;
        if(selectionType != null) {
            switch (selectionType) {
                case "top": top = selectionValue; break;
                default:
                case "latest" :
                    latest = selectionValue; break;
            }
        }

        MessageToSend messageToSend = getDonationMessageToSend(event.getGuild().getIdLong(), top, latest);
        return interactionService.replyMessageToSend(messageToSend, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    private MessageToSend getDonationMessageToSend(Long serverId, Integer top, Integer latest) {
        DonationsModel donationModel;
        DonationsResponse donationResponse = donationService.fetchCurrentDonationAmount(serverId);
        donationModel = donationConverter.convertDonationResponse(donationResponse);
        if(top != null) {
            donationModel.setDonations(donationService.getHighestDonations(donationResponse, top));
            donationModel.setType(DonationsModel.DonationType.TOP);
        } else if(latest != null) {
            donationModel.setType(DonationsModel.DonationType.LATEST);
            donationModel.setDonations(donationService.getLatestDonations(donationResponse, latest));
        } else {
            donationModel.setDonations(new ArrayList<>());
        }
        return templateService.renderEmbedTemplate(DONATIONS_RESPONSE_TEMPLATE_KEY, donationModel, serverId);
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(DebraSlashCommandNames.DEBRA)
                .commandName(DONATIONS_COMMAND_NAME)
                .build();

        Parameter selectionParameter = Parameter
                .builder()
                .templated(true)
                .name(SELECTION_PARAMETER)
                .optional(true)
                .type(String.class)
                .build();


        Parameter selectionValueParameter = Parameter
                .builder()
                .templated(true)
                .name(SELECTION_VALUE_PARAMETER)
                .optional(true)
                .type(Integer.class)
                .build();

        List<Parameter> parameters = Arrays.asList(selectionParameter, selectionValueParameter);

        return CommandConfiguration.builder()
                .name(DONATIONS_COMMAND_NAME)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .parameters(parameters)
                .supportsEmbedException(true)
                .causesReaction(false)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return DebraFeatureDefinition.DEBRA;
    }
}
