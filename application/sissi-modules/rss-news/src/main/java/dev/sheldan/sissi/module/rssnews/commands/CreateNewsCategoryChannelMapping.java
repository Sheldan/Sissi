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
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandAutoCompleteService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.sissi.module.rssnews.config.RssNewsFeatureDefinition;
import dev.sheldan.sissi.module.rssnews.config.RssNewsSlashCommandNames;
import dev.sheldan.sissi.module.rssnews.service.NewsCategoryChannelMappingServiceBean;
import dev.sheldan.sissi.module.rssnews.service.NewsCategoryServiceBean;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Component
public class CreateNewsCategoryChannelMapping extends AbstractConditionableCommand {

    private static final String CATEGORY_KEY_NAME_PARAMETER = "categoryName";
    private static final String CHANNEL_PARAMETER = "channel";
    private static final String CREATE_NEWS_CATEGORY_CHANNEL_MAPPING_RESPONSE_TEMPLATE_KEY = "createNewsCategoryChannelMapping_response";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private NewsCategoryServiceBean newsCategoryServiceBean;

    @Autowired
    private SlashCommandAutoCompleteService slashCommandAutoCompleteService;

    @Autowired
    private NewsCategoryChannelMappingServiceBean newsCategoryMappingServiceBean;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String categoryName = slashCommandParameterService.getCommandOption(CATEGORY_KEY_NAME_PARAMETER, event, String.class);
        GuildChannel channel = slashCommandParameterService.getCommandOption(CHANNEL_PARAMETER, event, TextChannel.class, GuildChannel.class);
        newsCategoryMappingServiceBean.createNewsCategoryChannelMapping(categoryName, channel);
        return interactionService.replyEmbed(CREATE_NEWS_CATEGORY_CHANNEL_MAPPING_RESPONSE_TEMPLATE_KEY, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public List<String> performAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if(slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), CATEGORY_KEY_NAME_PARAMETER)) {
            return newsCategoryServiceBean.getNamesOfNewsCategoriesStartingWith(event.getFocusedOption().getValue(), event.getGuild());
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

        Parameter channelParameter = Parameter
                .builder()
                .name(CHANNEL_PARAMETER)
                .type(TextChannel.class)
                .templated(true)
                .build();

        List<Parameter> parameters = Arrays.asList(categoryParameter, channelParameter);

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(RssNewsSlashCommandNames.RSS_NEWS)
                .groupName("categorychannelmapping")
                .commandName("create")
                .build();

        return CommandConfiguration.builder()
                .name("createNewsCategoryChannelMapping")
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
