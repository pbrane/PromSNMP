package org.promsnmp.metrics.services;

import org.promsnmp.metrics.dto.InventoryDTO;

public interface InventoryService {
    void importInventory(InventoryDTO dto);
    InventoryDTO exportInventory();
}
