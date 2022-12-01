package dev.sheldan.sissi.module.debra.config;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum DebraPostTarget implements PostTargetEnum {
    DEBRA_DONATION_NOTIFICATION("debraDonationNotification"), DEBRA_DONATION_NOTIFICATION2("debraDonationNotification2");

    private String key;

    DebraPostTarget(String key) {
        this.key = key;
    }
}
