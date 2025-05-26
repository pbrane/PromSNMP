package org.promsnmp.metrics.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.promsnmp.common.dto.DiscoveryRequestDTO;
import org.promsnmp.common.dto.DiscoverySeedDTO;
import org.promsnmp.metrics.services.DiscoveryManagementService;
import org.promsnmp.metrics.services.DiscoverySeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * This controller supports SNMP based network discovery of SNMP Agents and related
 * Network Devices.
 */

@Tag(name = "Discovery", description = "Manage SNMP discovery and seeding")
@RestController
@RequestMapping("/promsnmp")
public class DiscoveryController {

    private final DiscoveryManagementService discoveryService;
    private final DiscoverySeedService seedService;

    public DiscoveryController(DiscoveryManagementService discoveryService, DiscoverySeedService seedService) {
        this.discoveryService = discoveryService;
        this.seedService = seedService;
    }

    @Operation(summary = "Seed SNMP discovery", description = "Submit SNMP agent configuration and Target list for immediate or scheduled discovery.")
    @PostMapping("/discovery")
    public ResponseEntity<?> handleDiscovery(
            @Parameter(description = "Run discovery immediately", example = "true")
            @RequestParam(defaultValue = "true") boolean scheduleNow,

            @Parameter(description = "Save this discovery seed (auto true if scheduleNow is false)", example = "false")
            @RequestParam(defaultValue = "false") boolean saveSeed,

            @Valid @RequestBody DiscoveryRequestDTO request) {

        try {
            discoveryService.handleDiscoveryRequest(request, scheduleNow, saveSeed);
            return ResponseEntity.accepted().body("Discovery request accepted.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "List all discovery seeds", description = "Returns a list of all saved discovery seeds used for scheduled SNMP polling.")
    @GetMapping("/discovery")
    public ResponseEntity<List<DiscoverySeedDTO>> listSeeds() {
        return ResponseEntity.ok(seedService.toDtoList(seedService.findAllSeeds()));
    }

    @Operation(summary = "Get a specific discovery seed", description = "Returns a discovery seed by its UUID.")
    @GetMapping("/discovery/{id}")
    public ResponseEntity<?> getSeed(
            @Parameter(description = "Discovery seed UUID", required = true)
            @PathVariable UUID id) {

        return seedService.findSeedById(id)
                .map(seedService::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete all discovery seeds", description = "Removes all saved discovery seed configurations.")
    @DeleteMapping("/discovery")
    public ResponseEntity<?> deleteAllSeeds() {
        seedService.deleteAllSeeds();
        return ResponseEntity.ok("All discovery seeds deleted.");
    }

    @Operation(summary = "Delete a specific discovery seed", description = "Deletes a discovery seed using its UUID.")
    @DeleteMapping("/discovery/{id}")
    public ResponseEntity<?> deleteSeed(
            @Parameter(description = "Discovery seed UUID", required = true)
            @PathVariable UUID id) {

        if (seedService.deleteSeedById(id)) {
            return ResponseEntity.ok("Seed deleted: " + id);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
