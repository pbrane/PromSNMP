package org.promsnmp.metrics.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for creating Prometheus-formatted metrics.
 * Provides direct text formatting instead of relying on client library implementations
 * to ensure consistent format across different Prometheus clients.
 */
public class PromFormatUtil {

    /**
     * Creates a help line for a metric in Prometheus format.
     * @param name The name of the metric
     * @param help The help text describing the metric
     * @return A formatted help line
     */
    public static String formatHelp(String name, String help) {
        return String.format("# HELP %s %s", name, help);
    }

    /**
     * Creates a type line for a metric in Prometheus format.
     * @param name The name of the metric
     * @param type The type of the metric (counter, gauge, histogram, summary)
     * @return A formatted type line
     */
    public static String formatType(String name, String type) {
        if (!isValidType(type)) {
            throw new IllegalArgumentException("Invalid metric type: " + type + 
                ". Must be one of: counter, gauge, histogram, summary");
        }
        return String.format("# TYPE %s %s", name, type);
    }

    /**
     * Creates a metric line for a counter or gauge in Prometheus format.
     * @param name The name of the metric
     * @param labels A map of label names to values
     * @param value The value of the metric
     * @return A formatted metric line
     */
    public static String formatMetric(String name, Map<String, String> labels, double value) {
        return String.format("%s%s %s", 
            name, 
            formatLabels(labels), 
            formatValue(value));
    }

    /**
     * Creates a metric line for a counter or gauge in Prometheus format.
     * @param name The name of the metric
     * @param labels A map of label names to values
     * @param value The value of the metric as a long (for large counters)
     * @return A formatted metric line
     */
    public static String formatMetric(String name, Map<String, String> labels, long value) {
        return String.format("%s%s %d", 
            name, 
            formatLabels(labels), 
            value);
    }
    
    /**
     * Creates a metric line for a counter or gauge in Prometheus format with no labels.
     * @param name The name of the metric
     * @param value The value of the metric
     * @return A formatted metric line
     */
    public static String formatMetric(String name, double value) {
        return formatMetric(name, Collections.emptyMap(), value);
    }
    
    /**
     * Creates a metric block with help, type and value lines.
     * @param name The name of the metric
     * @param help The help text describing the metric
     * @param type The type of the metric (counter, gauge, histogram, summary)
     * @param labels A map of label names to values
     * @param value The value of the metric
     * @return A complete metric block
     */
    public static String formatMetricBlock(String name, String help, String type, 
                                          Map<String, String> labels, double value) {
        return formatHelp(name, help) + "\n" +
               formatType(name, type) + "\n" +
               formatMetric(name, labels, value);
    }

    /**
     * Formats a set of labels in Prometheus format: {label1="value1",label2="value2"}
     * @param labels A map of label names to values
     * @return A formatted label string
     */
    public static String formatLabels(Map<String, String> labels) {
        if (labels == null || labels.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append(entry.getKey())
              .append("=\"")
              .append(escapeString(entry.getValue()))
              .append("\"");
            first = false;
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Escapes special characters in string values for Prometheus metrics.
     * @param s The string to escape
     * @return An escaped string
     */
    public static String escapeString(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
    
    /**
     * Formats a numeric value according to Prometheus best practices.
     * @param value The numeric value
     * @return A formatted value string
     */
    public static String formatValue(double value) {
        // Use simple formatting for integers
        if (value == (long) value) {
            return Long.toString((long) value);
        }
        
        // For normal floating point values
        return Double.toString(value);
    }
    
    /**
     * Creates a counter metric with the correct naming (no _total suffix).
     * @param name The metric name
     * @param help The help text
     * @param labels The labels
     * @param value The counter value
     * @return A formatted counter metric
     */
    public static String formatCounter(String name, String help, Map<String, String> labels, long value) {
        // Don't add _total suffix as the Java client does
        return formatMetricBlock(name, help, "counter", labels, value);
    }
    
    /**
     * Create a simple label map from variable arguments.
     * @param keyValues Alternating key-value pairs
     * @return A map of labels
     */
    public static Map<String, String> labels(String... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Labels must be provided as key-value pairs");
        }
        
        Map<String, String> labels = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            labels.put(keyValues[i], keyValues[i + 1]);
        }
        return labels;
    }
    
    /**
     * Checks if the metric type is valid.
     * @param type The type to check
     * @return True if valid, false otherwise
     */
    private static boolean isValidType(String type) {
        return "counter".equals(type) || 
               "gauge".equals(type) || 
               "histogram".equals(type) || 
               "summary".equals(type);
    }
}