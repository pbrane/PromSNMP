package org.promsnmp.metrics.model;

import lombok.Getter;

@Getter
/**
 * Simple class to hold router configuration data
 */
public class RouterConfig {
    private final String hostname;
    private final String type;
    private final String location;
    private final String[] interfaces;
    private final String[] interfaceAliases;
    private final int cpuCount;
    private final int baseSpeed;

    public RouterConfig(String hostname, String type, String location,
                        String[] interfaces, String[] interfaceAliases,
                        int cpuCount, int baseSpeed) {
        this.hostname = hostname;
        this.type = type;
        this.location = location;
        this.interfaces = interfaces;
        this.interfaceAliases = interfaceAliases;
        this.cpuCount = cpuCount;
        this.baseSpeed = baseSpeed;
    }
}
