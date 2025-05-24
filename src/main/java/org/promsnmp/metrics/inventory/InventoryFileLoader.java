
package org.promsnmp.metrics.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.promsnmp.metrics.dto.InventoryDTO;
import org.promsnmp.metrics.services.InventoryService;
import org.promsnmp.metrics.utils.EncryptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class InventoryFileLoader implements InventoryBackupManager {

    @Value("${PROM_INV_FILE:/app/data/metrics-inventory.json}")
    private String inventoryFile;

    private final ObjectMapper objectMapper;
    private final InventoryService inventoryService;

    public InventoryFileLoader(ObjectMapper objectMapper, InventoryService inventoryService) {
        this.objectMapper = objectMapper;
        this.inventoryService = inventoryService;
    }

    @Override
    public void backup() {
        Path inventoryFilePath = Paths.get(inventoryFile);
        log.info("Backing up inventory to {}", inventoryFilePath);

        try {
            InventoryDTO inventory = inventoryService.exportInventory();
            Files.createDirectories(inventoryFilePath.getParent());
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(inventory);

            //if (!ENCRYPTION_KEY.equals(DEFAULT_SYMMETRIC_KEY) && ENCRYPTION_KEY.length() == 16) {
            if (ENCRYPTION_KEY.length() == 16) {
                String encrypted = EncryptionUtils.encrypt(json, ENCRYPTION_KEY);
                Files.writeString(inventoryFilePath, encrypted);
                log.info("Persisted encrypted inventory to {}", inventoryFilePath);
            } else {
                Files.writeString(inventoryFilePath, json);
                log.warn("Persisting inventory without encryption to {}", inventoryFilePath);
            }

        } catch (Exception e) {
            log.error("Failed to persist inventory to file", e);
        }
    }

    @Override
    @EventListener(ApplicationReadyEvent.class)
    public void restore() {
        Path inventoryFilePath = Paths.get(inventoryFile);
        log.info("Restoring inventory from {}", inventoryFilePath);

        if (Files.exists(inventoryFilePath)) {
            try {
                String content = Files.readString(inventoryFilePath);
                String decryptedJson = (ENCRYPTION_KEY.length() == 16)
                        ? EncryptionUtils.decrypt(content, ENCRYPTION_KEY)
                        : content;

                InventoryDTO inventory = objectMapper.readValue(decryptedJson, InventoryDTO.class);
                inventoryService.importInventory(inventory);
                log.info("Loaded inventory from {}", inventoryFilePath);

            } catch (Exception e) {
                log.error("Failed to load or decrypt inventory from file", e);
            }
        } else {
            log.info("No existing inventory file found at startup: {}", inventoryFilePath);
        }
    }
}
