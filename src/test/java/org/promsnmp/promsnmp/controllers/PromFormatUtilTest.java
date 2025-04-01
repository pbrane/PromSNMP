package org.promsnmp.promsnmp.controllers;

import org.junit.jupiter.api.Test;
import org.promsnmp.promsnmp.utils.PromFormatUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PromFormatUtilTest {

    @Test
    void testFormatHelp() {
        String help = PromFormatUtil.formatHelp("metric_name", "This is a help message");
        assertEquals("# HELP metric_name This is a help message", help);
    }

    @Test
    void testFormatType() {
        String type = PromFormatUtil.formatType("metric_name", "counter");
        assertEquals("# TYPE metric_name counter", type);
        
        // Test with invalid type
        assertThrows(IllegalArgumentException.class, () -> {
            PromFormatUtil.formatType("metric_name", "invalid_type");
        });
    }

    @Test
    void testFormatMetricWithValues() {
        Map<String, String> labels = new HashMap<>();
        labels.put("instance", "router.example.com");
        labels.put("ifIndex", "1");
        
        // Test with integer value
        String metric = PromFormatUtil.formatMetric("ifHCInOctets", labels, 1234567890L);
        // Since HashMap order is unpredictable, check parts
        assertTrue(metric.startsWith("ifHCInOctets{"));
        assertTrue(metric.endsWith("} 1234567890"));
        assertTrue(metric.contains("instance=\"router.example.com\""));
        assertTrue(metric.contains("ifIndex=\"1\""));
        
        // Test with double value
        String metricDouble = PromFormatUtil.formatMetric("cpuUsage", labels, 45.7);
        assertTrue(metricDouble.startsWith("cpuUsage{"));
        assertTrue(metricDouble.contains("instance=\"router.example.com\""));
        assertTrue(metricDouble.contains("ifIndex=\"1\""));
        assertTrue(metricDouble.endsWith("} 45.7"));
        
        // Test with integer-like double (should format as integer)
        String metricIntegerLike = PromFormatUtil.formatMetric("uptime", labels, 123.0);
        assertTrue(metricIntegerLike.startsWith("uptime{"));
        assertTrue(metricIntegerLike.contains("instance=\"router.example.com\""));
        assertTrue(metricIntegerLike.contains("ifIndex=\"1\""));
        assertTrue(metricIntegerLike.endsWith("} 123"));
    }
    
    @Test
    void testFormatWithNoLabels() {
        String metric = PromFormatUtil.formatMetric("process_start_time_seconds", 1616403245.3);
        assertTrue(metric.startsWith("process_start_time_seconds "));
        // Different JDKs/platforms might format doubles differently (scientific vs decimal)
        // Just check it's a valid number format
        assertTrue(metric.matches("process_start_time_seconds [0-9.E]+"));
    }

    @Test
    void testFormatLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("instance", "router.example.com");
        labels.put("ifName", "GigabitEthernet0/0/0");
        labels.put("ifAlias", "WAN-Link");
        
        String result = PromFormatUtil.formatLabels(labels);
        // We can't assert the exact string since the order of map entries isn't guaranteed
        assertTrue(result.startsWith("{"));
        assertTrue(result.endsWith("}"));
        assertTrue(result.contains("instance=\"router.example.com\""));
        assertTrue(result.contains("ifName=\"GigabitEthernet0/0/0\""));
        assertTrue(result.contains("ifAlias=\"WAN-Link\""));
    }
    
    @Test
    void testEscapeString() {
        assertEquals("normal", PromFormatUtil.escapeString("normal"));
        assertEquals("with\\\"quotes\\\"", PromFormatUtil.escapeString("with\"quotes\""));
        assertEquals("with\\\\backslash", PromFormatUtil.escapeString("with\\backslash"));
        assertEquals("with\\nline", PromFormatUtil.escapeString("with\nline"));
    }
    
    @Test
    void testFormatValue() {
        assertEquals("123", PromFormatUtil.formatValue(123.0));
        assertEquals("123.45", PromFormatUtil.formatValue(123.45));
    }
    
    @Test
    void testFormatCounter() {
        String counter = PromFormatUtil.formatCounter(
            "http_requests_total", 
            "Total number of HTTP requests made", 
            PromFormatUtil.labels("method", "GET", "status", "200"),
            42
        );
        
        String[] lines = counter.split("\n");
        assertEquals(3, lines.length);
        assertEquals("# HELP http_requests_total Total number of HTTP requests made", lines[0]);
        assertEquals("# TYPE http_requests_total counter", lines[1]);
        assertEquals("http_requests_total{method=\"GET\",status=\"200\"} 42", lines[2]);
    }
    
    @Test
    void testLabelsMethod() {
        Map<String, String> labels = PromFormatUtil.labels("key1", "value1", "key2", "value2");
        assertEquals(2, labels.size());
        assertEquals("value1", labels.get("key1"));
        assertEquals("value2", labels.get("key2"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            PromFormatUtil.labels("key1", "value1", "key2");
        });
    }
    
    @Test
    void testCompleteMetricBlock() {
        // Create a complete metric with all components
        String metricBlock = PromFormatUtil.formatMetricBlock(
            "network_bytes_total",
            "Total network bytes transmitted",
            "counter",
            PromFormatUtil.labels("direction", "out", "device", "eth0"),
            1234567890L
        );
        
        String[] lines = metricBlock.split("\n");
        assertEquals(3, lines.length);
        assertEquals("# HELP network_bytes_total Total network bytes transmitted", lines[0]);
        assertEquals("# TYPE network_bytes_total counter", lines[1]);
        assertTrue(lines[2].startsWith("network_bytes_total{"));
        assertTrue(lines[2].contains("direction=\"out\""));
        assertTrue(lines[2].contains("device=\"eth0\""));
        assertTrue(lines[2].endsWith("} 1234567890"));
    }
}