package org.promsnmp.promsnmp.services;

import java.util.Optional;

public interface PromSnmpService {
    Optional<String> getServices();

    Optional<String> getMetrics(String instance, boolean regex);
}
