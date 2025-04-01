package org.promsnmp.promsnmp.repositories;

import org.springframework.core.io.Resource;

public interface PromSnmpRepository {

    public Resource readMetrics(String instance);
    public Resource readServices();

}
