package org.promsnmp.metrics.inventory;

import org.springframework.stereotype.Component;

@Component
public interface InventoryBackupManager {

    String DEFAULT_SYMMETRIC_KEY = "0123456789ABCDEF";
    String ENCRYPTION_KEY = System.getenv().getOrDefault("PROM_ENCRYPT_KEY", DEFAULT_SYMMETRIC_KEY);

    void backup();
    void restore();
}
