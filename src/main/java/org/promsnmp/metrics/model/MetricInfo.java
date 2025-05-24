package org.promsnmp.metrics.model;

public record MetricInfo(String name, String oid, String help, String type, boolean walkable) {
}
