package org.promsnmp.promsnmp.configuration;

import org.promsnmp.promsnmp.repositories.PromSnmpRepository;
import org.promsnmp.promsnmp.repositories.resource.DirectFormattingRepository;
import org.promsnmp.promsnmp.services.PromSnmpService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceApiConfig {

    @Value("${SVC_API:demo}")
    private String apiSvcMode;

    @Value("${REPO_API:demo}")
    private String apiRepoMode;

    @Bean("configuredService")
    public PromSnmpService promSnmpService(@Qualifier("DemoSvc") PromSnmpService demoService, @Qualifier("ResSvc") PromSnmpService resourceService) {
        return switch (apiSvcMode.toLowerCase()) {
            case "demo" -> demoService;
            case "resource" -> resourceService;
            default -> throw new IllegalStateException("Unknown SERVICE_API mode: " + apiSvcMode);
        };
    }

    @Bean("configuredRepo")
    public PromSnmpRepository promSnmpRepository(@Qualifier("DemoRepo") PromSnmpRepository demoRepository,
                                                 @Qualifier("ClassPathRepo") PromSnmpRepository cpRepo,
                                                 @Qualifier("DirectRepo")DirectFormattingRepository directRepo) {
        return switch (apiRepoMode.toLowerCase()) {
            case "demo" -> demoRepository;
            case "classpath" -> cpRepo;
            case "direct" -> directRepo;
            default -> throw new IllegalStateException("Unknown REPOSITORY_API mode");
        };
    }
}
