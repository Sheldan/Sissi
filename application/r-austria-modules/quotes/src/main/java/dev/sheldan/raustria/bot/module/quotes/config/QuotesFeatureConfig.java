package dev.sheldan.raustria.bot.module.quotes.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import org.springframework.stereotype.Component;

@Component
public class QuotesFeatureConfig implements FeatureConfig {
    @Override
    public FeatureDefinition getFeature() {
        return QuotesFeatureDefinition.QUOTES;
    }

}
