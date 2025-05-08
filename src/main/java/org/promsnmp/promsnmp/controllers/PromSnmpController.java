package org.promsnmp.promsnmp.controllers;

import org.promsnmp.promsnmp.dto.InventoryDTO;
import org.promsnmp.promsnmp.services.InventoryService;
import org.promsnmp.promsnmp.services.PrometheusDiscoveryService;
import org.promsnmp.promsnmp.services.PrometheusMetricsService;
import org.promsnmp.promsnmp.utils.ProtocolOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/promSnmp")
public class PromSnmpController {

    private final PrometheusMetricsService prometheusMetricsService;
    private final PrometheusDiscoveryService prometheusDiscoveryService;
    private final CacheManager cacheManager;

    private final InventoryService inventoryService;

    private final ThreadPoolTaskExecutor snmpDiscoveryExecutor;
    private final ThreadPoolTaskExecutor snmpMetricsExecutor;

    public PromSnmpController(
            @Qualifier("prometheusMetricsService") PrometheusMetricsService metricsService,
            @Qualifier("prometheusDiscoveryService") PrometheusDiscoveryService discoveryService,
            CacheManager cacheManager,
            InventoryService inventoryService,
            @Qualifier("snmpDiscoveryExecutor") ThreadPoolTaskExecutor discoveryExecutor,
            @Qualifier("snmpMetricsExecutor") ThreadPoolTaskExecutor metricsExecutor) {
        this.prometheusMetricsService = metricsService;
        this.prometheusDiscoveryService = discoveryService;
        this.cacheManager = cacheManager;
        this.inventoryService = inventoryService;
        this.snmpDiscoveryExecutor = discoveryExecutor;
        this.snmpMetricsExecutor = metricsExecutor;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello World");
    }

    @GetMapping("/metrics")
    public ResponseEntity<String> sampleData(
            @RequestParam(required = false)
            String instance,
            @RequestParam(required = false, defaultValue = "false")
            Boolean regex ) {

        return prometheusMetricsService.getMetrics(instance, regex)
                .map(metrics -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                        .body(metrics))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error reading file"));
    }

    @GetMapping("/services")
    public ResponseEntity<String> sampleServices() {
        return prometheusDiscoveryService.getServices()
                .map(services -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(services))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"File not found\"}"));
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

    @GetMapping("/inventory")
    public ResponseEntity<InventoryDTO> exportInventory() {
        return ResponseEntity.ok(inventoryService.exportInventory());
    }

    @PostMapping("/inventory")
    public ResponseEntity<?> importInventory(@RequestBody InventoryDTO dto) {
        inventoryService.importInventory(dto);
        return ResponseEntity.ok("Inventory imported successfully.");
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
