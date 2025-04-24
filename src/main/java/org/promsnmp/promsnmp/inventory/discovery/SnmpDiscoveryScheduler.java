package org.promsnmp.promsnmp.inventory.discovery;

import org.promsnmp.promsnmp.inventory.InventoryPublisher;
import org.promsnmp.promsnmp.model.CommunityAgent;
import org.promsnmp.promsnmp.model.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SnmpDiscoveryScheduler {

    private static final Logger log = LoggerFactory.getLogger(SnmpDiscoveryScheduler.class);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final SnmpAgentDiscoveryService discoveryService;
    private final InventoryPublisher publisher;

    public SnmpDiscoveryScheduler(SnmpAgentDiscoveryService discoveryService, InventoryPublisher publisher) {
        this.discoveryService = discoveryService;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelayString = "PT10M")
    public void scheduledDiscovery() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Discovery already in progress — skipping this run.");
            return;
        }

        try {
            log.info("Starting scheduled SNMP discovery...");

            List<InetAddress> targets = List.of(
                    InetAddress.getByName("127.0.0.1"),
                    InetAddress.getByName("192.168.1.1"),
                    InetAddress.getByName("192.168.1.63")
            );

            CompletableFuture<List<CommunityAgent>> v2Future =
                    discoveryService.discoverMultiple(targets, 161, "public");

            CompletableFuture<List<UserAgent>> v3Future =
                    discoveryService.discoverMultipleV3(targets, 161, "demoUser", "authpass123", "privpass123");

            v2Future.thenAccept(v2Agents -> {
                log.info("SNMPv2 discovery complete: {} agents", v2Agents.size());
                publisher.publish(v2Agents);
            });

            v3Future.thenAccept(v3Agents -> {
                log.info("SNMPv3 discovery complete: {} agents", v3Agents.size());
                publisher.publish(v3Agents);
            });

        } catch (Exception e) {
            log.error("Scheduled discovery encountered an error", e);
        } finally {
            running.set(false);
        }
    }
}
