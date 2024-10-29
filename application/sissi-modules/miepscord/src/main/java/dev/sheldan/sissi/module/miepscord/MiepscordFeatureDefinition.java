package dev.sheldan.sissi.module.miepscord;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum MiepscordFeatureDefinition implements FeatureDefinition {
    WEEKLY_TEXT("weeklyText");

    private String key;

    MiepscordFeatureDefinition(String key) {
        this.key = key;
    }
}
