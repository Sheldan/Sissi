package dev.sheldan.sissi.module.rssnews.commands;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.sissi.module.rssnews.config.RssNewsFeatureDefinition;
import dev.sheldan.sissi.module.rssnews.config.RssNewsSlashCommandNames;
import dev.sheldan.sissi.module.rssnews.model.template.NewsCategoryInfo;
import dev.sheldan.sissi.module.rssnews.model.template.ShowNewsCategoriesResponse;
import dev.sheldan.sissi.module.rssnews.service.NewsCategoryServiceBean;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ShowNewsCategories extends AbstractConditionableCommand {

    private static final String SHOW_NEWS_CATEGORIES_TEMPLATE_KEY = "showNewsCategories_response";

    @Autowired
    private NewsCategoryServiceBean newsCategoryServiceBean;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        List<NewsCategoryInfo> categoryInfos = newsCategoryServiceBean.getCategoryInfos(event.getGuild());
        ShowNewsCategoriesResponse responseModel = ShowNewsCategoriesResponse
                .builder()
                .newsCategories(categoryInfos)
                .build();
        return interactionService.replyEmbed(SHOW_NEWS_CATEGORIES_TEMPLATE_KEY, responseModel, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(RssNewsSlashCommandNames.RSS_NEWS)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .groupName("category")
                .commandName("show")
                .build();

        return CommandConfiguration.builder()
                .name("showNewsCategories")
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .slashCommandOnly(true)
                .supportsEmbedException(true)
                .causesReaction(false)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return RssNewsFeatureDefinition.RSS_NEWS;
    }
}
