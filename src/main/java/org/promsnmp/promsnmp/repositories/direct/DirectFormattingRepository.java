package org.promsnmp.promsnmp.repositories.direct;

import org.promsnmp.promsnmp.utils.PromFormatUtil;
import org.promsnmp.promsnmp.model.RouterConfig;
import org.promsnmp.promsnmp.repositories.PromSnmpRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Repository that demonstrates using the direct text formatting approach
 * instead of relying on a specific Prometheus client library.
 */
@Repository("DirectRepo")
public class DirectFormattingRepository implements PromSnmpRepository {

    // Define router configurations
    private final List<RouterConfig> routers = Arrays.asList(
            new RouterConfig("router1.example.com", "Core Router", "ISP-Edge",
                    new String[]{"GigabitEthernet0/0/0", "GigabitEthernet0/0/1", "GigabitEthernet0/0/2"},
                    new String[]{"WAN-Link", "LAN-Primary", "Management"},
                    4, 1000),
            new RouterConfig("router2.example.com", "Distribution Router", "Internal",
                    new String[]{"GigabitEthernet1/0/0", "GigabitEthernet1/0/1", "GigabitEthernet1/0/2", "GigabitEthernet1/0/3"},
                    new String[]{"Core-Uplink", "Branch-1", "Branch-2", "DMZ"},
                    8, 1000),
            new RouterConfig("switch1.example.com", "Access Switch", "Floor-1",
                    new String[]{"FastEthernet0/1", "FastEthernet0/2", "GigabitEthernet0/1"},
                    new String[]{"User-VLAN", "IoT-VLAN", "Uplink"},
                    2, 100)
    );


