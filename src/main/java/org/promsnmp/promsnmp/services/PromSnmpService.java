package org.promsnmp.promsnmp.services;

import java.util.Optional;

public interface PromSnmpService {
    Optional<String> readServices();

    Optional<String> readMetrics();

    Optional<String> getFilteredOutput(String instance, boolean regex);
}
