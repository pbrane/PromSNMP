package org.promsnmp.promsnmp.controllers;

import org.promsnmp.promsnmp.services.PromSnmpService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/promSnmp")
public class DemoController {

    private final PromSnmpService promSnmpService;

    public DemoController(PromSnmpService promSnmpService) {
        this.promSnmpService = promSnmpService;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello World");
    }

    @GetMapping("/sample")
    public ResponseEntity<String> sampleData(
            @RequestParam(required = false)
            String instance,
            @RequestParam(required = false, defaultValue = "false")
            Boolean regex ) {

        return promSnmpService.readMetrics(instance, regex)
                .map(metrics -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                        .body(metrics))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error reading file"));
    }

    @GetMapping("/services")
    public ResponseEntity<String> sampleServices() {
        return promSnmpService.readServices()
                .map(services -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(services))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"File not found\"}"));
    }
}