    @Override
    public Resource readMetrics(String instance) {

        String metrics = null;
        if (instance == null) {
            metrics = getDirectFormattedMetrics();
        } else {
            metrics = getRouterMetrics(instance, "router");
        }
        return new ByteArrayResource(metrics.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Resource readServices() {
        return null;
    }

    public String getDirectFormattedMetrics() {
        StringBuilder metricsBuilder = new StringBuilder();

        // Generate metrics for all routers
        for (RouterConfig router : routers) {
            addInterfaceMetrics(metricsBuilder, router);
            addSystemMetrics(metricsBuilder, router);
            metricsBuilder.append("\n\n");
        }

        return metricsBuilder.toString();
    }

    public String getRouterMetrics(String routerId, String type) {

        // Find the requested router
        RouterConfig selectedRouter = null;
        for (RouterConfig router : routers) {
            if (router.getHostname().startsWith(routerId)) {
                selectedRouter = router;
                break;
            }
        }

        if (selectedRouter == null) {
            return null;
        }

        StringBuilder metricsBuilder = new StringBuilder();

        // If type parameter is provided, filter metrics by type
        if (type != null && !type.isEmpty()) {
            if ("interface".equals(type)) {
                addInterfaceMetrics(metricsBuilder, selectedRouter);
            } else if ("system".equals(type)) {
                addSystemMetrics(metricsBuilder, selectedRouter);
            } else {
                return null;
            }
        } else {
            // Otherwise return all metrics for the router
            addInterfaceMetrics(metricsBuilder, selectedRouter);
            addSystemMetrics(metricsBuilder, selectedRouter);
        }

        return metricsBuilder.toString();
    }

    private void addInterfaceMetrics(StringBuilder builder, RouterConfig router) {
        // Loop through all interfaces
        for (int i = 0; i < router.getInterfaces().length; i++) {
            String ifIndex = String.valueOf(i + 1);
            String ifName = router.getInterfaces()[i];
            String ifAlias = router.getInterfaceAliases()[i];

            // Traffic counters use random values to simulate different traffic levels
            long inOctets = ThreadLocalRandom.current().nextLong(100_000_000_000L, 5_000_000_000_000L);
            long outOctets = ThreadLocalRandom.current().nextLong(100_000_000_000L, 5_000_000_000_000L);

            // Interface metrics - traffic counters
            builder.append(PromFormatUtil.formatCounter(
                    "ifHCInOctets",
                    "The total number of octets received on the interface (High Capacity).",
                    PromFormatUtil.labels("instance", router.getHostname(), "ifIndex", ifIndex, "ifName", ifName, "ifAlias", ifAlias),
                    inOctets
            )).append("\n\n");

            builder.append(PromFormatUtil.formatCounter(
                    "ifHCOutOctets",
                    "The total number of octets transmitted out of the interface (High Capacity).",
                    PromFormatUtil.labels("instance", router.getHostname(), "ifIndex", ifIndex, "ifName", ifName, "ifAlias", ifAlias),
                    outOctets
            )).append("\n\n");

            // Interface status - gauge metrics
            Map<String, String> labels = PromFormatUtil.labels(
                    "instance", router.getHostname(),
                    "ifIndex", ifIndex,
                    "ifName", ifName,
                    "ifAlias", ifAlias
            );

            // Most interfaces are up (1), but occasionally one might be down (2)
            int operStatus = ThreadLocalRandom.current().nextInt(10) > 0 ? 1 : 2;

            builder.append(PromFormatUtil.formatMetricBlock(
                    "ifOperStatus",
                    "Operational status of the interface (1=up, 2=down, 3=testing).",
                    "gauge",
                    labels,
                    operStatus
            )).append("\n\n");

            // Interface speed based on router type
            int speed = ifName.startsWith("Gigabit") ? 1000 : 100;

            builder.append(PromFormatUtil.formatMetricBlock(
                    "ifHighSpeed",
                    "Interface speed in Mbps.",
                    "gauge",
                    labels,
                    speed
            )).append("\n\n");

            // Add error and discard counters
            int errorRate = ThreadLocalRandom.current().nextInt(100);
            if (errorRate < 20) {  // 20% chance of having errors
                int inErrors = ThreadLocalRandom.current().nextInt(50);
                int outErrors = ThreadLocalRandom.current().nextInt(30);

                builder.append(PromFormatUtil.formatCounter(
                        "ifInErrors",
                        "Number of inbound packets with errors.",
                        labels,
                        inErrors
                )).append("\n\n");

                builder.append(PromFormatUtil.formatCounter(
                        "ifOutErrors",
                        "Number of outbound packets with errors.",
                        labels,
                        outErrors
                )).append("\n\n");
            }
        }
    }

    private void addSystemMetrics(StringBuilder builder, RouterConfig router) {
        // System uptime (in seconds)
        long uptime = ThreadLocalRandom.current().nextLong(86400, 31536000); // 1 day to 1 year
        builder.append(PromFormatUtil.formatMetricBlock(
                "sysUpTime",
                "System uptime in seconds.",
                "counter",
                PromFormatUtil.labels("instance", router.getHostname()),
                uptime
        )).append("\n\n");

        // CPU metrics
        Map<String, Double> cpuUsage = new HashMap<>();
        for (int i = 0; i < router.getCpuCount(); i++) {
            cpuUsage.put(String.valueOf(i), ThreadLocalRandom.current().nextDouble(10, 80));
        }

        for (Map.Entry<String, Double> cpu : cpuUsage.entrySet()) {
            Map<String, String> cpuLabels = PromFormatUtil.labels(
                    "instance", router.getHostname(),
                    "cpu", cpu.getKey()
            );

            builder.append(PromFormatUtil.formatMetricBlock(
                    "cpuUsage",
                    "CPU usage percentage.",
                    "gauge",
                    cpuLabels,
                    cpu.getValue()
            )).append("\n\n");
        }

        // Memory metrics
        double memoryUsage = ThreadLocalRandom.current().nextDouble(30, 90);
        builder.append(PromFormatUtil.formatMetricBlock(
                "memoryUsage",
                "Memory usage percentage.",
                "gauge",
                PromFormatUtil.labels("instance", router.getHostname(), "type", router.getType()),
                memoryUsage
        )).append("\n\n");

        // Calculate memory based on router type
        long multiplier = "Core Router".equals(router.getType()) ? 8L : ("Distribution Router".equals(router.getType()) ? 4L : 2L);
        long totalMemory = multiplier * 1024 * 1024 * 1024L; // in bytes
        long freeMemory = (long) (totalMemory * (100 - memoryUsage) / 100);

        builder.append(PromFormatUtil.formatMetricBlock(
                "memoryTotal",
                "Total memory in bytes.",
                "gauge",
                PromFormatUtil.labels("instance", router.getHostname(), "type", router.getType()),
                totalMemory
        )).append("\n\n");

        builder.append(PromFormatUtil.formatMetricBlock(
                "memoryFree",
                "Available memory in bytes.",
                "gauge",
                PromFormatUtil.labels("instance", router.getHostname(), "type", router.getType()),
                freeMemory
        )).append("\n\n");

        // Add temperature sensor readings
        String[] sensorLocations = {"CPU", "Intake", "Exhaust", "Power Supply"};
        for (String location : sensorLocations) {
            // Different base temperatures based on sensor location
            double baseTemp = "CPU".equals(location) ? 45 : ("Exhaust".equals(location) ? 38 : 28);
            double variance = ThreadLocalRandom.current().nextDouble(-3, 5);

            builder.append(PromFormatUtil.formatMetricBlock(
                    "temperatureSensor",
                    "Temperature in Celsius.",
                    "gauge",
                    PromFormatUtil.labels("instance", router.getHostname(), "sensor", location),
                    baseTemp + variance
            )).append("\n\n");
        }
    }

}