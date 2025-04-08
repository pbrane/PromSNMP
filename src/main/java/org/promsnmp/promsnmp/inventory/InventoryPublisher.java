package org.promsnmp.promsnmp.inventory;

import org.promsnmp.promsnmp.model.Agent;
import java.util.List;

public interface InventoryPublisher {
    void publish(List<? extends Agent> agents);
}