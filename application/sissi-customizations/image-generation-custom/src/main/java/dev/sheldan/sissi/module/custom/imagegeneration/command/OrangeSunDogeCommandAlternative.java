package dev.sheldan.sissi.module.custom.imagegeneration.command;

import dev.sheldan.abstracto.core.command.CommandAlternative;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import dev.sheldan.abstracto.core.command.service.CommandRegistry;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.templating.model.AttachedFile;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FileService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.imagegeneration.config.ImageGenerationFeatureConfig;
import dev.sheldan.sissi.module.custom.imagegeneration.service.ImageGenerationService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Component
public class OrangeSunDogeCommandAlternative implements CommandAlternative {

    @Autowired
    private ImageGenerationService imageGenerationService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private FileService fileService;

    @Autowired
    private CommandRegistry commandRegistry;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private ImageGenerationFeatureConfig imageGenerationFeatureConfig;

    private static final String DOGE_ORANGE_SUN_RESPONSE_TEMPLATE_KEY = "doge_orangeSun_response";

    @Override
    public boolean shouldExecute(UnParsedCommandParameter parameter, Guild guild, Message message) {
        String contentStripped = message.getContentRaw();
        String[] parameters = contentStripped.split(" ");
        return parameters.length == 1 && featureFlagService.isFeatureEnabled(imageGenerationFeatureConfig, guild.getIdLong());
    }

    @Override
    public void execute(UnParsedCommandParameter parameter, Message message) {
        String contentStripped = message.getContentRaw();
        List<String> parameters = Arrays.asList(contentStripped.split(" "));
        String inputText = commandRegistry.getCommandName(parameters.get(0), message.getGuild().getIdLong());
        File triggeredGifFile = imageGenerationService.getOrangeSunDogeImage(inputText);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(DOGE_ORANGE_SUN_RESPONSE_TEMPLATE_KEY, new Object());
        // template support does not support binary files
        AttachedFile file = AttachedFile
                .builder()
                .file(triggeredGifFile)
                .fileName("doge.png")
                .build();
        messageToSend.getAttachedFiles().add(file);
        FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, message.getGuildChannel()))
                .thenAccept(unused -> fileService.safeDeleteIgnoreException(messageToSend.getAttachedFiles().get(0).getFile()));
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.LOW;
    }
}
