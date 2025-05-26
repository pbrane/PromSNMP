package org.promsnmp.metrics.inventory;

import org.promsnmp.common.model.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LoggingInventoryPublisher implements InventoryPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingInventoryPublisher.class);

    @Override
    public void publish(List<? extends Agent> agents) {
        if (agents.isEmpty()) {
            log.info("No agents to publish.");
        } else {
            for (Agent agent : agents) {
                log.info("[Inventory] Discovered Agent: {} [{}]", agent.getEndpoint().getAddress(), agent.getType());
            }
        }
    }
}
