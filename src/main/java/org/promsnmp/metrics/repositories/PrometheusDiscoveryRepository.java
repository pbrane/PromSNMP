package org.promsnmp.metrics.repositories;

import java.util.Optional;

public interface PrometheusDiscoveryRepository {

    Optional<String> readServices();

}
