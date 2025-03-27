package org.promsnmp.promsnmp.commands;

import org.promsnmp.promsnmp.service.PromSnmpService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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

    @ShellMethod(key = "sample", value = "Displays sample metric data, optionally filtered by instance name.")
    public String sampleData(
            @ShellOption(defaultValue = "false", help = "Treat the instance filter as a regular expression.") boolean regex,
            @ShellOption(defaultValue = ShellOption.NULL, help = "Optional instance name to filter (e.g., router-1.example.com)") String instance
    ) {
        return promSnmpService.readMetricsFile()
                .map(promSnmpService::formatMetrics)
                .map(metrics -> filterByInstance(metrics, instance, regex))
                .orElse("Error reading file");
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


    @ShellMethod(key = "services", value = "Displays services from prometheus-snmp-services.json")
    public String sampleServices() {
        return promSnmpService.readServicesFile()
                .orElse("{\"error\": \"File not found\"}");
    }
}
