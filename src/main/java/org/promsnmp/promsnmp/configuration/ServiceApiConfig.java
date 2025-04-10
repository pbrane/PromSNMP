package org.promsnmp.promsnmp.configuration;

import org.promsnmp.promsnmp.repositories.PrometheusMetricsRepository;
import org.promsnmp.promsnmp.repositories.prometheus.DirectFormattingRepository;
import org.promsnmp.promsnmp.services.PrometheusMetricsService;
import org.promsnmp.promsnmp.services.PrometheusDiscoveryService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceApiConfig {

    @Value("${PROM_DISCOVERY_API:demo}")
    private String discoverySvcMode;

    @Value(("${PROM_METRICS_API:demo}"))
    private String metricsSvcMode;

    @Value("${REPO_API:demo}")
    private String repoMode;

    @Bean("prometheusMetricsService")
    public PrometheusMetricsService metricsService(@Qualifier("ResSvc") PrometheusMetricsService resourceService,
                                                   @Qualifier("directService") PrometheusMetricsService directRepo) {

        return switch (metricsSvcMode.toLowerCase()) {
            case "demo" -> resourceService;
            case "direct" -> directRepo;
            default -> throw new IllegalStateException("Unknown PROM_METRICS_API mode: " + metricsSvcMode);
        };
    }

    @Bean("prometheusDiscoveryService")
    public PrometheusDiscoveryService discoveryService(@Qualifier("ResSvc") PrometheusDiscoveryService resourceService,
                                                       @Qualifier("jpaDiscoveryService") PrometheusDiscoveryService jpaService) {

        return switch (discoverySvcMode.toLowerCase()) {
            case "demo" -> resourceService;
            case "snmp" -> jpaService;
            default -> throw new IllegalStateException("Unknown PROM_DISCOVERY_API mode: " + discoverySvcMode);
        };
    }

    @Bean("configuredRepo")
    public PrometheusMetricsRepository metricsRepository(@Qualifier("ClassPathRepo") PrometheusMetricsRepository cpRepo,
                                                         @Qualifier("jpaRepo") PrometheusMetricsRepository jpaRepo,
                                                         @Qualifier("DirectRepo")DirectFormattingRepository directRepo) {

        return switch (repoMode.toLowerCase()) {
            case "demo" -> cpRepo;
            case "direct" -> directRepo;
            case "jpa" -> jpaRepo;
            default -> throw new IllegalStateException("Unknown REPO_API mode");
        };
    }
}
