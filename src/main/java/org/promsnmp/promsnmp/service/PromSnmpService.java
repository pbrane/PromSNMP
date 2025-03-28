package org.promsnmp.promsnmp.service;

import org.springframework.core.io.ClassPathResource;
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

@Service
public class PromSnmpService {

    public Optional<String> readServicesFile() {
        try {
            ClassPathResource resource = new ClassPathResource("static/prometheus-snmp-services.json");
            return Optional.of(new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public Optional<String> readMetricsFile() {
        return Optional.of("static/prometheus-snmp-export.dat")
                .map(ClassPathResource::new)
                .flatMap(resource -> {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                        return Optional.of(reader.lines().collect(Collectors.joining("\n")));
                    } catch (Exception e) {
                        return Optional.empty();
                    }
                });
    }

    public String formatMetrics(String rawMetrics) {
        return Stream.of(rawMetrics.split("\n"))
                .map(line -> line.matches("# (HELP|TYPE).*") ? "\n" + line : line)
                .collect(Collectors.joining("\n"));
    }

    public Optional<String> getFilteredOutput(boolean regex, String instance) {
        return Optional.of(readMetricsFile()
                .map(this::formatMetrics)
                .map(metrics -> filterByInstance(metrics, instance, regex))
                .orElse("Error reading file"));
    }


    public String filterByInstance(String metrics, String instance, boolean regex) {
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
                return "Invalid regular expression: " + instance;
            }
        }

        for (String line : lines) {
            if (line.isBlank() || line.startsWith("# HELP") || line.startsWith("# TYPE")) {
                filtered.append(line).append("\n");
            } else {
                boolean instanceMatch = regex
                        ? pattern.matcher(line).find()
                        : line.contains("instance=\"" + instance + "\"");

                if (instanceMatch) {
                    filtered.append(line).append("\n");
                }
            }
        }

        return filtered.toString();
    }


}
