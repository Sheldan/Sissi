package dev.sheldan.sissi.module.costum.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.ReactionService;
import dev.sheldan.abstracto.moderation.listener.ReportMessageCreatedListener;
import dev.sheldan.abstracto.moderation.model.listener.ReportMessageCreatedModel;
import dev.sheldan.sissi.module.costum.config.ModerationCustomFeatureDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReactionReportReactionListener implements ReportMessageCreatedListener {

    public static final String REACTION_REPORT_REACTION_AMOUNT_CONFIG_KEY = "reportReactionAmount";
    public static final String REACTION_REPORT_EMOTE_PREFIX = "reactionReport";

    @Autowired
    private ConfigService configService;

    @Autowired
    private ReactionService reactionService;

    @Override
    public DefaultListenerResult execute(ReportMessageCreatedModel model) {
        Long reactionAmount = configService.getLongValueOrConfigDefault(REACTION_REPORT_REACTION_AMOUNT_CONFIG_KEY, model.getServerId());
        for (int i = 0; i < reactionAmount; i++) {
            String emoteKey = buildReactionEmoteName(i + 1);
            reactionService.addReactionToMessageAsync(emoteKey,
                    model.getServerId(), model.getReportMessage().getChannelId(), model.getReportMessage().getMessageId())
                    .thenAccept(unused -> log.info("Added reaction emote {} on report message {} in channel {} in server {}",
                            emoteKey, model.getReportMessage().getMessageId(), model.getReportMessage().getChannelId(), model.getServerId()))
                    .exceptionally(throwable -> {
                        log.info("Failed to add reaction emote {} on report message {} in channel {} in server {}",
                                emoteKey, model.getReportMessage().getMessageId(), model.getReportMessage().getChannelId(), model.getServerId());
                        return null;
                    });
        }

        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationCustomFeatureDefinition.MODERATION_CUSTOM;
    }

    private String buildReactionEmoteName(Integer position) {
        return REACTION_REPORT_EMOTE_PREFIX + position;
    }

}
