package org.promsnmp.promsnmp.repositories.demo;

import org.promsnmp.promsnmp.repositories.PromSnmpRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

@Repository("DemoRepo")
public class DemoRepository implements PromSnmpRepository {

    @Override
    public Resource readMetrics(String instance) {
        return new ClassPathResource("static/prometheus-snmp-export.dat");
    }

    @Override
    public Resource readServices() {
        return new ClassPathResource("static/prometheus-snmp-services.json");
    }
}
