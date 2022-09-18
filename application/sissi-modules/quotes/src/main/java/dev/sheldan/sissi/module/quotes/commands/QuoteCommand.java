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
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.sissi.module.quotes.config.QuoteSlashCommandNames;
import dev.sheldan.sissi.module.quotes.config.QuotesFeatureDefinition;
import dev.sheldan.sissi.module.quotes.config.QuotesModuleDefinition;
import dev.sheldan.sissi.module.quotes.exception.QuoteNotFoundException;
import dev.sheldan.sissi.module.quotes.model.database.Quote;
import dev.sheldan.sissi.module.quotes.service.QuoteServiceBean;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
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
public class QuoteCommand extends AbstractConditionableCommand {

    private static final String QUOTE_COMMAND = "quote";
    private static final String MEMBER_PARAMETER = "member";

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

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

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
        Quote quoteToDisplay = foundQuote.orElseThrow(QuoteNotFoundException::new);
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
        Optional<Quote> foundQuote;
        if(slashCommandParameterService.hasCommandOption(MEMBER_PARAMETER, event)) {
            Member targetMember = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER, event, Member.class);
            AUserInAServer user = userInServerManagementService.loadOrCreateUser(targetMember);
            foundQuote = quoteServiceBean.getRandomQuoteForMember(user);
        } else {
            AServer server = serverManagementService.loadServer(event.getGuild().getIdLong());
            foundQuote = quoteServiceBean.getRandomQuote(server);
        }
        Quote quoteToDisplay = foundQuote.orElseThrow(QuoteNotFoundException::new);
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
        Parameter memberParameter = Parameter
                .builder()
                .templated(true)
                .name(MEMBER_PARAMETER)
                .type(Member.class)
                .optional(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(QuoteSlashCommandNames.QUOTE)
                .commandName("random")
                .build();

        List<Parameter> parameters = Collections.singletonList(memberParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name(QUOTE_COMMAND)
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
