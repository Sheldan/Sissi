package dev.sheldan.sissi.module.debra.config;

import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.ehcache.xml.XmlConfiguration;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import java.net.URL;

@Configuration
public class CacheConfig {

    @Bean("donationCacheManager")
    public JCacheCacheManager jCacheCacheManager() {
        return new JCacheCacheManager(getDonationCacheManager());
    }

    @Bean
    public CacheManager getDonationCacheManager() {
        URL myUrl = getClass().getResource("/donation-cache-config.xml");
        XmlConfiguration xmlConfig = new XmlConfiguration(myUrl);
        org.ehcache.CacheManager myCacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
        EhcacheCachingProvider provider = (EhcacheCachingProvider) Caching.getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider");

        return  provider.getCacheManager(provider.getDefaultURI(), myCacheManager.getRuntimeConfiguration());
    }


}
