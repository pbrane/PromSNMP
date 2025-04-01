package org.promsnmp.promsnmp.repositories;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;

public interface PromSnmpRepository {

    public Resource getMetricsResource();
    public Resource getServicesResource();

}
