package org.promsnmp.metrics.inventory;

import org.promsnmp.common.model.Agent;

import java.util.List;

public interface InventoryPublisher {
    void publish(List<? extends Agent> agents);
}