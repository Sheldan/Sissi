package dev.sheldan.sissi.module.meetup.config;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum MeetupFeatureMode implements FeatureMode {
    ATTACH_ICS_FILE("attachIcsFile");

    private final String key;

    MeetupFeatureMode(String key) {
        this.key = key;
    }
}
