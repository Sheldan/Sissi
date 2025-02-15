package dev.sheldan.sissi.module.miepscord.weeklytext.commands;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.sissi.module.miepscord.MiepscordFeatureDefinition;
import dev.sheldan.sissi.module.miepscord.MiepscordSlashCommandNames;
import dev.sheldan.sissi.module.miepscord.weeklytext.service.TextItemServiceBean;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Component
public class RemoveTextItem extends AbstractConditionableCommand {

    private static final String REMOVE_WEEKLY_TEXT_ID_PARAMETER = "id";
    private static final String REMOVE_WEEKLY_TEXT_COMMAND_NAME = "removeWeeklyText";

    private static final String REMOVE_WEEKLY_TEXT_RESPONSE = "removeWeeklyText_response";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private TextItemServiceBean textItemServiceBean;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long itemId = slashCommandParameterService.getCommandOption(REMOVE_WEEKLY_TEXT_ID_PARAMETER, event,  Integer.class).longValue();
        textItemServiceBean.removeTextItem(itemId);
        return interactionService.replyEmbed(REMOVE_WEEKLY_TEXT_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter textItemId = Parameter
                .builder()
                .templated(true)
                .name(REMOVE_WEEKLY_TEXT_ID_PARAMETER)
                .type(Long.class)
                .build();

        List<Parameter> parameters = Arrays.asList(textItemId);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(MiepscordSlashCommandNames.MIEPSCORD_ROOT_NAME_CONFIG)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .groupName("weeklytexts")
                .commandName("remove")
                .build();

        return CommandConfiguration.builder()
                .name(REMOVE_WEEKLY_TEXT_COMMAND_NAME)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandOnly(true)
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
        return MiepscordFeatureDefinition.WEEKLY_TEXT;
    }
}
