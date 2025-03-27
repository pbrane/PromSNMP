package org.promsnmp.promsnmp.commands;

import org.promsnmp.promsnmp.service.PromSnmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class PromSnmpCommands {

    private final PromSnmpService promSnmpService;

    public PromSnmpCommands(PromSnmpService promSnmpService) {
        this.promSnmpService = promSnmpService;
    }

    @ShellMethod(key = "hello", value = "Returns a greeting message.")
    public String hello() {
        return "Hello World";
    }

    @ShellMethod(key = "sample", value = "Displays sample metric data from prometheus-snmp-export.dat")
    public String sampleData() {
        return promSnmpService.readMetricsFile()
                .map(promSnmpService::formatMetrics)
                .orElse("Error reading file");
    }

    @ShellMethod(key = "services", value = "Displays services from prometheus-snmp-services.json")
    public String sampleServices() {
        return promSnmpService.readServicesFile()
                .orElse("{\"error\": \"File not found\"}");
    }
}
