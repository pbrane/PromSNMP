package org.promsnmp.promsnmp.commands;

import org.promsnmp.promsnmp.services.sample.PromSnmpServiceDemo;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Optional;

@ShellComponent
public class PromSnmpCommands {

    private final PromSnmpServiceDemo promSnmpService;

    public PromSnmpCommands(PromSnmpServiceDemo promSnmpService) {
        this.promSnmpService = promSnmpService;
    }

    @ShellMethod(key = "hello", value = "Returns a greeting message.")
    public String hello() {
        return "Hello World";
    }

    @ShellMethod(key = "sample", value = "Displays sample metric data, optionally filtered by instance name.")
    public Optional<String> sampleData(
            @ShellOption(defaultValue = "false", help = "Treat the instance filter as a regular expression.") boolean regex,
            @ShellOption(defaultValue = ShellOption.NULL, help = "Optional instance name to filter (e.g., router-1.example.com)") String instance
    ) {
        return promSnmpService.readMetrics(instance, regex);
    }


    @ShellMethod(key = "services", value = "Displays services from prometheus-snmp-services.json")
    public String sampleServices() {
        return promSnmpService.readServices()
                .orElse("{\"error\": \"File not found\"}");
    }
}
