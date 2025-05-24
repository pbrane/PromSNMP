package org.promsnmp.metrics.configuration;

import org.promsnmp.metrics.repositories.PrometheusMetricsRepository;
import org.promsnmp.metrics.repositories.prometheus.DirectFormattingRepository;
import org.promsnmp.metrics.repositories.prometheus.SnmpMetricsRepository;
import org.promsnmp.metrics.services.PrometheusMetricsService;
import org.promsnmp.metrics.services.PrometheusDiscoveryService;
import org.promsnmp.metrics.services.prometheus.SnmpBasedMetricsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceApiConfig {

    @Value("${PROM_DISCOVERY_API:snmp}")
    private String discoverySvcMode;

    @Value(("${PROM_METRICS_API:snmp}"))
    private String metricsSvcMode;

    @Value("${METRICS_REPO_API:snmp}")
    private String repoMode;

    @Bean("prometheusMetricsService")
    public PrometheusMetricsService metricsService(@Qualifier("ResSvc") PrometheusMetricsService resourceService,
                                                   @Qualifier("directService") PrometheusMetricsService directService,
                                                   @Qualifier("SnmpSvc")SnmpBasedMetricsService snmpService) {

        return switch (metricsSvcMode.toLowerCase()) {
            case "demo" -> resourceService;
            case "direct" -> directService;
            case "snmp" -> snmpService;
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

    @Bean("configuredMetricsRepo")
    public PrometheusMetricsRepository metricsRepository(@Qualifier("ClassPathRepo") PrometheusMetricsRepository cpRepo,
                                                         @Qualifier("DirectRepo")DirectFormattingRepository directRepo,
                                                         @Qualifier("SnmpMetricsRepo" ) SnmpMetricsRepository snmpRepo) {

        return switch (repoMode.toLowerCase()) {
            case "demo" -> cpRepo;
            case "direct" -> directRepo;
            case "snmp" -> snmpRepo;
            default -> throw new IllegalStateException("Unknown REPO_API mode" + repoMode);
        };
    }
}
