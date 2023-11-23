package dev.sheldan.sissi.module.debra.config;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.ehcache.xml.XmlConfiguration;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;

@Configuration
@Slf4j
@EnableCaching
public class CacheConfig {

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        URL myUrl = getClass().getResource("/donation-cache-config.xml");
        XmlConfiguration xmlConfig = new XmlConfiguration(myUrl);
        org.ehcache.CacheManager myCacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
        return cm -> {
            myCacheManager.getRuntimeConfiguration().getCacheConfigurations().entrySet().forEach(cacheConfiguration -> {
                javax.cache.configuration.Configuration<?, ?> jConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(cacheConfiguration.getValue());
                log.info("Creating custom cache: " + cacheConfiguration.getKey());
                cm.createCache(cacheConfiguration.getKey(), jConfiguration);
            });
        };
    }
}

