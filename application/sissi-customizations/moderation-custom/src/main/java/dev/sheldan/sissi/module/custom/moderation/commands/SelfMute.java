package dev.sheldan.sissi.module.custom.moderation.commands;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.sissi.module.custom.moderation.config.ModerationCustomFeatureDefinition;
import dev.sheldan.sissi.module.custom.moderation.service.SelfMuteServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class SelfMute extends AbstractConditionableCommand {

    public static final String DURATION_PARAMETER = "duration";

    @Autowired
    private SelfMuteServiceBean selfMuteServiceBean;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Duration muteDuration = (Duration) parameters.get(0);
        return selfMuteServiceBean.selfMuteMember(commandContext.getAuthor(), muteDuration)
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter durationParameter = Parameter
                .builder()
                .name(DURATION_PARAMETER)
                .type(Duration.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(durationParameter);

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        return CommandConfiguration.builder()
                .name("selfMute")
                .async(true)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationCustomFeatureDefinition.MODERATION_CUSTOM;
    }
}
