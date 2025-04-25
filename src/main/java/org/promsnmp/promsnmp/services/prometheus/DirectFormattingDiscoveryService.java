package org.promsnmp.promsnmp.services.prometheus;

import org.promsnmp.promsnmp.services.PrometheusDiscoveryService;
import org.promsnmp.promsnmp.services.PrometheusMetricsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("directService")
public class DirectFormattingDiscoveryService implements PrometheusDiscoveryService, PrometheusMetricsService {
    @Override
    public Optional<String> getServices() {
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
