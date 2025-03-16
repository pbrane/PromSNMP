package org.promsnmp.promsnmp.controllers;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests demonstrating the direct use of the Prometheus Java client
 * to generate metrics in a format compatible with our existing endpoints.
 */
class PrometheusClientTest {

    private CollectorRegistry registry;

    @BeforeEach
    void setUp() {
        // Create a new registry for each test to avoid interference
        registry = new CollectorRegistry();
    }

    @Test
    void testCreateCounterMetric() {
        // Create a counter metric similar to ifHCInOctets
        Counter bytesCounter = Counter.build()
                .name("ifHCInOctets")
                .help("The total number of octets received on the interface (High Capacity).")
                .labelNames("instance", "ifIndex", "ifName")
                .register(registry);

        // Add some sample data
        bytesCounter.labels("router.example.com", "1", "GigabitEthernet0/0/0").inc(1532687459326L);
        bytesCounter.labels("router.example.com", "2", "GigabitEthernet0/0/1").inc(843215678923L);

        // Get the metrics as a string
        String result = getMetricsAsString(registry);

        // Verify the output format - print the result for debugging
        System.out.println("Metrics output:\n" + result);
        
        // Just check for the essential parts
        assertTrue(result.contains("ifHCInOctets"));
        assertTrue(result.contains("counter"));
        assertTrue(result.contains("router.example.com"));
    }

    @Test
    void testCreateGaugeMetric() {
        // Create a gauge metric similar to cpuUsage
        Gauge cpuGauge = Gauge.build()
                .name("cpuUsage")
                .help("CPU usage percentage.")
                .labelNames("instance", "cpu")
                .register(registry);

        // Add some sample data
        cpuGauge.labels("router.example.com", "0").set(42.5);
        cpuGauge.labels("router.example.com", "1").set(38.2);

        // Get the metrics as a string
        String result = getMetricsAsString(registry);

        // Verify the output format
        assertTrue(result.contains("# HELP cpuUsage CPU usage percentage."));
        assertTrue(result.contains("# TYPE cpuUsage gauge"));
        assertTrue(result.contains("cpuUsage{instance=\"router.example.com\",cpu=\"0\",} 42.5"));
        assertTrue(result.contains("cpuUsage{instance=\"router.example.com\",cpu=\"1\",} 38.2"));
    }

    @Test
    void testCreateResponseEntityFromMetrics() {
        // Create a gauge metric for a network device
        Gauge interfaceStatus = Gauge.build()
                .name("ifOperStatus")
                .help("Operational status of the interface (1=up, 2=down, 3=testing).")
                .labelNames("instance", "ifIndex", "ifName")
                .register(registry);

        // Add sample data
        interfaceStatus.labels("router.example.com", "1", "GigabitEthernet0/0/0").set(1);
        interfaceStatus.labels("router.example.com", "2", "GigabitEthernet0/0/1").set(2);

        // Get the metrics as string and print for debugging
        String metricsString = getMetricsAsString(registry);
        System.out.println("Response metrics output:\n" + metricsString);
        
        // Create a response entity similar to what our controller does
        ResponseEntity<String> response = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, TextFormat.CONTENT_TYPE_004)
                .body(metricsString);

        // Verify the response
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(TextFormat.CONTENT_TYPE_004, response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("# HELP ifOperStatus Operational status of the interface (1=up, 2=down, 3=testing)."));
        assertTrue(body.contains("# TYPE ifOperStatus gauge"));
        assertTrue(body.contains("ifOperStatus{instance=\"router.example.com\",ifIndex=\"1\",ifName=\"GigabitEthernet0/0/0\",}"));
    }

    /**
     * Helper method to get metrics from a registry as a string in Prometheus text format.
     */
    private String getMetricsAsString(CollectorRegistry registry) {
        try {
            Writer writer = new StringWriter();
            TextFormat.write004(writer, registry.metricFamilySamples());
            return writer.toString();
        } catch (IOException e) {
            fail("Failed to get metrics as string: " + e.getMessage());
            return "";
        }
    }
}