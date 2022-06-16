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
public class QuoteGet extends AbstractConditionableCommand {

    private static final String QUOTE_GET_COMMAND = "quoteGet";
    private static final String QUOTE_ID_PARAMETER = "quoteId";

    @Autowired
    private QuoteServiceBean quoteServiceBean;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private QuoteGet self;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Long quoteId = (Long) parameters.get(0);
        AServer server = serverManagementService.loadServer(commandContext.getGuild().getIdLong());

        Optional<Quote> possibleQuote = quoteServiceBean.getQuote(quoteId, server);
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
        Long quoteId = slashCommandParameterService.getCommandOption(QUOTE_ID_PARAMETER, event, Integer.class).longValue();
        AServer server = serverManagementService.loadServer(event.getGuild().getIdLong());

        Optional<Quote> possibleQuote = quoteServiceBean.getQuote(quoteId, server);
        Quote quoteToDisplay = possibleQuote.orElseThrow(QuoteNotFoundException::new);
        return quoteServiceBean.renderQuoteToMessageToSend(quoteToDisplay)
                .thenCompose(messageToSend -> self.replySlashCommand(event, messageToSend))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Transactional
    public CompletableFuture<InteractionHook> replySlashCommand(SlashCommandInteractionEvent event, MessageToSend messageToSend) {
        return interactionService.replyMessageToSend(messageToSend, event);
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
                .rootCommandName(QuoteSlashCommandNames.QUOTE)
                .commandName("get")
                .build();

        return CommandConfiguration.builder()
                .name(QUOTE_GET_COMMAND)
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
