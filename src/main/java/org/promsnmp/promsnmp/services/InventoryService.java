package org.promsnmp.promsnmp.services;

import org.promsnmp.promsnmp.dto.InventoryDTO;

public interface InventoryService {
    void importInventory(InventoryDTO dto);
    InventoryDTO exportInventory();
}
