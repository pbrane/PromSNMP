package org.promsnmp.metrics.repositories.prometheus;

import org.promsnmp.metrics.repositories.PrometheusDiscoveryRepository;
import org.promsnmp.metrics.repositories.PrometheusMetricsRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository("ClassPathRepo")
public class ClassPathResourcePrometheusRepository implements PrometheusMetricsRepository, PrometheusDiscoveryRepository {

    public Optional<String> readMetrics(String instance) {
        Resource metrics = new ClassPathResource("metrics/" + instance + ".prom");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(metrics.getInputStream(), StandardCharsets.UTF_8))) {

            return Optional.of(reader.lines().collect(Collectors.joining("\n")));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public Optional<String> readServices() {
        Resource services = new ClassPathResource("services/prometheus-snmp-services.json");
        if (!services.exists()) {
            return Optional.empty();
        }

        try (InputStream is = services.getInputStream()) {
            return Optional.of(new String(is.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            return Optional.empty();
        }

    }

}
