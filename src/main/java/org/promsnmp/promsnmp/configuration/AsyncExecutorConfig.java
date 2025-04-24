package org.promsnmp.promsnmp.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncExecutorConfig {

    @Bean(name = "snmpDiscoveryExecutor")
    public ThreadPoolTaskExecutor snmpDiscoveryExecutor(MeterRegistry registry) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("snmp-discovery-");
        executor.initialize();

        ExecutorServiceMetrics.monitor(registry, executor.getThreadPoolExecutor(), "snmp.discovery.executor");
        return executor;
    }

    @Bean(name = "snmpMetricsExecutor")
    public ThreadPoolTaskExecutor snmpMetricsExecutor(MeterRegistry registry) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("snmp-metrics-");
        executor.initialize();

        ExecutorServiceMetrics.monitor(registry, executor.getThreadPoolExecutor(), "snmp.metrics.executor");
        return executor;
    }

}
