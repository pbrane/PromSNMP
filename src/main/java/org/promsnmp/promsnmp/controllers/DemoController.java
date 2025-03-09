package org.promsnmp.promsnmp.controllers;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/promSnmp")
public class DemoController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello World");
    }

    @GetMapping("/sample")
    public ResponseEntity<String> sampleData() {
        return readMetricsFile()
                .map(this::formatMetrics)
                .map(metrics -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                        .body(metrics))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error reading file"));
    }

    private Optional<String> readMetricsFile() {
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

    private String formatMetrics(String rawMetrics) {
        return Stream.of(rawMetrics.split("\n"))
                .map(line -> line.matches("# (HELP|TYPE).*") ? "\n" + line : line) // Newline before HELP/TYPE lines
                .collect(Collectors.joining("\n"));
    }
}
