package org.promsnmp.promsnmp.controllers;

import org.promsnmp.promsnmp.dto.InventoryDTO;
import org.promsnmp.promsnmp.services.InventoryService;
import org.promsnmp.promsnmp.services.PrometheusDiscoveryService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * This Controller supports import and export of the PromSnmp instances inventory.
 * It also supports the Prometheus' "Service Discovery" of Targets
 * fixme
 * Importing inventory should only allowed to happen at startup (future capability)
 * and only if the current inventory is empty when in "container mode."
 */
@RestController
public class InventoryController {

    private final InventoryService inventoryService;
    private final PrometheusDiscoveryService prometheusDiscoveryService;


    public InventoryController(InventoryService inventoryService,
                               @Qualifier("prometheusDiscoveryService") PrometheusDiscoveryService prometheusDiscoveryService) {
        this.inventoryService = inventoryService;
        this.prometheusDiscoveryService = prometheusDiscoveryService;
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

    @GetMapping("/targets")
    public ResponseEntity<String> sampleServices() {
        return prometheusDiscoveryService.getTargets()
                .map(services -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(services))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"File not found\"}"));
    }
    
}
