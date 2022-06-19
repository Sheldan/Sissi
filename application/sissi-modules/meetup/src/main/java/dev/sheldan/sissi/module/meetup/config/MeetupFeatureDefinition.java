package dev.sheldan.sissi.module.meetup.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum MeetupFeatureDefinition implements FeatureDefinition {
    MEETUP("meetup");

    private String key;

    MeetupFeatureDefinition(String key) {
        this.key = key;
    }
}
