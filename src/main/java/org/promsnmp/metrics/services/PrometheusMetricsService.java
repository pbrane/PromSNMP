package org.promsnmp.metrics.services;

import java.util.Optional;

public interface PrometheusMetricsService {
    Optional<String> getMetrics(String instance, boolean regex);
    Optional<String> forceRefreshMetrics(String instance, boolean regex);
}
