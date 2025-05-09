package org.promsnmp.promsnmp.services.prometheus;

import org.promsnmp.promsnmp.services.PrometheusMetricsService;
import org.promsnmp.promsnmp.services.cache.CachedMetricsService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("SnmpSvc") // Matches key in ServiceApiConfig.java
public class SnmpBasedMetricsService implements PrometheusMetricsService {

    private final CachedMetricsService cachedMetrics;

    public SnmpBasedMetricsService(CachedMetricsService cachedMetrics) {
        this.cachedMetrics = cachedMetrics;
    }

    @Override
    public Optional<String> getMetrics(String instance, boolean regex) {
        return cachedMetrics.getRawMetrics(instance)
                //.map(this::formatMetrics)
                .map(metrics -> filterByInstance(metrics, instance, regex));
    }

    @Override
    public Optional<String> forceRefreshMetrics(String instance, boolean regex) {
        return cachedMetrics.refreshMetrics(instance);
    }

    private String formatMetrics(String rawMetrics) {
        return Stream.of(rawMetrics.split("\n"))
                .map(line -> line.matches("# (HELP|TYPE).*") ? "\n" + line : line)
                .collect(Collectors.joining("\n"));
    }

    private String filterByInstance(String metrics, String instance, boolean regex) {
        if (instance == null || instance.isBlank()) {
            return metrics;
        }

        StringBuilder filtered = new StringBuilder();
        String[] lines = metrics.split("\n");

        Pattern pattern = null;
        if (regex) {
            try {
                pattern = Pattern.compile(instance);
            } catch (PatternSyntaxException e) {
                return "Invalid regex pattern: " + instance;
            }
        }

        for (String line : lines) {
            if (line.isBlank() || line.startsWith("# HELP") || line.startsWith("# TYPE")) {
                filtered.append(line).append("\n");
            } else {
                boolean match = regex
                        ? pattern.matcher(line).find()
                        : line.contains("instance=\"" + instance + "\"");

                if (match) {
                    filtered.append(line).append("\n");
                }
            }
        }

        return filtered.toString();
    }
}
