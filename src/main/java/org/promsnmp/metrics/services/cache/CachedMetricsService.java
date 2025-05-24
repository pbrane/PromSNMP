package org.promsnmp.metrics.services.cache;

import org.promsnmp.metrics.repositories.PrometheusMetricsRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CachedMetricsService {

    private final PrometheusMetricsRepository repository;

    public CachedMetricsService(@Qualifier("configuredMetricsRepo") PrometheusMetricsRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "metrics", key = "#instance")
    public Optional<String> getRawMetrics(String instance) {
        return repository.readMetrics(instance);
    }

    @Transactional
    @CachePut(value = "metrics", key = "#instance")
    public Optional<String> refreshMetrics(String instance) {
        return repository.readMetrics(instance);
    }
}

