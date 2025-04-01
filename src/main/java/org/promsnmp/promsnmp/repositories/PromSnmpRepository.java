package org.promsnmp.promsnmp.repositories;

import org.springframework.core.io.Resource;

public interface PromSnmpRepository {

    Resource readMetrics(String instance);
    Resource readServices();

}
