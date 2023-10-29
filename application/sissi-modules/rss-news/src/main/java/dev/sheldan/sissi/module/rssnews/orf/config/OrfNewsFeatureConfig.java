package dev.sheldan.sissi.module.rssnews.orf.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.sissi.module.rssnews.config.RSSNewsFeatureConfig;
import dev.sheldan.sissi.module.rssnews.config.RssNewsFeatureDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class OrfNewsFeatureConfig implements FeatureConfig {

    @Autowired
    private RSSNewsFeatureConfig rssNewsFeatureConfig;

    @Override
    public FeatureDefinition getFeature() {
        return RssNewsFeatureDefinition.ORF_NEWS;
    }

    @Override
    public List<FeatureConfig> getRequiredFeatures() {
        return Arrays.asList(rssNewsFeatureConfig);
    }
}
