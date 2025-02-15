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
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandAutoCompleteService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.sissi.module.rssnews.config.RssNewsFeatureDefinition;
import dev.sheldan.sissi.module.rssnews.config.RssNewsSlashCommandNames;
import dev.sheldan.sissi.module.rssnews.model.database.NewsFeedSource;
import dev.sheldan.sissi.module.rssnews.service.NewsCategoryServiceBean;
import dev.sheldan.sissi.module.rssnews.service.NewsFeedSourceCategoryServiceBean;
import dev.sheldan.sissi.module.rssnews.service.NewsFeedSourceCategorySubscriptionServiceBean;
import dev.sheldan.sissi.module.rssnews.service.management.NewsFeedSourceManagementServiceBean;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@Component
public class CreateNewsCategorySubscription extends AbstractConditionableCommand {

    private static final String CATEGORY_KEY_NAME_PARAMETER = "categoryName";
    private static final String SOURCE_CATEGORY_PARAMETER = "sourceCategory";
    private static final String NEWS_FEED_SOURCE_KEY_PARAMETER = "newsFeed";
    private static final String CREATE_NEWS_CATEGORY_SUBSCRIPTION_RESPONSE_TEMPLATE_KEY = "createNewsCategorySubscription_response";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private NewsCategoryServiceBean newsCategoryServiceBean;

    @Autowired
    private SlashCommandAutoCompleteService slashCommandAutoCompleteService;

    @Autowired
    private NewsFeedSourceCategorySubscriptionServiceBean newsFeedSourceCategorySubscriptionServiceBean;

    @Autowired
    private NewsFeedSourceCategoryServiceBean newsFeedSourceCategoryServiceBean;

    @Autowired
    private NewsFeedSourceManagementServiceBean newsFeedSourceManagementServiceBean;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String categoryName = slashCommandParameterService.getCommandOption(CATEGORY_KEY_NAME_PARAMETER, event, String.class);
        String sourceCategoryName = slashCommandParameterService.getCommandOption(SOURCE_CATEGORY_PARAMETER, event, String.class);
        String newsFeedSourceName = slashCommandParameterService.getCommandOption(NEWS_FEED_SOURCE_KEY_PARAMETER, event, String.class);
        Guild guild = event.getGuild();
        newsFeedSourceCategorySubscriptionServiceBean.createNewsFeedSourceCategorySubscription(categoryName, sourceCategoryName, newsFeedSourceName, guild);
        return interactionService.replyEmbed(CREATE_NEWS_CATEGORY_SUBSCRIPTION_RESPONSE_TEMPLATE_KEY, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public List<String> performAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if(slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), CATEGORY_KEY_NAME_PARAMETER)) {
            return newsCategoryServiceBean.getNamesOfNewsCategoriesStartingWith(event.getFocusedOption().getValue(), event.getGuild());
        } if(slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), SOURCE_CATEGORY_PARAMETER)) {
            String newsFeedSourceName = slashCommandParameterService.getCommandOption(NEWS_FEED_SOURCE_KEY_PARAMETER, event, String.class);
            Optional<NewsFeedSource> newsFeedSourceOptional = newsFeedSourceManagementServiceBean.getNewsFeedSourceWithNameOptional(newsFeedSourceName);
            if(newsFeedSourceOptional.isEmpty()) {
                return new ArrayList<>();
            }
            return newsFeedSourceCategoryServiceBean.getNamesOfNewsSourceCategoriesInNewsFeedStartingWith(event.getFocusedOption().getValue(), newsFeedSourceOptional.get());
        } if(slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), NEWS_FEED_SOURCE_KEY_PARAMETER)) {
            return newsFeedSourceManagementServiceBean.getNewsFeedSourceNamesStartingWithName(event.getFocusedOption().getValue());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {

        Parameter categoryParameter = Parameter
                .builder()
                .templated(true)
                .name(CATEGORY_KEY_NAME_PARAMETER)
                .supportsAutoComplete(true)
                .type(String.class)
                .build();

        Parameter sourceCategoryParameter = Parameter
                .builder()
                .name(SOURCE_CATEGORY_PARAMETER)
                .type(String.class)
                .supportsAutoComplete(true)
                .templated(true)
                .build();

        Parameter newsFeedKeyParameter = Parameter
                .builder()
                .name(NEWS_FEED_SOURCE_KEY_PARAMETER)
                .type(String.class)
                .supportsAutoComplete(true)
                .templated(true)
                .build();

        List<Parameter> parameters = Arrays.asList(categoryParameter, newsFeedKeyParameter, sourceCategoryParameter);

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(RssNewsSlashCommandNames.RSS_NEWS)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .groupName("categorysubscription")
                .commandName("create")
                .build();

        return CommandConfiguration.builder()
                .name("createNewsCategorySubscription")
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .slashCommandOnly(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .causesReaction(false)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return RssNewsFeatureDefinition.RSS_NEWS;
    }
}
