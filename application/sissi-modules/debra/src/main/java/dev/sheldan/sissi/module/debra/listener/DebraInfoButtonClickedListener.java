package dev.sheldan.sissi.module.debra.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.sissi.module.debra.config.DebraFeatureDefinition;
import dev.sheldan.sissi.module.debra.service.DonationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DebraInfoButtonClickedListener implements ButtonClickedListener {

    @Autowired
    private MessageService messageService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InteractionService interactionService;

    private static final String DEBRA_INFO_MESSAGE_TEMPLATE_KEY = "debraInfoMessage";
    private static final String DEBRA_INFO_MESSAGE_RESPONSE_TEMPLATE_KEY = "debraInfoMessage_response";

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        MessageToSend messageToSend = templateService.renderEmbedTemplate(DEBRA_INFO_MESSAGE_TEMPLATE_KEY, new Object(), model.getServerId());
        messageService.sendMessageToSendToUser(model.getEvent().getUser(), messageToSend).thenAccept(interactionHook -> {
            log.info("Send debra info message to user {}", model.getEvent().getUser().getIdLong());
        }).exceptionally(throwable -> {
            log.error("Failed to send debra info message to user {}", model.getEvent().getUser().getIdLong(), throwable);
            return null;
        });
        MessageToSend responseMessageToSend = templateService.renderEmbedTemplate(DEBRA_INFO_MESSAGE_RESPONSE_TEMPLATE_KEY, new Object(), model.getServerId());
        FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(responseMessageToSend, model.getEvent().getInteraction().getHook()))
        .thenAccept(interactionHook -> {
            log.info("Send debra info message response to user {}", model.getEvent().getUser().getIdLong());
        }).exceptionally(throwable -> {
            log.error("Failed to send debra info message response to user {}", model.getEvent().getUser().getIdLong(), throwable);
            return null;
        });
        return ButtonClickedListenerResult.ACKNOWLEDGED;
    }

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        return model.getOrigin().equals(DonationService.DEBRA_INFO_BUTTON_ORIGIN);
    }

    @Override
    public FeatureDefinition getFeature() {
        return DebraFeatureDefinition.DEBRA;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }


}
