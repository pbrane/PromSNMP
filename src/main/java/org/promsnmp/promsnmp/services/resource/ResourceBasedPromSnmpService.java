package org.promsnmp.promsnmp.services.resource;

import org.promsnmp.promsnmp.repositories.PromSnmpRepository;
import org.promsnmp.promsnmp.services.PromSnmpService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("ResSvc")
public class ResourceBasedPromSnmpService implements PromSnmpService {

    private final PromSnmpRepository repository;

    public ResourceBasedPromSnmpService(@Qualifier("configuredRepo") PromSnmpRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<String> getServices() {
        try {
            Resource resource = repository.readServices();
            if (!resource.exists()) return Optional.empty();

            return Optional.of(new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getMetrics(String instance, boolean regex) {
        Resource instanceResource = repository.readMetrics(instance);
        if (!instanceResource.exists()) {
            return Optional.of("Instance file not found: " + instance);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(instanceResource.getInputStream(), StandardCharsets.UTF_8))) {

            String metrics = reader.lines().collect(Collectors.joining("\n"));
            String formatted = formatMetrics(metrics);
            return Optional.of(filterByInstance(formatted, instance, regex));
        } catch (IOException e) {
            return Optional.of("Error reading instance metrics for " + instance);
        }
    }

    private String formatMetrics(String rawMetrics) {
        return Stream.of(rawMetrics.split("\n"))
                .map(line -> line.matches("# (HELP|TYPE).*") ? "\n" + line : line)
                .collect(Collectors.joining("\n"));
    }

    private String filterByInstance(String metrics, String instance, boolean regex) {
        if (instance == null || instance.isBlank()) {
            return metrics;
        }

        StringBuilder filtered = new StringBuilder();
        String[] lines = metrics.split("\n");

        Pattern pattern = null;
        if (regex) {
            try {
                pattern = Pattern.compile(instance);
            } catch (PatternSyntaxException e) {
                return "Invalid regex pattern: " + instance;
            }
        }

        for (String line : lines) {
            if (line.isBlank() || line.startsWith("# HELP") || line.startsWith("# TYPE")) {
                filtered.append(line).append("\n");
            } else {
                boolean match = regex
                        ? pattern.matcher(line).find()
                        : line.contains("instance=\"" + instance + "\"");

                if (match) {
                    filtered.append(line).append("\n");
                }
            }
        }

        return filtered.toString();
    }
}
