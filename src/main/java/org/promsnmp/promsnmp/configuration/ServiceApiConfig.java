package org.promsnmp.promsnmp.configuration;

import org.promsnmp.promsnmp.services.PromSnmpService;
import org.promsnmp.promsnmp.services.demo.PromSnmpServiceDemo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceApiConfig {

    @Value("${SERVICE_API:demo}")
    private String apiSvcMode;

    @Bean
    public PromSnmpService promSnmpService(PromSnmpServiceDemo demoService) {
        if ("demo".equalsIgnoreCase(apiSvcMode)) {
            return demoService;
        } else {
            throw new IllegalStateException("No configured PromSnmpService.");
        }
    }

}
