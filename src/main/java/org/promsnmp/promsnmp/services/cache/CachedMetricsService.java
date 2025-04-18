package org.promsnmp.promsnmp.services.cache;

import org.promsnmp.promsnmp.repositories.PrometheusMetricsRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CachedMetricsService {

    private final PrometheusMetricsRepository repository;

    public CachedMetricsService(@Qualifier("configuredRepo") PrometheusMetricsRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "metrics", key = "#instance")
    public Optional<String> getRawMetrics(String instance) {
        return repository.readMetrics(instance);

    }
}
