package dev.sheldan.sissi.module.debra.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DebraFeatureConfig implements FeatureConfig {

    public static final String DEBRA_DONATION_NOTIFICATION_DELAY_CONFIG_KEY = "debraDonationNotificationDelayMillis";
    public static final String DEBRA_DONATION_NOTIFICATION_SERVER_ID_ENV_NAME = "DEBRA_DONATION_NOTIFICATION_SERVER_ID";
    @Override
    public FeatureDefinition getFeature() {
        return DebraFeatureDefinition.DEBRA;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(DebraPostTarget.DEBRA_DONATION_NOTIFICATION, DebraPostTarget.DEBRA_DONATION_NOTIFICATION2);
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(DEBRA_DONATION_NOTIFICATION_DELAY_CONFIG_KEY);
    }
}
