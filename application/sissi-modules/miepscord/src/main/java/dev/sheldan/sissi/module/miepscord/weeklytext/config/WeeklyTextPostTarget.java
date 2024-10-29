package dev.sheldan.sissi.module.miepscord.weeklytext.config;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum WeeklyTextPostTarget implements PostTargetEnum {
    TEXT_ITEM_TARGET("textItemTarget");

    private String key;

    WeeklyTextPostTarget(String key) {
        this.key = key;
    }
}
