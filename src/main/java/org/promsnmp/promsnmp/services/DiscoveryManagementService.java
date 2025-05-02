package org.promsnmp.promsnmp.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.promsnmp.promsnmp.dto.DiscoveryRequestDTO;
import org.promsnmp.promsnmp.inventory.InventoryPublisher;
import org.promsnmp.promsnmp.inventory.discovery.SnmpAgentDiscoveryService;
import org.promsnmp.promsnmp.utils.IpUtils;
import org.springframework.stereotype.Service;
import static org.promsnmp.promsnmp.utils.Snmp4jUtils.resolveSnmpVersion;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscoveryManagementService {

    private final SnmpAgentDiscoveryService discoveryService;
    private final DiscoverySeedService seedService;
    private final InventoryPublisher inventoryPublisher;

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
                        seedService.saveSeed(request);
                    }
                    agents.forEach(a -> {
                        a.setVersion(snmpVersion);
                        a.getDevice().setPrimaryAgent(a);
                    });
                    inventoryPublisher.publish(agents);
                });
            } else {
                seedService.saveSeed(request);
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
                        seedService.saveSeed(request);
                    }
                    agents.forEach(a -> {
                        a.setVersion(snmpVersion);
                        a.getDevice().setPrimaryAgent(a);
                    });
                    inventoryPublisher.publish(agents);
                });
            } else {
                seedService.saveSeed(request);
            }

        } else {
            throw new IllegalArgumentException("Unsupported agent type: " + request.getAgentType());
        }
    }

}
