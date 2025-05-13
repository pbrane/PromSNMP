package org.promsnmp.promsnmp.services;

import java.util.Optional;

public interface PrometheusDiscoveryService {
    Optional<String> getTargets();
}
