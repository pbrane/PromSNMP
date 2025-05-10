package org.promsnmp.promsnmp.model;

public record MetricInfo(String name, String oid, String help, String type, boolean walkable) {
}
