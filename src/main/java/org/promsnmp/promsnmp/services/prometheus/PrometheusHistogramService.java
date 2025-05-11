package org.promsnmp.promsnmp.services.prometheus;

import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Unit;
import org.promsnmp.promsnmp.model.InterfaceInfo;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

import static io.micrometer.core.instrument.Metrics.globalRegistry;

@Service
public class PrometheusHistogramService {

    private final Histogram nativeHistogram;

    private final Map<String, Snapshot> previousSnapshots = new ConcurrentHashMap<>();

    private static final int[] CLASSIC_BUCKETS = {1, 5, 10, 25, 50, 75, 90, 95, 100};

    public PrometheusHistogramService() {
        nativeHistogram = Histogram.builder()
                .name("interface_utilization_native")
                .help("Interface bandwidth utilization as a percentage (native + classic)")
                .unit(Unit.RATIO) // Represents 0–100%
                .labelNames("instance", "ifName", "ifDescr", "ifIndex", "ifAlias")
                .register(PrometheusRegistry.defaultRegistry);
    }

    /**
     * Emits legacy-style histogram text and also records native histogram metric.
     */
    public Optional<String> renderUtilizationHistogram(
            String instance,
            InterfaceInfo iface,
            long currentTotalBytes,
            long speedBps) {

        if (speedBps <= 0) return Optional.empty();

        String key = instance + ":" + iface.ifName();
        long now = System.currentTimeMillis();
        Snapshot prev = previousSnapshots.get(key);
        previousSnapshots.put(key, new Snapshot(currentTotalBytes, now));

        if (prev == null) return Optional.empty();

        long deltaBytes = currentTotalBytes - prev.totalBytes;
        long deltaTimeMillis = now - prev.timestampMillis;
        if (deltaBytes < 0 || deltaTimeMillis <= 0) return Optional.empty();

        double bps = (deltaBytes * 8.0) / (deltaTimeMillis / 1000.0);
        double utilization = (bps / speedBps) * 100.0;

        // Record native histogram
        nativeHistogram.labelValues(
                instance,
                iface.ifName(),
                iface.ifDescr(),
                String.valueOf(iface.ifIndex()),
                iface.ifAlias()
        ).observe(utilization);

        // Render classic histogram lines manually
        String labelBlock = String.format(
                "instance=\"%s\",ifName=\"%s\",ifDescr=\"%s\",ifIndex=\"%d\",ifAlias=\"%s\"",
                instance,
                iface.ifName(),
                iface.ifDescr(),
                iface.ifIndex(),
                iface.ifAlias()
        );

        StringBuilder sb = new StringBuilder();

        for (int b : CLASSIC_BUCKETS) {
            sb.append(String.format(
                    "interface_utilization_bucket{%s,le=\"%d\"} %d\n",
                    labelBlock,
                    b,
                    utilization <= b ? 1 : 0
            ));
        }

        sb.append(String.format("interface_utilization_count{%s} 1\n", labelBlock));
        sb.append(String.format("interface_utilization_sum{%s} %.2f\n", labelBlock, utilization));

        return Optional.of(sb.toString());
    }

    private record Snapshot(long totalBytes, long timestampMillis) {}
}
