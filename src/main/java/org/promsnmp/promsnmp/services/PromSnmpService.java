package org.promsnmp.promsnmp.services;

import java.util.Optional;

public interface PromSnmpService {
    Optional<String> readServices();

    Optional<String> readMetrics(String instance, boolean regex);
}
