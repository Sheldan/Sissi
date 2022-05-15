package dev.sheldan.raustria.bot.module.quotes.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum QuotesFeatureDefinition implements FeatureDefinition {
    QUOTES("quotes");

    private String key;

    QuotesFeatureDefinition(String key) {
        this.key = key;
    }
}
