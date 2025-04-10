package org.promsnmp.promsnmp.repositories;

import java.util.Optional;

public interface PrometheusDiscoveryRepository {

    Optional<String> readServices();

}
