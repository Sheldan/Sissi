package dev.sheldan.sissi.module.quotes.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.sissi.module.quotes.config.QuoteSlashCommandNames;
import dev.sheldan.sissi.module.quotes.config.QuotesFeatureDefinition;
import dev.sheldan.sissi.module.quotes.config.QuotesModuleDefinition;
import dev.sheldan.sissi.module.quotes.service.QuoteServiceBean;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class QuoteDelete extends AbstractConditionableCommand {

    private static final String QUOTE_DELETE_COMMAND = "quoteDelete";
    private static final String QUOTE_ID_PARAMETER = "quoteId";
    private static final String QUOTE_DELETE_RESPONSE = "quoteDelete_response";

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private QuoteServiceBean quoteServiceBean;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Long quoteId = (Long) parameters.get(0);
        AServer server = serverManagementService.loadServer(commandContext.getGuild().getIdLong());
        quoteServiceBean.deleteQuote(quoteId, server);
        return CommandResult.fromSuccess();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long quoteId = slashCommandParameterService.getCommandOption(QUOTE_ID_PARAMETER, event, Integer.class).longValue();
        AServer server = serverManagementService.loadServer(event.getGuild().getIdLong());
        quoteServiceBean.deleteQuote(quoteId, server);
        return interactionService.replyEmbed(QUOTE_DELETE_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter quoteIdParameter = Parameter
                .builder()
                .templated(true)
                .name(QUOTE_ID_PARAMETER)
                .type(Long.class)
                .build();
        List<Parameter> parameters = Collections.singletonList(quoteIdParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(QuoteSlashCommandNames.QUOTE_INTERNAL)
                .commandName("delete")
                .build();

        return CommandConfiguration.builder()
                .name(QUOTE_DELETE_COMMAND)
                .module(QuotesModuleDefinition.QUOTES)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(false)
                .requiresConfirmation(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return QuotesFeatureDefinition.QUOTES;
    }
}
