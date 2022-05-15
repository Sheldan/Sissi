package dev.sheldan.raustria.bot.module.quotes.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.raustria.bot.module.quotes.config.QuotesFeatureDefinition;
import dev.sheldan.raustria.bot.module.quotes.config.QuotesModuleDefinition;
import dev.sheldan.raustria.bot.module.quotes.exception.QuoteNotFoundException;
import dev.sheldan.raustria.bot.module.quotes.model.database.Quote;
import dev.sheldan.raustria.bot.module.quotes.service.QuoteServiceBean;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class QuoteSearch extends AbstractConditionableCommand {

    @Autowired
    private QuoteServiceBean quoteServiceBean;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private QuoteSearch self;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String query = (String) parameters.get(0);
        AServer server = serverManagementService.loadServer(commandContext.getGuild().getIdLong());

        Optional<Quote> possibleQuote = quoteServiceBean.searchQuote(query, server);
        if(possibleQuote.isPresent()) {
            Quote quoteToDisplay = possibleQuote.get();
            return quoteServiceBean.renderQuoteToMessageToSend(quoteToDisplay)
                    .thenCompose(messageToSend -> self.sendMessageToChannel(messageToSend, commandContext.getChannel()))
                    .thenApply(unused -> CommandResult.fromSuccess());
        } else {
            throw new QuoteNotFoundException();
        }
    }

    @Transactional
    public CompletableFuture<Void> sendMessageToChannel(MessageToSend messageToSend, MessageChannel messageChannel) {
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, messageChannel));
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter searchParameter = Parameter.builder().templated(true).name("query").type(String.class).build();
        List<Parameter> parameters = Collections.singletonList(searchParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("quoteSearch")
                .module(QuotesModuleDefinition.QUOTES)
                .templated(true)
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
