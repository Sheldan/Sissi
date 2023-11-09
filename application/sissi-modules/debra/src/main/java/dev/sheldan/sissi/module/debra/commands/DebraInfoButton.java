package dev.sheldan.sissi.module.debra.commands;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.sissi.module.debra.config.DebraFeatureDefinition;
import dev.sheldan.sissi.module.debra.config.DebraSlashCommandNames;
import dev.sheldan.sissi.module.debra.service.DonationService;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class DebraInfoButton extends AbstractConditionableCommand {

    private static final String DEBRA_INFO_BUTTON = "debraInfoButton";
    private static final String DEBRA_INFO_BUTTON_RESPONSE_TEMPLATE_KEY = "debraInfoButton_response";
    private static final String TARGET_CHANNEL_PARAMETER_KEY = "targetChannel";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private DonationService donationService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        GuildMessageChannel targetChannel = slashCommandParameterService.getCommandOption(TARGET_CHANNEL_PARAMETER_KEY, event, GuildMessageChannel.class);
        return donationService.sendDebraInfoButtonMessage(targetChannel)
                .thenCompose(unused -> interactionService.replyEmbed(DEBRA_INFO_BUTTON_RESPONSE_TEMPLATE_KEY, event))
                .thenApply(interactionHook -> CommandResult.fromSuccess());
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
                .rootCommandName(DebraSlashCommandNames.DEBRA_INTERNAL)
                .commandName("debrainfobutton")
                .build();

        Parameter targetChannelParameter = Parameter
                .builder()
                .templated(true)
                .name(TARGET_CHANNEL_PARAMETER_KEY)
                .type(GuildMessageChannel.class)
                .build();


        List<Parameter> parameters = Arrays.asList(targetChannelParameter);

        return CommandConfiguration.builder()
                .name(DEBRA_INFO_BUTTON)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .slashCommandOnly(true)
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
