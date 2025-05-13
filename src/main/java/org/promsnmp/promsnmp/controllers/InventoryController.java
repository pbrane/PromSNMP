package org.promsnmp.promsnmp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Inventory", description = "Manage PromSNMP Inventory and expose Prometheus Service Discovery Endpoint")
@RestController
public class InventoryController {

    private final InventoryService inventoryService;
    private final PrometheusDiscoveryService prometheusDiscoveryService;


    public InventoryController(InventoryService inventoryService,
                               @Qualifier("prometheusDiscoveryService") PrometheusDiscoveryService prometheusDiscoveryService) {
        this.inventoryService = inventoryService;
        this.prometheusDiscoveryService = prometheusDiscoveryService;
    }

    @Operation(summary = "Export current PromSNMP Inventory", description = "The PromSNMP Inventory exported as JSON document")
    @GetMapping("/inventory")
    public ResponseEntity<InventoryDTO> exportInventory() {
        return ResponseEntity.ok(inventoryService.exportInventory());
    }

    @Operation(summary = "Import PromSNMP Inventory", description = "Endpoint for establishing the PromSNMP Inventory")
    @PostMapping("/inventory")
    public ResponseEntity<?> importInventory(@RequestBody InventoryDTO dto) {
        inventoryService.importInventory(dto);
        return ResponseEntity.ok("Inventory imported successfully.");
    }

    @Operation(summary = "Prometheus HTTP Service Discovery", description = "Prometheus Endpoint used to discovery PromSNMP managed Targets")
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
