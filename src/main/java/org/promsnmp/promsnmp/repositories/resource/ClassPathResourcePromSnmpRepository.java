package org.promsnmp.promsnmp.repositories.resource;

import org.promsnmp.promsnmp.repositories.PromSnmpRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

@Repository("ClassPathRepo")
public class ClassPathResourcePromSnmpRepository implements PromSnmpRepository {

    @Override
    public Resource readMetrics(String instance) {
        // default file (could be null)
        return new ClassPathResource("metrics/" + instance + ".prom");
    }

    @Override
    public Resource readServices() {
        return new ClassPathResource("services/prometheus-snmp-services.json");
    }

}
