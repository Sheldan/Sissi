package dev.sheldan.sissi.module.custom.moderation.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum ModerationCustomFeatureDefinition implements FeatureDefinition {
    MODERATION_CUSTOM("moderationCustom");

    private String key;

    ModerationCustomFeatureDefinition(String key) {
        this.key = key;
    }
}