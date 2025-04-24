package org.promsnmp.promsnmp.services.prometheus;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PrometheusHistogramService {

    private final Map<String, Snapshot> previousSnapshots = new ConcurrentHashMap<>();

    public Optional<String> renderUtilizationHistogram(String instance, String interfaceName, long currentTotalBytes, long speedBps) {
        if (speedBps <= 0) return Optional.empty();

        String key = instance + ":" + interfaceName;
        Instant now = Instant.now();
        Snapshot prev = previousSnapshots.get(key);
        previousSnapshots.put(key, new Snapshot(currentTotalBytes, now));

        if (prev == null) return Optional.empty();

        long deltaBytes = currentTotalBytes - prev.totalBytes;
        long deltaTimeMillis = Duration.between(prev.timestamp, now).toMillis();
        if (deltaBytes < 0 || deltaTimeMillis <= 0) return Optional.empty();

        double bps = (deltaBytes * 8.0) / (deltaTimeMillis / 1000.0);
        double utilization = (bps / speedBps) * 100.0;

        int[] buckets = {1, 5, 10, 25, 50, 75, 90, 95, 100};
        StringBuilder sb = new StringBuilder();
        for (int b : buckets) {
            sb.append(String.format(
                    "interface_utilization_bucket{instance=\"%s\",if=\"%s\",le=\"%d\"} %d\n",
                    instance, interfaceName, b, utilization <= b ? 1 : 0
            ));
        }

        sb.append(String.format("interface_utilization_count{instance=\"%s\",if=\"%s\"} 1\n", instance, interfaceName));
        sb.append(String.format("interface_utilization_sum{instance=\"%s\",if=\"%s\"} %.2f\n", instance, interfaceName, utilization));

        return Optional.of(sb.toString());
    }

    private record Snapshot(long totalBytes, Instant timestamp) {}
}
