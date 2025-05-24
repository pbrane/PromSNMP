package org.promsnmp.metrics.services.prometheus;

import lombok.extern.slf4j.Slf4j;
import org.promsnmp.metrics.model.NetworkDevice;
import org.promsnmp.metrics.repositories.PrometheusMetricsRepository;
import org.promsnmp.metrics.repositories.jpa.NetworkDeviceRepository;
import org.promsnmp.metrics.services.cache.CachedMetricsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class SnmpMetricsScheduler {

    private final NetworkDeviceRepository deviceRepository;
    private final PrometheusMetricsRepository snmpMetricsRepository;

    private final ConcurrentHashMap<String, AtomicBoolean> collectionLocks = new ConcurrentHashMap<>();
    private final Executor snmpExecutor;

    private final CachedMetricsService cachedMetricsService;

    @Value("${COLLECTION_INTERVAL:300000}")
    private int collectionInterval;

    public SnmpMetricsScheduler(NetworkDeviceRepository deviceRepository,
                                @Qualifier("configuredMetricsRepo") PrometheusMetricsRepository snmpMetricsRepository,
                                @Qualifier("snmpDiscoveryExecutor") Executor snmpExecutor,
                                CachedMetricsService cachedMetricsService) {

        this.deviceRepository = deviceRepository;
        this.snmpMetricsRepository = snmpMetricsRepository;
        this.snmpExecutor = snmpExecutor;
        this.cachedMetricsService = cachedMetricsService;
    }

    @Scheduled(fixedRateString = "${collection.interval:30000}") // Every 30 seconds
    public void scheduledMetricCollection() {
        log.info("Starting scheduled SNMP metric collection...");

//        List<NetworkDevice> devices = deviceRepository.findAll();
        List<NetworkDevice> devices = deviceRepository.findAllWithAgents();


        devices.stream()
                .filter(device -> device.resolvePrimaryAgent() != null)
                .map(NetworkDevice::getSysName)
                .map(this::collectMetricsAsync)
                .forEach(future -> future.exceptionally(ex -> {
                    log.error("Exception during async metric collection", ex);
                    return null;
                }));
    }

    @Async("snmpMetricsExecutor") //Tying this to a thread pool configuration
    public CompletableFuture<Void> collectMetricsAsync(String instance) {
        AtomicBoolean lock = collectionLocks.computeIfAbsent(instance, k -> new AtomicBoolean(false));

        // Try to acquire lock
        if (!lock.compareAndSet(false, true)) {
            log.warn("Metric collection for instance '{}' is already in progress. Skipping this run.", instance);
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                log.debug("Collecting metrics for instance: {}", instance);
                //snmpMetricsRepository.readMetrics(instance);
                cachedMetricsService.refreshMetrics(instance);
            } catch (Exception ex) {
                log.error("Error during SNMP metric collection for instance: {}", instance, ex);
            } finally {
                lock.set(false); // Always release the lock
            }
        }, snmpExecutor);

    }
}
