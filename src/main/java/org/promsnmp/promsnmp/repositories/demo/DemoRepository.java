package org.promsnmp.promsnmp.repositories.demo;

import org.promsnmp.promsnmp.repositories.PromSnmpRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class DemoRepository implements PromSnmpRepository {

    @Override
    public Resource getMetricsResource() {
        return new ClassPathResource("static/prometheus-snmp-export.dat");
    }

    @Override
    public Resource getServicesResource() {
        return new ClassPathResource("static/prometheus-snmp-services.json");
    }
}
