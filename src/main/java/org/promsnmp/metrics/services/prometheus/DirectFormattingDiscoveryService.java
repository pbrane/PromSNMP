package org.promsnmp.metrics.services.prometheus;

import org.promsnmp.metrics.services.PrometheusDiscoveryService;
import org.promsnmp.metrics.services.PrometheusMetricsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("directService")
public class DirectFormattingDiscoveryService implements PrometheusDiscoveryService, PrometheusMetricsService {
    @Override
    public Optional<String> getTargets() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getMetrics(String instance, boolean regex) {
        return Optional.empty();
    }

    @Override
    public Optional<String> forceRefreshMetrics(String instance, boolean regex) {
        return Optional.empty();
    }
}
