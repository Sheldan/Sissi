package dev.sheldan.sissi.module.quotes.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum QuotesFeatureDefinition implements FeatureDefinition {
    QUOTES("quotes"), STARBOARD_QUOTE_SYNC("starboardQuoteSync");

    private String key;

    QuotesFeatureDefinition(String key) {
        this.key = key;
    }
}
