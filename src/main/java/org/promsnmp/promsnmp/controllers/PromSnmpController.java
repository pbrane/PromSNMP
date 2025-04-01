package org.promsnmp.promsnmp.controllers;

import org.promsnmp.promsnmp.services.PromSnmpService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/promSnmp")
public class PromSnmpController {

    private final PromSnmpService promSnmpService;
    private final CacheManager cacheManager;

    public PromSnmpController(@Qualifier("configuredService") PromSnmpService promSnmpService, CacheManager cacheManager) {
        this.promSnmpService = promSnmpService;
        this.cacheManager = cacheManager;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello World");
    }

    @GetMapping("/metrics")
    public ResponseEntity<String> sampleData(
            @RequestParam(required = false)
            String instance,
            @RequestParam(required = false, defaultValue = "false")
            Boolean regex ) {

        return promSnmpService.getMetrics(instance, regex)
                .map(metrics -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                        .body(metrics))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error reading file"));
    }

    @GetMapping("/services")
    public ResponseEntity<String> sampleServices() {
        return promSnmpService.getServices()
                .map(services -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(services))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"File not found\"}"));
    }

    @GetMapping("/evictCache")
    public String evictAll() {
        Objects.requireNonNull(cacheManager.getCache("metrics")).clear();
        return "Cache cleared.";
    }
}
