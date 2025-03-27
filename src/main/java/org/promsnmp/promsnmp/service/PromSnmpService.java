package org.promsnmp.promsnmp.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
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
}
