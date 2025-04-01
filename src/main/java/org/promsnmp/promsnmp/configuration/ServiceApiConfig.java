package org.promsnmp.promsnmp.configuration;

import org.promsnmp.promsnmp.repositories.PromSnmpRepository;
import org.promsnmp.promsnmp.repositories.demo.DemoRepository;
import org.promsnmp.promsnmp.repositories.resource.ClassPathResourcePromSnmpRepository;
import org.promsnmp.promsnmp.services.PromSnmpService;
import org.promsnmp.promsnmp.services.demo.PromSnmpServiceDemo;
import org.promsnmp.promsnmp.services.resource.ResourceBasedPromSnmpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceApiConfig {

    @Value("${SERVICE_API:demo}")
    private String apiSvcMode;

    @Value("${REPOSITORY_API:demo}")
    private String apiRepoMode;

    @Bean("configuredService")
    public PromSnmpService promSnmpService(PromSnmpServiceDemo demoService, ResourceBasedPromSnmpService resourceService) {
        return switch (apiSvcMode.toLowerCase()) {
            case "demo" -> demoService;
            case "resource" -> resourceService;
            default -> throw new IllegalStateException("Unknown SERVICE_API mode: " + apiSvcMode);
        };
    }

    @Bean("configuredRepo")
    public PromSnmpRepository promSnmpRepository(ClassPathResourcePromSnmpRepository classPathResourcePromSnmpRepository, DemoRepository demoRepository) {
        return switch (apiRepoMode.toLowerCase()) {
            case "classpath" -> classPathResourcePromSnmpRepository;
            case "demo" -> demoRepository;
            default -> throw new IllegalStateException("Unknown REPOSITORY_API mode");
        };
    }
}
