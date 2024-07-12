package dev.sheldan.sissi.module.quotes.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.starboard.listener.StarboardPostDeletedListener;
import dev.sheldan.abstracto.starboard.model.StarboardPostDeletedModel;
import dev.sheldan.sissi.module.quotes.config.QuotesFeatureDefinition;
import dev.sheldan.sissi.module.quotes.service.QuoteServiceBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StarboardPostDeletedListenerBean implements StarboardPostDeletedListener {

    @Autowired
    private QuoteServiceBean quoteServiceBean;

    @Override
    public DefaultListenerResult execute(StarboardPostDeletedModel model) {
        log.info("Handling delete of starboard post {}, causing the quote of message {} in server {} to be deleted.", model.getStarboardPostId(), model.getStarredMessage().getMessageId(), model.getServerId());
        quoteServiceBean.deleteByMessageId(model.getStarredMessage().getMessageId());
        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return QuotesFeatureDefinition.STARBOARD_QUOTE_SYNC;
    }

}
