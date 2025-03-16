package org.promsnmp.promsnmp.controllers;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/promSnmp")
public class DemoController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello World");
    }

    @GetMapping("/sample")
    public ResponseEntity<String> sampleData() {
        return readMetricsFile("static/prometheus-snmp-export.dat")
                .map(this::formatMetrics)
                .map(metrics -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                        .body(metrics))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error reading file"));
    }
    
    @GetMapping("/router")
    public ResponseEntity<String> routerData() {
        return readMetricsFile("static/advanced-router-metrics.dat")
                .map(this::formatMetrics)
                .map(metrics -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                        .body(metrics))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error reading router metrics file"));
    }
    
    @GetMapping("/metrics/{type}")
    public ResponseEntity<String> specificMetrics(
            @PathVariable String type,
            @RequestParam(required = false) String instance) {
        
        return readMetricsFile("static/advanced-router-metrics.dat")
                .map(content -> filterMetricsByType(content, type, instance))
                .map(this::formatMetrics)
                .map(metrics -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                        .body(metrics))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error reading or filtering metrics"));
    }
    
    private String filterMetricsByType(String content, String type, String instance) {
        StringBuilder result = new StringBuilder();
        boolean captureMetric = false;
        String[] lines = content.split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            // Check for metric help line (start of a metric block)
            if (line.startsWith("# HELP")) {
                captureMetric = line.contains("# HELP " + type);
                if (captureMetric) {
                    result.append(line).append("\n");
                    // Add the TYPE line that follows
                    if (i + 1 < lines.length && lines[i + 1].startsWith("# TYPE")) {
                        result.append(lines[i + 1]).append("\n");
                    }
                }
            } 
            // Add metric values if they match our filter
            else if (captureMetric && !line.startsWith("#")) {
                // If instance filter is provided, only include matching instances
                if (instance == null || line.contains("instance=\"" + instance + "\"")) {
                    result.append(line).append("\n");
                }
            } 
            // Handle blank lines between metric blocks
            else if (line.trim().isEmpty()) {
                if (captureMetric) {
                    result.append(line).append("\n");
                }
            }
        }
        
        return result.toString();
    }

    private Optional<String> readMetricsFile(String filePath) {
        return Optional.of(filePath)
                .map(ClassPathResource::new)
                .flatMap(resource -> {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                        return Optional.of(reader.lines().collect(Collectors.joining("\n")));
                    } catch (Exception e) {
                        return Optional.empty();
                    }
                });
    }

    private String formatMetrics(String rawMetrics) {
        return Stream.of(rawMetrics.split("\n"))
                .map(line -> line.matches("# (HELP|TYPE).*") ? "\n" + line : line) // Newline before HELP/TYPE lines
                .collect(Collectors.joining("\n"));
    }
}
