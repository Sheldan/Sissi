package dev.sheldan.sissi.module.quotes.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.sissi.module.quotes.config.QuotesFeatureDefinition;
import dev.sheldan.sissi.module.quotes.config.QuotesModuleDefinition;
import dev.sheldan.sissi.module.quotes.model.command.QuoteStatsModel;
import dev.sheldan.sissi.module.quotes.service.QuoteServiceBean;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class QuoteStats extends AbstractConditionableCommand {

    @Autowired
    private QuoteServiceBean quoteServiceBean;

    @Autowired
    private ChannelService channelService;

    private static final String QUOTE_STATS_RESPONSE_TEMPLATE_KEY = "quoteStats_response";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member targetMember;
        if(parameters.isEmpty()) {
            targetMember = commandContext.getAuthor();
        } else {
            targetMember = (Member) parameters.get(0);
        }
        QuoteStatsModel model = quoteServiceBean.getQuoteStats(targetMember);
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInMessageChannelList(QUOTE_STATS_RESPONSE_TEMPLATE_KEY, model, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter searchParameter = Parameter.builder().templated(true).name("member").type(Member.class).optional(true).build();
        List<Parameter> parameters = Collections.singletonList(searchParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("quoteStats")
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
