package dev.sheldan.sissi.module.custom.moderation.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.moderation.config.feature.ReportReactionFeatureConfig;
import dev.sheldan.sissi.module.custom.moderation.service.ModModeServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static dev.sheldan.sissi.module.custom.moderation.listener.ReactionReportReactionListener.REACTION_REPORT_REACTION_AMOUNT_CONFIG_KEY;

@Component
public class ModerationCustomFeature implements FeatureConfig {

    @Autowired
    private ReportReactionFeatureConfig reportReactionFeatureConfig;

    @Override
    public FeatureDefinition getFeature() {
        return ModerationCustomFeatureDefinition.MODERATION_CUSTOM;
    }

    @Override
    public List<FeatureConfig> getRequiredFeatures() {
        return Arrays.asList(reportReactionFeatureConfig);
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(REACTION_REPORT_REACTION_AMOUNT_CONFIG_KEY, ModModeServiceBean.MODMODE_ROLE_CONFIG_KEY,
                ModModeServiceBean.MODMODE_CHANGED_ROLE_COLOR_CONFIG_KEY);
    }
}
