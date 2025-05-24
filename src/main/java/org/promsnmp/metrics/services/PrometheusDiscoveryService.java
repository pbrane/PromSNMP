package org.promsnmp.metrics.services;

import java.util.Optional;

public interface PrometheusDiscoveryService {
    Optional<String> getTargets();
}
