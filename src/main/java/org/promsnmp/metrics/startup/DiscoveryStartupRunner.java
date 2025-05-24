package org.promsnmp.metrics.startup;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.promsnmp.metrics.inventory.discovery.SnmpDiscoveryScheduler;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DiscoveryStartupRunner implements ApplicationRunner {

    private final SnmpDiscoveryScheduler scheduler;
    private final Environment environment;

    public DiscoveryStartupRunner(SnmpDiscoveryScheduler scheduler, Environment environment) {
        this.scheduler = scheduler;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean discoverOnStart = environment.getProperty("DISCOVERY_ON_START", Boolean.class, false);
        log.warn(">>> ApplicationRunner.run() executing — DISCOVERY_ON_START = {}", discoverOnStart);

        if (!discoverOnStart) {
            log.warn("Skipping discovery on startup.");
            return;
        }

        scheduler.discoverOnStartup();
    }

    @PostConstruct
    public void dumpInterfaces() {
        for (Class<?> i : this.getClass().getInterfaces()) {
            log.warn("Implements interface: {}", i.getName());
        }
    }
}
