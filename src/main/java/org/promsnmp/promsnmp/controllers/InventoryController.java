package org.promsnmp.promsnmp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.promsnmp.promsnmp.dto.InventoryDTO;
import org.promsnmp.promsnmp.services.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * This Controller supports import and export of the PromSnmp instances inventory.
 * It also supports the Prometheus' "Service Discovery" of Targets
 * fixme
 * Importing inventory should only allowed to happen at startup (future capability)
 * and only if the current inventory is empty when in "container mode."
 */
@Tag(name = "Inventory", description = "Manage PromSNMP Inventory and expose Prometheus Service Discovery Endpoint")
@RestController("/promsnmp")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
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

}
