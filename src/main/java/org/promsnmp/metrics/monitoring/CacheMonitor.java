package org.promsnmp.metrics.monitoring;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CacheMonitor {

    private final CacheManager cacheManager;

    public CacheMonitor(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Scheduled(fixedRateString = "${CACHE_STATS_RATE_MILLIS:60000}")
    public void logCacheStats() {
        CaffeineCache metricsCache = (CaffeineCache) cacheManager.getCache("metrics");
        if (metricsCache == null) return;

        CacheStats stats = metricsCache.getNativeCache().stats();
        System.out.println("[CacheMonitor] Metrics Cache Stats: " + stats.toString());
    }
}
