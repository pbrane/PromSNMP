package org.promsnmp.metrics.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${CACHE_EXP_MILLIS:300000}")
    private Integer cacheExp;

    @Value("${CACHE_ENTRY_CNT:10000}")
    private Integer cacheEntries;

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(cacheExp, TimeUnit.MILLISECONDS)
                .maximumSize(cacheEntries)
                .recordStats();
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager manager = new CaffeineCacheManager("metrics");
        manager.setCaffeine(caffeine);
        return manager;
    }
}
