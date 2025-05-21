package org.promsnmp.promsnmp.controllers;

import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.promsnmp.promsnmp.services.PrometheusDiscoveryService;
import org.promsnmp.promsnmp.services.PrometheusMetricsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * This Controller supports requests for SNMP Metrics and Prometheus
 * HTTP Service (Targets) Discovery
 */

@Tag(name = "Metrics", description = "Endpoint for Prometheus Metric Scrapes")
@RestController
public class MetricsController {

    private final PrometheusMetricsService prometheusMetricsService;
    private final PrometheusDiscoveryService prometheusDiscoveryService;


    public MetricsController(@Qualifier("prometheusMetricsService") PrometheusMetricsService metricsService,
                             @Qualifier("prometheusDiscoveryService") PrometheusDiscoveryService prometheusDiscoveryService) {
        this.prometheusMetricsService = metricsService;
        this.prometheusDiscoveryService = prometheusDiscoveryService;
    }

    @Operation(summary = "Prometheus HTTP Service Discovery", description = "Prometheus Endpoint used to discovery PromSNMP managed Targets")
    @GetMapping(value = "/targets", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getPrometheusTargets() {
        return prometheusDiscoveryService.getTargets()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok("[]"));
    }

    /*
    @Operation(summary = "Prometheus HTTP Service Discovery", description = "Prometheus Endpoint used to discovery PromSNMP managed Targets")
    @GetMapping("/targets")
    public ResponseEntity<String> sampleServices() {
        return prometheusDiscoveryService.getTargets()
                .map(services -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(services))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"File not found\"}"));
    }
     */

    @Operation(summary = "Prometheus Metric Endpoint", description = "Scrape SNMP Metrics for a specified Target in the PromSNMP Inventory")
    @GetMapping("/snmp")
    public ResponseEntity<String> snmpMetrics(
            @Parameter(description = "Target in the current Inventory", example = "myrouter.promsnmp.com, 192.168.1.1")
            @RequestParam(required = true)
            String target) {

        return prometheusMetricsService.getMetrics(target, false)
                .map(metrics -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                        .body(metrics))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error reading file"));
    }

    @GetMapping(value = "/metrics", produces = "application/openmetrics-text; version=1.0.0; charset=utf-8")
    public void metrics(@RequestParam(name = "target", required = true) String target, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/openmetrics-text; version=1.0.0; charset=utf-8");

        try (OutputStream os = response.getOutputStream()) {
            // 1. Cached SNMP metrics (classic time series + classic histograms)
            prometheusMetricsService.getMetrics(target, false)
                    .ifPresentOrElse(
                            metrics -> {
                                try {
                                    os.write(metrics.getBytes(StandardCharsets.UTF_8));
                                    os.write("\n".getBytes(StandardCharsets.UTF_8)); // separator
                                } catch (IOException e) {
                                    throw new RuntimeException("Failed to write cached time series metrics", e);
                                }
                            },
                            () -> {
                                try {
                                    os.write("# no cached time series metrics found\n".getBytes(StandardCharsets.UTF_8));
                                } catch (IOException e) {
                                    throw new RuntimeException("Failed to write fallback message", e);
                                }
                            });

            // 2. Native histograms filtered by target's "instance" label
            MetricSnapshots snapshots = PrometheusRegistry.defaultRegistry.scrape();
            MetricSnapshots.Builder builder = MetricSnapshots.builder();

            for (MetricSnapshot snapshot : snapshots) {
                boolean match = snapshot.getDataPoints().stream()
                        .anyMatch(dp -> dp.getLabels().stream()
                                .anyMatch(label -> label.getName().equals("instance") &&
                                        label.getValue().equalsIgnoreCase(target)));

                if (match) {
                    builder.metricSnapshot(snapshot);
                }
            }

            MetricSnapshots filtered = builder.build();
            new OpenMetricsTextFormatWriter(true, false)
                    .write(os, filtered);
        }
    }


    //experimental
    @GetMapping("/metricsPlain")
    public ResponseEntity<String> getMetricsPlain() throws IOException {
        try {
            // Retrieve the default Prometheus registry
            PrometheusRegistry registry = PrometheusRegistry.defaultRegistry;

            // Collect metric snapshots
            MetricSnapshots snapshots = registry.scrape();

            // Initialize the OpenMetrics text format writer
            OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(true,false);

            // Write metrics to an output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            writer.write(outputStream, snapshots);

            // Convert the output stream to a UTF-8 string
            String metricsData = outputStream.toString(StandardCharsets.UTF_8);

            // Return the metrics with appropriate headers
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/openmetrics-text; version=1.0.0; charset=UTF-8")
                    .body(metricsData);

        } catch (IOException e) {
            // Handle exceptions and return a 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating metrics: " + e.getMessage());
        }
    }

    //experimental
    @GetMapping(value = "/allMetrics", produces = "application/openmetrics-text; version=1.0.0; charset=utf-8")
    public void allMetrics(HttpServletResponse response) throws IOException {

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/openmetrics-text; version=1.0.0; charset=utf-8");

        try (OutputStream output = response.getOutputStream()) {
            MetricSnapshots snapshots = PrometheusRegistry.defaultRegistry.scrape();
            OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(true, true);
            writer.write(output, snapshots);
        }
    }

    //experimental
    @GetMapping(value = "/filteredMetrics", produces = "application/openmetrics-text; version=1.0.0; charset=utf-8")
    public void filteredMetrics(@RequestParam(required = true) String target, HttpServletResponse response) throws IOException {

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try (OutputStream os = response.getOutputStream()) {
            MetricSnapshots snapshots = PrometheusRegistry.defaultRegistry.scrape();
            MetricSnapshots.Builder builder = MetricSnapshots.builder();

            for (MetricSnapshot snapshot : snapshots) {
                boolean match = snapshot.getDataPoints().stream()
                        .anyMatch(dp -> dp.getLabels().stream()
                                .anyMatch(label -> label.getName().equals("instance") &&
                                        label.getValue().equalsIgnoreCase(target)));

                if (match) {
                    builder.metricSnapshot(snapshot);
                }
            }

            MetricSnapshots filtered = builder.build();

            new OpenMetricsTextFormatWriter(true, false)
                    .write(os, filtered);
        }
    }
}






