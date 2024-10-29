package dev.sheldan.sissi.module.miepscord.weeklytext.commands;

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
import dev.sheldan.abstracto.core.service.PaginatorService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.sissi.module.miepscord.MiepscordFeatureDefinition;
import dev.sheldan.sissi.module.miepscord.MiepscordSlashCommandNames;
import dev.sheldan.sissi.module.miepscord.weeklytext.model.database.TextItem;
import dev.sheldan.sissi.module.miepscord.weeklytext.model.template.ShowTextItemModel;
import dev.sheldan.sissi.module.miepscord.weeklytext.model.template.ShowTextItemsModel;
import dev.sheldan.sissi.module.miepscord.weeklytext.service.management.TextItemServiceManagementBean;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Component
public class ShowTextItems extends AbstractConditionableCommand {

    private static final String SHOW_WEEKLY_TEXTS_COMMAND_NAME = "showWeeklyTexts";
    private static final String SHOW_WEEKLY_TEXTS_RESPONSE_TEMPLATE = "showWeeklyTexts";
    public static final String NO_ITEMS_TEMPLATE_KEY = "showWeeklyTexts_no_items_found";

    private static final String SHOW_WEEKLY_TEXT_SHOW_ALL_PARAMETER = "showAll";

    @Autowired
    private TextItemServiceManagementBean textItemServiceManagementBean;

    @Autowired
    private PaginatorService paginatorService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;
    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        boolean showAll = false;
        if(slashCommandParameterService.hasCommandOption(SHOW_WEEKLY_TEXT_SHOW_ALL_PARAMETER, event)) {
            showAll = slashCommandParameterService.getCommandOption(SHOW_WEEKLY_TEXT_SHOW_ALL_PARAMETER, event, Boolean.class);
        }
        List<TextItem> textItems;
        if(showAll) {
            textItems = textItemServiceManagementBean.getAllTextItems();
        } else {
            textItems = textItemServiceManagementBean.getAllTextItemsWithDoneFlag(false);
        }
        textItems.sort(Comparator.comparing(TextItem::getCreated));
        if(textItems.isEmpty()) {
            MessageToSend messageToSend = templateService.renderEmbedTemplate(NO_ITEMS_TEMPLATE_KEY, new Object(), event.getGuild().getIdLong());
            return interactionService.replyMessageToSend(messageToSend, event)
                    .thenApply(interactionHook -> CommandResult.fromSuccess());

        }
        List<ShowTextItemModel> convertedTextItems = textItems.stream().map(this::convertTextItem).toList();
        ShowTextItemsModel items = ShowTextItemsModel
                .builder()
                .items(convertedTextItems)
                .build();
        return paginatorService.createPaginatorFromTemplate(SHOW_WEEKLY_TEXTS_RESPONSE_TEMPLATE, items, event)
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    private ShowTextItemModel convertTextItem(TextItem textItem) {
        return ShowTextItemModel
                .builder()
                .text(textItem.getText())
                .id(textItem.getId())
                .done(textItem.getDone())
                .created(textItem.getCreated())
                .build();
    }

    @Override
    public CommandConfiguration getConfiguration() {

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        Parameter showAllItems = Parameter
                .builder()
                .name(SHOW_WEEKLY_TEXT_SHOW_ALL_PARAMETER)
                .type(Boolean.class)
                .optional(true)
                .templated(true)
                .build();

        List<Parameter> parameters = Arrays.asList(showAllItems);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(MiepscordSlashCommandNames.MIEPSCORD_ROOT_NAME)
                .groupName("weeklytexts")
                .commandName("show")
                .build();

        return CommandConfiguration.builder()
                .name(SHOW_WEEKLY_TEXTS_COMMAND_NAME)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandOnly(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .parameters(parameters)
                .supportsEmbedException(true)
                .causesReaction(true)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return MiepscordFeatureDefinition.WEEKLY_TEXT;
    }
}
