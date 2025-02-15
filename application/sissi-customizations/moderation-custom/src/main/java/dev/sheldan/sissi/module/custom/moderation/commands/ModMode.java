package dev.sheldan.sissi.module.custom.moderation.commands;

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
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.sissi.module.custom.moderation.config.ModerationCustomFeatureDefinition;
import dev.sheldan.sissi.module.custom.moderation.config.ModerationCustomSlashCommandNames;
import dev.sheldan.sissi.module.custom.moderation.service.ModModeServiceBean;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ModMode extends AbstractConditionableCommand {

    public static final String NEW_STATE_PARAMETER = "newState";
    public static final String MOD_MODE_COMMAND = "modMode";
    public static final String MOD_MODE_RESPONSE = "modMode_response";

    @Autowired
    private ModModeServiceBean modModeServiceBean;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        Boolean newState = (Boolean) commandContext.getParameters().getParameters().get(0);
        return modModeServiceBean.setModModeTo(commandContext.getGuild(), newState)
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Boolean newState = slashCommandParameterService.getCommandOption(NEW_STATE_PARAMETER, event, Boolean.class);
        return modModeServiceBean.setModModeTo(event.getGuild(), newState)
                .thenApply(unused -> interactionService.replyEmbed(MOD_MODE_RESPONSE, event))
                .thenApply(interactionHookCompletableFuture -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {

        Parameter memberParameter = Parameter
                .builder()
                .templated(true)
                .name(NEW_STATE_PARAMETER)
                .type(Boolean.class)
                .build();
        List<Parameter> parameters = Collections.singletonList(memberParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModerationCustomSlashCommandNames.MODERATION)
                .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
                .commandName(MOD_MODE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(MOD_MODE_COMMAND)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .module(ModerationModuleDefinition.MODERATION)
                .parameters(parameters)
                .help(helpInfo)
                .templated(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationCustomFeatureDefinition.MODERATION_CUSTOM;
    }
}
