package dev.sheldan.sissi.module.rssnews.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum RssNewsFeatureDefinition implements FeatureDefinition {
    RSS_NEWS("rssNews"), ORF_NEWS("orfNews");

    private String key;

    RssNewsFeatureDefinition(String key) {
        this.key = key;
    }
}
