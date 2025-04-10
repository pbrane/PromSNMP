package org.promsnmp.promsnmp.repositories.prometheus;

import org.promsnmp.promsnmp.repositories.PrometheusMetricsRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("jpaRepo")
public class JpaPrometheusMetricsRepository implements PrometheusMetricsRepository {
    @Override
    public Optional<String> readMetrics(String instance) {
        return Optional.empty();
    }
}
