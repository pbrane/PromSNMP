package org.promsnmp.promsnmp.controllers;

import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import jakarta.servlet.http.HttpServletResponse;
import org.promsnmp.promsnmp.services.PrometheusMetricsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

@RestController
public class MetricsController {

    private final PrometheusMetricsService prometheusMetricsService;

    public MetricsController(@Qualifier("prometheusMetricsService") PrometheusMetricsService metricsService) {
        this.prometheusMetricsService = metricsService;
    }

    @GetMapping("/snmp")
    public ResponseEntity<String> snmpMetrics(
            @RequestParam(required = false)
            String instance,
            @RequestParam(required = false, defaultValue = "false")
            Boolean regex ) {

        return prometheusMetricsService.getMetrics(instance, regex)
                .map(metrics -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                        .body(metrics))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error reading file"));
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
    @GetMapping(value = "/metrics2", produces = "application/openmetrics-text; version=1.0.0; charset=utf-8")
    public void metrics2(HttpServletResponse response) throws IOException {

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/openmetrics-text; version=1.0.0; charset=utf-8");

        try (OutputStream output = response.getOutputStream()) {
            MetricSnapshots snapshots = PrometheusRegistry.defaultRegistry.scrape();
            OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(true, true);
            writer.write(output, snapshots);
        }
    }

    //experimental
    @GetMapping(value = "/metrics", produces = "application/openmetrics-text; version=1.0.0; charset=utf-8")
    public void metrics(@RequestParam(required = false) String instance, HttpServletResponse response) throws IOException {

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try (OutputStream output = response.getOutputStream()) {
            MetricSnapshots snapshots = PrometheusRegistry.defaultRegistry.scrape();
            MetricSnapshots.Builder builder = MetricSnapshots.builder();

            for (MetricSnapshot snapshot : snapshots) {
                boolean match = snapshot.getDataPoints().stream()
                        .anyMatch(dp -> dp.getLabels().stream()
                                .anyMatch(label -> label.getName().equals("instance") &&
                                        label.getValue().equalsIgnoreCase(instance)));

                if (instance == null || instance.isEmpty() || match) {
                    builder.metricSnapshot(snapshot);
                }
            }

            MetricSnapshots filtered = builder.build();

            new OpenMetricsTextFormatWriter(true, false)
                    .write(output, filtered);
        }
    }
}






