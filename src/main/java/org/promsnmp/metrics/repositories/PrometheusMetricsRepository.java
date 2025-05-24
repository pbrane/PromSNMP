package org.promsnmp.metrics.repositories;

import java.util.Optional;

public interface PrometheusMetricsRepository {

    Optional<String> readMetrics(String instance);

}
