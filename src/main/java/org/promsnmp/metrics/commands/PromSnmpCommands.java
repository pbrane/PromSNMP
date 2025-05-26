package org.promsnmp.metrics.commands;

import org.promsnmp.common.model.CommunityAgent;
import org.promsnmp.common.model.UserAgent;
import org.promsnmp.metrics.inventory.discovery.SnmpAgentDiscovery;
import org.promsnmp.metrics.repositories.jpa.CommunityAgentRepository;
import org.promsnmp.metrics.repositories.jpa.UserAgentRepository;
import org.promsnmp.metrics.services.PrometheusDiscoveryService;
import org.promsnmp.metrics.services.PrometheusMetricsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@ShellComponent
public class PromSnmpCommands {

    private final PrometheusMetricsService prometheusMetricsService;
    private final PrometheusDiscoveryService prometheusDiscoveryService;
    private final CacheManager cacheManager;
    private final SnmpAgentDiscovery snmpDiscoveryService;

    private final CommunityAgentRepository communityAgentRepository;
    private final UserAgentRepository userAgentRepository;

    public PromSnmpCommands(
            @Qualifier("prometheusMetricsService") PrometheusMetricsService prometheusMetricsService,
            @Qualifier("prometheusDiscoveryService") PrometheusDiscoveryService prometheusDiscoveryService,
            CacheManager cacheManager, SnmpAgentDiscovery snmpDiscoveryService, CommunityAgentRepository communityAgentRepository, UserAgentRepository userAgentRepository) {

        this.prometheusMetricsService = prometheusMetricsService;
        this.prometheusDiscoveryService = prometheusDiscoveryService;
        this.cacheManager = cacheManager;
        this.snmpDiscoveryService = snmpDiscoveryService;
        this.communityAgentRepository = communityAgentRepository;
        this.userAgentRepository = userAgentRepository;
    }

    @ShellMethod(key = "hello", value = "Returns a greeting message.")
    public String hello() {
        return "Hello World";
    }


    @ShellMethod(key = "collectSnmpMetrics", value = "Immediately collect SNMP metrics for a given instance. Use regex=true for pattern matching.")
    public String collectSnmpMetrics(
            @ShellOption(help = "Instance name (sysName)") String instance,
            @ShellOption(defaultValue = "false", help = "Enable regex matching") boolean regex) {

        return prometheusMetricsService.forceRefreshMetrics(instance, regex)
                .orElse("Collection failed or no metrics returned.");
    }

    @ShellMethod(key = "metrics", value = "Displays metric data from cache, optionally filtered by instance name.")
    public Optional<String> metrics(
            @ShellOption(defaultValue = "false", help = "Treat the instance filter as a regular expression.") boolean regex,
            @ShellOption(defaultValue = ShellOption.NULL, help = "Optional instance name to filter (e.g., router-1.example.com)") String instance) {

        if (instance == null || instance.isBlank()) {
            return Optional.of("Please provide a valid instance name.");
        }
        return prometheusMetricsService.getMetrics(instance, regex);
    }

    @ShellMethod(key = "services", value = "Displays services from prometheus-snmp-services.json")
    public String sampleServices() {
        return prometheusDiscoveryService.getTargets()
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
            @ShellOption(help = "Port", defaultValue = "161") Integer port,
            @ShellOption(help = "SNMP community string", defaultValue = "public") String community) {

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

    @ShellMethod(key = "showInventory", value = "Display Prometheus service discovery targets in JSON format.")
    public String serviceDiscovery() {
        return prometheusDiscoveryService.getTargets()
                .orElse("{\"error\": \"Unable to generate service discovery output.\"}");
    }

    @ShellMethod(key = "list-agents", value = "List all discovered SNMP agents.")
    public String listAllAgents() {
        StringBuilder output = new StringBuilder();

        List<CommunityAgent> communityAgents = communityAgentRepository.findAll();
        if (communityAgents.isEmpty()) {
            output.append("No CommunityAgents found.\n");
        } else {
            output.append("CommunityAgents:\n");
            for (CommunityAgent agent : communityAgents) {
                output.append(" - ")
                        .append(agent.getEndpoint())
                        .append(" → Device: ")
                        .append(agent.getDevice() != null ? agent.getDevice().getSysName() : "unlinked")
                        .append("\n");
            }
        }

        List<UserAgent> userAgents = userAgentRepository.findAll();
        if (userAgents.isEmpty()) {
            output.append("No UserAgents found.\n");
        } else {
            output.append("UserAgents:\n");
            for (UserAgent agent : userAgents) {
                output.append(" - ")
                        .append(agent.getEndpoint())
                        .append(" → Device: ")
                        .append(agent.getDevice() != null ? agent.getDevice().getSysName() : "unlinked")
                        .append("\n");
            }
        }

        return output.toString();
    }


}
