package dev.sheldan.sissi.module.miepscord.weeklytext.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.sissi.module.miepscord.MiepscordFeatureDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Component
public class WeeklyTextFeatureConfig implements FeatureConfig {

    public static final String WEEKLY_TEXT_SERVER_ID = "WEEKLY_TEXT_SERVER_ID";
    @Override
    public FeatureDefinition getFeature() {
        return MiepscordFeatureDefinition.WEEKLY_TEXT;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(WeeklyTextPostTarget.TEXT_ITEM_TARGET);
    }

}
