package org.promsnmp.metrics.services;

import lombok.extern.slf4j.Slf4j;
import org.promsnmp.common.dto.DiscoveryRequestDTO;
import org.promsnmp.common.utils.IpUtils;
import org.promsnmp.metrics.inventory.InventoryBackupManager;
import org.promsnmp.metrics.inventory.InventoryPublisher;
import org.promsnmp.metrics.inventory.discovery.SnmpAgentDiscovery;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

import static org.promsnmp.metrics.snmp.Snmp4jUtils.resolveSnmpVersion;

@Slf4j
@Service
public class DiscoveryManagementService {

    private final SnmpAgentDiscovery discoveryService;
    private final DiscoverySeedService seedService;
    private final InventoryPublisher inventoryPublisher;
    private final InventoryBackupManager inventoryBackupManager;

    public DiscoveryManagementService(SnmpAgentDiscovery discoveryService,
                                      DiscoverySeedService seedService,
                                      InventoryPublisher inventoryPublisher,
                                      InventoryBackupManager inventoryBackupManager) {
        this.discoveryService = discoveryService;
        this.seedService = seedService;
        this.inventoryPublisher = inventoryPublisher;
        this.inventoryBackupManager = inventoryBackupManager;
    }

    public void handleDiscoveryRequest(DiscoveryRequestDTO request, boolean scheduleNow, boolean saveSeed) {
        int snmpVersion = resolveSnmpVersion(request.getVersion());
        UUID contextId = UUID.randomUUID();
        List<InetAddress> targets = IpUtils.toInetAddressList(request.getPotentialTargets(), contextId);

        if (targets.isEmpty()) throw new IllegalArgumentException("No valid targets provided.");

        boolean shouldPersist = !scheduleNow || saveSeed;

        if ("snmp-community".equals(request.getAgentType())) {
            if (request.getReadCommunity() == null)
                throw new IllegalArgumentException("readCommunity is required for snmp-community");

            if (scheduleNow) {
                var future = discoveryService.discoverMultiple(
                        targets, request.getPort(), request.getReadCommunity());

                future.thenAccept(agents -> {
                    if (shouldPersist) {
                        seedService.saveDiscoverySeed(request);
                    }
                    agents.forEach(a -> {
                        a.setVersion(snmpVersion);
                        a.getDevice().setPrimaryAgent(a);
                    });
                    inventoryPublisher.publish(agents);
                });
            } else {
                seedService.saveDiscoverySeed(request);
            }

        } else if ("snmp-user".equals(request.getAgentType())) {
            if (request.getSecurityName() == null || request.getAuthProtocol() == null || request.getAuthPassphrase() == null)
                throw new IllegalArgumentException("Incomplete SNMPv3 configuration");

            if (scheduleNow) {
                var future = discoveryService.discoverMultipleV3(
                        targets, request.getPort(),
                        request.getSecurityName(),
                        request.getAuthProtocol(), request.getAuthPassphrase(),
                        request.getPrivProtocol(), request.getPrivPassphrase());

                future.thenAccept(agents -> {
                    if (shouldPersist) {
                        seedService.saveDiscoverySeed(request);
                    }
                    agents.forEach(a -> {
                        a.setVersion(snmpVersion);
                        a.getDevice().setPrimaryAgent(a);
                    });
                    inventoryPublisher.publish(agents);
                });
            } else {
                seedService.saveDiscoverySeed(request);
            }

        } else {
            throw new IllegalArgumentException("Unsupported agent type: " + request.getAgentType());
        }

        inventoryBackupManager.backup();
    }

}
