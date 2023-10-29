package dev.sheldan.sissi.module.rssnews.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.sissi.module.rssnews.orf.config.OrfNewsFeatureConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Component
public class RSSNewsFeatureConfig implements FeatureConfig {

    @Autowired
    private OrfNewsFeatureConfig orfNewsFeatureConfig;

    @Override
    public FeatureDefinition getFeature() {
        return RssNewsFeatureDefinition.RSS_NEWS;
    }

    @Override
    public List<FeatureConfig> getDependantFeatures() {
        return Arrays.asList(orfNewsFeatureConfig);
    }
}
