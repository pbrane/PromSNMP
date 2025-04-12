package org.promsnmp.promsnmp.services;

import java.util.Optional;

public interface PrometheusMetricsService {
    Optional<String> getMetrics(String instance, boolean regex);
}
