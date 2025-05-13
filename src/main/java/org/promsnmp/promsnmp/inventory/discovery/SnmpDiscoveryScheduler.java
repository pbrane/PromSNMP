package org.promsnmp.promsnmp.inventory.discovery;

import lombok.extern.slf4j.Slf4j;
import org.promsnmp.promsnmp.inventory.InventoryPublisher;
import org.promsnmp.promsnmp.model.CommunityAgent;
import org.promsnmp.promsnmp.model.UserAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class SnmpDiscoveryScheduler {

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final SnmpAgentDiscovery discoveryService;
    private final InventoryPublisher publisher;

    //doing this so we can debug the value
    @Value("${DISCOVERY_CRON:0 0 2 * * *}")
    private String discoveryCronExpression;

    @Value("${DISCOVERY_TZ:America/New_York}")
    private String discoveryZone;

    public SnmpDiscoveryScheduler(SnmpAgentDiscovery discoveryService, InventoryPublisher publisher) {
        log.info("SnmpDiscoveryScheduler constructor called");
        this.discoveryService = discoveryService;
        this.publisher = publisher;
    }

    public void discoverOnStartup() {
        log.info("DISCOVER_ON_START is true — performing initial discovery...");
        CompletableFuture.runAsync(() -> {
            try {
                scheduledDiscovery();
            } finally {
                running.set(false);
            }
        });
    }

    @Scheduled(cron = "${discovery.cron:0 0 2 * * *}", zone = "${discovery.zone:America/New_York}")
    public void scheduledDiscovery() {

        if (!running.compareAndSet(false, true)) {
            log.warn("Discovery already in progress — skipping this run.");
            log.debug("Cron: {}, TZ: {}", discoveryCronExpression, discoveryZone);
            return;
        }

        try {
            log.info("Starting scheduled SNMP discovery...");

            List<InetAddress> targets = List.of(
                    InetAddress.getByName("10.0.0.1"),
                    InetAddress.getByName("127.0.0.1"),
                    InetAddress.getByName("192.168.1.1"),
                    InetAddress.getByName("192.168.1.63")
            );

            CompletableFuture<List<CommunityAgent>> v2Future =
                    discoveryService.discoverMultiple(targets, 161, "public");

            CompletableFuture<List<UserAgent>> v3Future =
                    discoveryService.discoverMultipleV3(targets, 161, "demoUser", "SHA256", "authpass123", "AES256", "privpass123");

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
