package org.promsnmp.promsnmp.controllers;

import org.promsnmp.promsnmp.dto.InventoryDTO;
import org.promsnmp.promsnmp.model.Agent;
import org.promsnmp.promsnmp.model.NetworkDevice;
import org.promsnmp.promsnmp.repositories.jpa.NetworkDeviceRepository;
import org.promsnmp.promsnmp.services.PrometheusMetricsService;
import org.promsnmp.promsnmp.services.PrometheusDiscoveryService;
import org.promsnmp.promsnmp.utils.ProtocolOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/promSnmp")
public class PromSnmpController {

    private final PrometheusMetricsService prometheusMetricsService;
    private final PrometheusDiscoveryService prometheusDiscoveryService;
    private final CacheManager cacheManager;

    private final NetworkDeviceRepository networkDeviceRepository;

    public PromSnmpController(
            @Qualifier("prometheusMetricsService") PrometheusMetricsService metricsService,
            @Qualifier("prometheusDiscoveryService") PrometheusDiscoveryService discoveryService,
            CacheManager cacheManager, NetworkDeviceRepository networkDeviceRepository) {
        this.prometheusMetricsService = metricsService;
        this.prometheusDiscoveryService = discoveryService;
        this.cacheManager = cacheManager;
        this.networkDeviceRepository = networkDeviceRepository;
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
    public InventoryDTO exportInventory() {
        List<NetworkDevice> allDevices = networkDeviceRepository.findAll();
        InventoryDTO dto = new InventoryDTO();
        dto.setDevices(allDevices);
        return dto;
    }

    @PostMapping("/inventory")
    @Transactional
    public ResponseEntity<?> importInventory(@RequestBody InventoryDTO dto) {
        networkDeviceRepository.deleteAll();

        for (NetworkDevice device : dto.getDevices()) {
            device.setId(null); // Let JPA assign a new ID
            device.setDiscoveredAt(Instant.now());

            for (Agent agent : device.getAgents()) {
                agent.setId(UUID.randomUUID()); // New ID for agents
                agent.setDevice(device);
                agent.setDiscoveredAt(Instant.now());
            }

            if (device.getPrimaryAgent() != null) {
                // Re-resolve the primaryAgent to the matching one in new list
                UUID oldId = device.getPrimaryAgent().getId();
                device.setPrimaryAgent(device.getAgents().stream()
                        .filter(a -> a.getId().equals(oldId))
                        .findFirst().orElse(null));
            }
        }

        networkDeviceRepository.saveAll(dto.getDevices());
        return ResponseEntity.ok("Inventory imported successfully.");
    }
}
