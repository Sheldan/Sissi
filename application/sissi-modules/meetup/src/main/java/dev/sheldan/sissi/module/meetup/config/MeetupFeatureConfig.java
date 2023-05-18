package dev.sheldan.sissi.module.meetup.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static dev.sheldan.sissi.module.meetup.config.MeetupFeatureMode.ATTACH_ICS_FILE;

@Component
public class MeetupFeatureConfig implements FeatureConfig {
    @Override
    public FeatureDefinition getFeature() {
        return MeetupFeatureDefinition.MEETUP;
    }

    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(ATTACH_ICS_FILE);
    }
}
