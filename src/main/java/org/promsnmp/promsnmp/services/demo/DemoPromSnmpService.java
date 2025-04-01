package org.promsnmp.promsnmp.services.demo;

import org.promsnmp.promsnmp.repositories.PromSnmpRepository;
import org.promsnmp.promsnmp.services.PromSnmpService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("DemoSvc")
public class DemoPromSnmpService implements PromSnmpService {

    private final PromSnmpRepository demoRepository;

    public DemoPromSnmpService(@Qualifier("configuredRepo") PromSnmpRepository repository) {
        this.demoRepository = repository;
    }

    @Override
    public Optional<String> getServices() {
        Resource demoResource = demoRepository.readServices();

        if (demoResource == null || !demoResource.exists()) {
            return Optional.empty();
        }

        try (InputStream is = demoResource.getInputStream()) {
            return Optional.of(new String(is.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            return Optional.empty();
        }
    }


    @Override
    public Optional<String> getMetrics(String instance, boolean regex) {
        return Optional.of(deriveMetrics()
                .map(this::formatMetrics)
                .map(metrics -> filterByInstance(metrics, instance, regex))
                .orElse("Error reading metrics"));
    }

    private Optional<String> deriveMetrics() {
        Resource demoResource = demoRepository.readMetrics(null);

        if (demoResource == null) {
            return Optional.empty();
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(demoResource.getInputStream(), StandardCharsets.UTF_8))) {

            return Optional.of(reader.lines().collect(Collectors.joining("\n")));
        } catch (IOException e) {
            return Optional.empty();
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
