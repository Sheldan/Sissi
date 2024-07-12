package dev.sheldan.sissi.module.quotes.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.starboard.listener.StarboardPostCreatedListener;
import dev.sheldan.abstracto.starboard.model.StarboardPostCreatedModel;
import dev.sheldan.sissi.module.quotes.config.QuotesFeatureDefinition;
import dev.sheldan.sissi.module.quotes.service.QuoteServiceBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class StarboardPostCreatedListenerBean implements StarboardPostCreatedListener {

    @Autowired
    private MessageService messageService;

    @Autowired
    private QuoteServiceBean quoteServiceBean;

    @Autowired
    private StarboardPostCreatedListenerBean self;

    @Override
    public DefaultListenerResult execute(StarboardPostCreatedModel model) {
        Long serverId = model.getServerId();
        Long starboardPostId = model.getStarboardPostId();
        messageService.loadMessage(serverId, model.getStarredMessage().getChannelId(), model.getStarredMessage().getMessageId())
                .thenAccept(message -> self.storeQuote(message, model))
                .exceptionally(throwable -> {
                    log.error("Failed to persist quote for starboard post {} in server {}.", starboardPostId, serverId, throwable);
                    return null;
                });
        return DefaultListenerResult.PROCESSED;
    }

    @Transactional
    public void storeQuote(Message message, StarboardPostCreatedModel model) {
        log.info("Creating quote from starboard post {} in server {} from user {} because of user {}.", model.getStarboardPostId(), model.getServerId(),
                model.getStarredUser().getUserId(), model.getLastStarrer().getUserId());
        quoteServiceBean.createQuote(model.getStarredUser(), model.getLastStarrer(), message);
    }

    @Override
    public FeatureDefinition getFeature() {
        return QuotesFeatureDefinition.STARBOARD_QUOTE_SYNC;
    }

}
