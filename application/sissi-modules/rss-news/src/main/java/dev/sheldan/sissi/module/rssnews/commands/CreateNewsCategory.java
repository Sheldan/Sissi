package dev.sheldan.sissi.module.rssnews.commands;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.sissi.module.rssnews.config.RssNewsFeatureDefinition;
import dev.sheldan.sissi.module.rssnews.config.RssNewsSlashCommandNames;
import dev.sheldan.sissi.module.rssnews.service.NewsCategoryServiceBean;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class CreateNewsCategory extends AbstractConditionableCommand {

    private static final String CATEGORY_KEY_NAME_PARAMETER = "categoryName";
    private static final String CREATE_SERVER_NEWS_CATEGORY_RESPONSE_TEMPLATE_KEY = "createNewsCategory_response";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private NewsCategoryServiceBean newsCategoryServiceBean;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String categoryName = slashCommandParameterService.getCommandOption(CATEGORY_KEY_NAME_PARAMETER, event, String.class);
        AServer server = serverManagementService.loadOrCreate(event.getGuild().getIdLong());
        newsCategoryServiceBean.createCategory(categoryName, server);
        return interactionService.replyEmbed(CREATE_SERVER_NEWS_CATEGORY_RESPONSE_TEMPLATE_KEY, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {

        Parameter nameParameter = Parameter
                .builder()
                .templated(true)
                .name(CATEGORY_KEY_NAME_PARAMETER)
                .type(String.class)
                .build();

        List<Parameter> parameters = Arrays.asList(nameParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(RssNewsSlashCommandNames.RSS_NEWS)
                .groupName("category")
                .commandName("create")
                .build();

        return CommandConfiguration.builder()
                .name("createNewsCategory")
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .slashCommandOnly(true)
                .supportsEmbedException(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return RssNewsFeatureDefinition.RSS_NEWS;
    }
}
