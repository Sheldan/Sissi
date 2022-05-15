package dev.sheldan.raustria.bot.module.quotes.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.raustria.bot.module.quotes.config.QuotesFeatureDefinition;
import dev.sheldan.raustria.bot.module.quotes.config.QuotesModuleDefinition;
import dev.sheldan.raustria.bot.module.quotes.service.QuoteServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class QuoteDelete extends AbstractConditionableCommand {

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private QuoteServiceBean quoteServiceBean;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Long quoteId = (Long) parameters.get(0);
        AServer server = serverManagementService.loadServer(commandContext.getGuild().getIdLong());
        quoteServiceBean.deleteQuote(quoteId, server);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter quoteIdParameter = Parameter.builder().templated(true).name("quoteId").type(Long.class).build();
        List<Parameter> parameters = Collections.singletonList(quoteIdParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("quoteDelete")
                .module(QuotesModuleDefinition.QUOTES)
                .templated(true)
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
