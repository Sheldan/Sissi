package dev.sheldan.sissi.module.debra.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class DonationCacheService implements InitializingBean {

    @Autowired
    private CacheManager cacheManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        cacheManager.getCache("donations");
    }
}
