package org.promsnmp.promsnmp.commands;

import org.promsnmp.promsnmp.services.PromSnmpService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Objects;
import java.util.Optional;

@ShellComponent
public class PromSnmpCommands {

    private final PromSnmpService promSnmpService;
    private final CacheManager cacheManager;


    public PromSnmpCommands(@Qualifier("configuredService") PromSnmpService promSnmpService, CacheManager cacheManager) {
        this.promSnmpService = promSnmpService;
        this.cacheManager = cacheManager;
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

}
