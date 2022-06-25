package dev.sheldan.sissi.module.quotes.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.sissi.module.quotes.config.QuoteSlashCommandNames;
import dev.sheldan.sissi.module.quotes.config.QuotesFeatureDefinition;
import dev.sheldan.sissi.module.quotes.config.QuotesModuleDefinition;
import dev.sheldan.sissi.module.quotes.exception.QuoteNotFoundException;
import dev.sheldan.sissi.module.quotes.model.database.Quote;
import dev.sheldan.sissi.module.quotes.service.QuoteServiceBean;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class QuoteSearch extends AbstractConditionableCommand {

    private static final String QUOTE_SEARCH_COMMAND = "quoteSearch";
    private static final String QUERY_PARAMETER = "query";

    @Autowired
    private QuoteServiceBean quoteServiceBean;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private QuoteSearch self;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String query = (String) parameters.get(0);
        AServer server = serverManagementService.loadServer(commandContext.getGuild().getIdLong());

        Optional<Quote> possibleQuote = quoteServiceBean.searchQuote(query, server);
        Quote quoteToDisplay = possibleQuote.orElseThrow(QuoteNotFoundException::new);
        return quoteServiceBean.renderQuoteToMessageToSend(quoteToDisplay)
                .thenCompose(messageToSend -> self.sendMessageToChannel(messageToSend, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Transactional
    public CompletableFuture<Void> sendMessageToChannel(MessageToSend messageToSend, MessageChannel messageChannel) {
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, messageChannel));
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String query = slashCommandParameterService.getCommandOption(QUERY_PARAMETER, event, String.class);
        AServer server = serverManagementService.loadServer(event.getGuild().getIdLong());

        Optional<Quote> possibleQuote = quoteServiceBean.searchQuote(query, server);
        Quote quoteToDisplay = possibleQuote.orElseThrow(QuoteNotFoundException::new);
        return quoteServiceBean.renderQuoteToMessageToSend(quoteToDisplay)
                .thenCompose(messageToSend -> self.replyMessage(event, messageToSend))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Transactional
    public CompletableFuture<InteractionHook> replyMessage(SlashCommandInteractionEvent event, MessageToSend messageToSend) {
        return interactionService.replyMessageToSend(messageToSend, event);
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter searchParameter = Parameter
                .builder()
                .templated(true)
                .name(QUERY_PARAMETER)
                .type(String.class)
                .build();
        List<Parameter> parameters = Collections.singletonList(searchParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(QuoteSlashCommandNames.QUOTE)
                .commandName("search")
                .build();

        return CommandConfiguration.builder()
                .name(QUOTE_SEARCH_COMMAND)
                .module(QuotesModuleDefinition.QUOTES)
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
        return QuotesFeatureDefinition.QUOTES;
    }
}
