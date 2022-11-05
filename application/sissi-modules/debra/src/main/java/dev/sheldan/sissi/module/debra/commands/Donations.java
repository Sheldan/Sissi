package dev.sheldan.sissi.module.debra.commands;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.sissi.module.debra.config.DebraFeatureDefinition;
import dev.sheldan.sissi.module.debra.config.DebraSlashCommandNames;
import dev.sheldan.sissi.module.debra.model.commands.DonationsModel;
import dev.sheldan.sissi.module.debra.service.DonationService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;


@Component
public class Donations extends AbstractConditionableCommand {

    private static final String DONATIONS_COMMAND_NAME = "donations";
    private static final String DONATIONS_RESPONSE_TEMPLATE_KEY = "donations_response";

    @Autowired
    private ChannelService channelService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private DonationService donationService;

    @Autowired
    private TemplateService templateService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        MessageToSend messageToSend = getDonationMessageToSend();
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        MessageToSend messageToSend = getDonationMessageToSend();
        return interactionService.replyMessageToSend(messageToSend, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    private MessageToSend getDonationMessageToSend() {
        BigDecimal currentDonationAmount = donationService.fetchCurrentDonationAmount();
        DonationsModel donationModel = DonationsModel
                .builder()
                .donationAmount(currentDonationAmount)
                .build();
        return templateService.renderEmbedTemplate(DONATIONS_RESPONSE_TEMPLATE_KEY, donationModel);
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
                .commandName("donations")
                .build();

        return CommandConfiguration.builder()
                .name(DONATIONS_COMMAND_NAME)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
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
