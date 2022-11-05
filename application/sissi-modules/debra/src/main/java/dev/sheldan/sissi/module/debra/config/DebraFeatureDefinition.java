package dev.sheldan.sissi.module.debra.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum DebraFeatureDefinition implements FeatureDefinition {
    DEBRA("debra");

    private String key;

    DebraFeatureDefinition(String key) {
        this.key = key;
    }
}
