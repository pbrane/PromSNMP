package org.promsnmp.promsnmp.controllers;

import org.promsnmp.promsnmp.utils.ProtocolOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This Controller is for management of the PromSnmp instance
 */

@RestController
@RequestMapping("/promSnmp")
public class PromSnmpController {

    private final CacheManager cacheManager;

    private final ThreadPoolTaskExecutor snmpDiscoveryExecutor;
    private final ThreadPoolTaskExecutor snmpMetricsExecutor;

    public PromSnmpController(CacheManager cacheManager,
            @Qualifier("snmpDiscoveryExecutor") ThreadPoolTaskExecutor discoveryExecutor,
            @Qualifier("snmpMetricsExecutor") ThreadPoolTaskExecutor metricsExecutor) {
        this.cacheManager = cacheManager;
        this.snmpDiscoveryExecutor = discoveryExecutor;
        this.snmpMetricsExecutor = metricsExecutor;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello World");
    }

    @GetMapping("/evictCache")
    public String evictAll() {
        Objects.requireNonNull(cacheManager.getCache("metrics")).clear();
        return "Cache cleared.";
    }

    @GetMapping("/authProtocols")
    public List<String> getAuthProtocols() {
        return ProtocolOptions.getSupportedAuthProtocols();
    }

    @GetMapping("/privProtocols")
    public List<String> getPrivProtocols() {
        return ProtocolOptions.getSupportedPrivProtocols();
    }

    @GetMapping("/threadPools")
    public Map<String, Object> threadPoolStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("snmpDiscoveryExecutor", Map.of(
                "active", snmpDiscoveryExecutor.getActiveCount(),
                "poolSize", snmpDiscoveryExecutor.getPoolSize(),
                "maxPoolSize", snmpDiscoveryExecutor.getMaxPoolSize(),
                "queueSize", snmpDiscoveryExecutor.getThreadPoolExecutor().getQueue().size()
        ));

        stats.put("snmpMetricsExecutor", Map.of(
                "active", snmpMetricsExecutor.getActiveCount(),
                "poolSize", snmpMetricsExecutor.getPoolSize(),
                "maxPoolSize", snmpMetricsExecutor.getMaxPoolSize(),
                "queueSize", snmpMetricsExecutor.getThreadPoolExecutor().getQueue().size()
        ));

        return stats;
    }

}
