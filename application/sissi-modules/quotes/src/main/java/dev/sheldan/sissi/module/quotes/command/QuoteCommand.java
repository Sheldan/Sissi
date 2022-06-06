package dev.sheldan.sissi.module.quotes.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.sissi.module.quotes.config.QuotesFeatureDefinition;
import dev.sheldan.sissi.module.quotes.config.QuotesModuleDefinition;
import dev.sheldan.sissi.module.quotes.exception.QuoteNotFoundException;
import dev.sheldan.sissi.module.quotes.model.database.Quote;
import dev.sheldan.sissi.module.quotes.service.QuoteServiceBean;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class QuoteCommand extends AbstractConditionableCommand {

    @Autowired
    private QuoteServiceBean quoteServiceBean;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private QuoteCommand self;


    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Optional<Quote> foundQuote;
        if(parameters.isEmpty()) {
            AServer server = serverManagementService.loadServer(commandContext.getGuild().getIdLong());
            foundQuote = quoteServiceBean.getRandomQuote(server);
        } else {
            Member targetMember = (Member) parameters.get(0);
            AUserInAServer user = userInServerManagementService.loadOrCreateUser(targetMember);
            foundQuote = quoteServiceBean.getRandomQuoteForMember(user);
        }
        if(foundQuote.isPresent()) {
                Quote quoteToDisplay = foundQuote.get();
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
        Parameter memberParameter = Parameter.builder().templated(true).name("member").type(Member.class).optional(true).build();
        List<Parameter> parameters = Collections.singletonList(memberParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("quote")
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
