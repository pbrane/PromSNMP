package org.promsnmp.promsnmp.services.cache;

import org.promsnmp.promsnmp.repositories.PromSnmpRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CachedMetricsService {

    private final PromSnmpRepository repository;

    public CachedMetricsService(@Qualifier("configuredRepo") PromSnmpRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "metrics", key = "#instance")
    public Optional<String> getRawMetrics(String instance) {
        if (instance == null || instance.isBlank()) {
            return Optional.empty();  // 🚫 don't cache null or blank instances
        }

        Resource instanceResource = repository.readMetrics(instance);
        if (!instanceResource.exists()) {
            return Optional.empty();
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(instanceResource.getInputStream(), StandardCharsets.UTF_8))) {
            return Optional.of(reader.lines().collect(Collectors.joining("\n")));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
