package dev.sheldan.sissi.module.quotes.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import org.springframework.stereotype.Component;

@Component
public class StarboardQuoteSyncFeatureConfig implements FeatureConfig {
    @Override
    public FeatureDefinition getFeature() {
        return QuotesFeatureDefinition.STARBOARD_QUOTE_SYNC;
    }
}
