package org.promsnmp.promsnmp.repositories;

import java.util.Optional;

public interface PrometheusMetricsRepository {

    Optional<String> readMetrics(String instance);

}
