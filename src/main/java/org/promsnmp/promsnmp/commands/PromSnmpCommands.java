package org.promsnmp.promsnmp.commands;

import org.promsnmp.promsnmp.inventory.discovery.SnmpAgentDiscoveryService;
import org.promsnmp.promsnmp.model.CommunityAgent;
import org.promsnmp.promsnmp.services.PromSnmpService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.net.InetAddress;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@ShellComponent
public class PromSnmpCommands {

    private final PromSnmpService promSnmpService;
    private final CacheManager cacheManager;
    private final SnmpAgentDiscoveryService snmpDiscoveryService;

    public PromSnmpCommands(@Qualifier("configuredService") PromSnmpService promSnmpService, CacheManager cacheManager, SnmpAgentDiscoveryService snmpDiscoveryService) {
        this.promSnmpService = promSnmpService;
        this.cacheManager = cacheManager;
        this.snmpDiscoveryService = snmpDiscoveryService;
    }

    @ShellMethod(key = "hello", value = "Returns a greeting message.")
    public String hello() {
        return "Hello World";
    }

    @ShellMethod(key = "metrics", value = "Displays sample metric data, optionally filtered by instance name.")
    public Optional<String> metrics(
            @ShellOption(defaultValue = "false", help = "Treat the instance filter as a regular expression.") boolean regex,
            @ShellOption(defaultValue = ShellOption.NULL, help = "Optional instance name to filter (e.g., router-1.example.com)") String instance) {

        if (instance == null || instance.isBlank()) {
            return Optional.of("Please provide a valid instance name.");
        }
        return promSnmpService.getMetrics(instance, regex);
    }

    @ShellMethod(key = "services", value = "Displays services from prometheus-snmp-services.json")
    public String sampleServices() {
        return promSnmpService.getServices()
                .orElse("{\"error\": \"File not found\"}");
    }

    @ShellMethod(key = "evictCache", value = "Evict all cached metrics")
    public String evictAll() {
        Objects.requireNonNull(cacheManager.getCache("metrics")).clear();
        return "Cache cleared.";
    }

    @ShellMethod(key = "discoverAgent", value = "Discover an SNMPv2 agent at a given IP address and port with community string")
    public String discoverOne(
            @ShellOption(help = "IP address", defaultValue = "127.0.0.1") String ip,
            @ShellOption(help = "Port", defaultValue = "161") int port,
            @ShellOption(help = "SNMP community string", defaultValue = "public") String community
    ) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            CompletableFuture<Optional<CommunityAgent>> future =
                    snmpDiscoveryService.discoverCommunityAgent(address, port, community);

            Optional<CommunityAgent> result = future.join();
            return result.map(agent -> "Discovered agent: " + agent.getEndpoint())
                    .orElse("No SNMP agent discovered at " + ip + ":" + port);
        } catch (Exception e) {
            return "Discovery failed: " + e.getMessage();
        }
    }

}
