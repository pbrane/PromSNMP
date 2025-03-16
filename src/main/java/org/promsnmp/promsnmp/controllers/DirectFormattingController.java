package org.promsnmp.promsnmp.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Controller that demonstrates using the direct text formatting approach
 * instead of relying on a specific Prometheus client library.
 */
@RestController
@RequestMapping("/promSnmp/direct")
public class DirectFormattingController {

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

    @GetMapping
    public ResponseEntity<String> getDirectFormattedMetrics() {
        StringBuilder metricsBuilder = new StringBuilder();
        
        // Generate metrics for all routers
        for (RouterConfig router : routers) {
            addInterfaceMetrics(metricsBuilder, router);
            addSystemMetrics(metricsBuilder, router);
            metricsBuilder.append("\n\n");
        }
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                .body(metricsBuilder.toString());
    }
    
    @GetMapping("/{routerId}")
    public ResponseEntity<String> getRouterMetrics(
            @PathVariable String routerId,
            @RequestParam(required = false) String type) {
        
        // Find the requested router
        RouterConfig selectedRouter = null;
        for (RouterConfig router : routers) {
            if (router.hostname.startsWith(routerId)) {
                selectedRouter = router;
                break;
            }
        }
        
        if (selectedRouter == null) {
            return ResponseEntity.notFound().build();
        }
        
        StringBuilder metricsBuilder = new StringBuilder();
        
        // If type parameter is provided, filter metrics by type
        if (type != null && !type.isEmpty()) {
            if ("interface".equals(type)) {
                addInterfaceMetrics(metricsBuilder, selectedRouter);
            } else if ("system".equals(type)) {
                addSystemMetrics(metricsBuilder, selectedRouter);
            } else {
                return ResponseEntity.badRequest().body("Invalid metric type: " + type);
            }
        } else {
            // Otherwise return all metrics for the router
            addInterfaceMetrics(metricsBuilder, selectedRouter);
            addSystemMetrics(metricsBuilder, selectedRouter);
        }
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                .body(metricsBuilder.toString());
    }
    
    private void addInterfaceMetrics(StringBuilder builder, RouterConfig router) {
        // Loop through all interfaces
        for (int i = 0; i < router.interfaces.length; i++) {
            String ifIndex = String.valueOf(i + 1);
            String ifName = router.interfaces[i];
            String ifAlias = router.interfaceAliases[i];
            
            // Traffic counters use random values to simulate different traffic levels
            long inOctets = ThreadLocalRandom.current().nextLong(100_000_000_000L, 5_000_000_000_000L);
            long outOctets = ThreadLocalRandom.current().nextLong(100_000_000_000L, 5_000_000_000_000L);
            
            // Interface metrics - traffic counters
            builder.append(PromFormatUtil.formatCounter(
                "ifHCInOctets", 
                "The total number of octets received on the interface (High Capacity).",
                PromFormatUtil.labels("instance", router.hostname, "ifIndex", ifIndex, "ifName", ifName, "ifAlias", ifAlias),
                inOctets
            )).append("\n\n");
            
            builder.append(PromFormatUtil.formatCounter(
                "ifHCOutOctets", 
                "The total number of octets transmitted out of the interface (High Capacity).",
                PromFormatUtil.labels("instance", router.hostname, "ifIndex", ifIndex, "ifName", ifName, "ifAlias", ifAlias),
                outOctets
            )).append("\n\n");
            
            // Interface status - gauge metrics
            Map<String, String> labels = PromFormatUtil.labels(
                "instance", router.hostname, 
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
            PromFormatUtil.labels("instance", router.hostname),
            uptime
        )).append("\n\n");
        
        // CPU metrics
        Map<String, Double> cpuUsage = new HashMap<>();
        for (int i = 0; i < router.cpuCount; i++) {
            cpuUsage.put(String.valueOf(i), ThreadLocalRandom.current().nextDouble(10, 80));
        }
        
        for (Map.Entry<String, Double> cpu : cpuUsage.entrySet()) {
            Map<String, String> cpuLabels = PromFormatUtil.labels(
                "instance", router.hostname, 
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
            PromFormatUtil.labels("instance", router.hostname, "type", router.type),
            memoryUsage
        )).append("\n\n");
        
        // Calculate memory based on router type
        long multiplier = "Core Router".equals(router.type) ? 8L : ("Distribution Router".equals(router.type) ? 4L : 2L);
        long totalMemory = multiplier * 1024 * 1024 * 1024L; // in bytes
        long freeMemory = (long) (totalMemory * (100 - memoryUsage) / 100);
        
        builder.append(PromFormatUtil.formatMetricBlock(
            "memoryTotal", 
            "Total memory in bytes.",
            "gauge", 
            PromFormatUtil.labels("instance", router.hostname, "type", router.type),
            totalMemory
        )).append("\n\n");
        
        builder.append(PromFormatUtil.formatMetricBlock(
            "memoryFree", 
            "Available memory in bytes.",
            "gauge", 
            PromFormatUtil.labels("instance", router.hostname, "type", router.type),
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
                PromFormatUtil.labels("instance", router.hostname, "sensor", location),
                baseTemp + variance
            )).append("\n\n");
        }
    }
    
    /**
     * Simple class to hold router configuration data
     */
    private static class RouterConfig {
        final String hostname;
        final String type;
        final String location;
        final String[] interfaces;
        final String[] interfaceAliases;
        final int cpuCount;
        final int baseSpeed;
        
        RouterConfig(String hostname, String type, String location, 
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
}