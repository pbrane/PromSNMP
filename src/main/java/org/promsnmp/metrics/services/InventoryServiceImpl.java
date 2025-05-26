package org.promsnmp.metrics.services;

import org.promsnmp.common.dto.DiscoverySeedDTO;
import org.promsnmp.common.dto.InventoryDTO;
import org.promsnmp.common.model.DiscoverySeed;
import org.promsnmp.common.model.NetworkDevice;
import org.promsnmp.metrics.inventory.InventoryBackupManager;
import org.promsnmp.metrics.repositories.jpa.NetworkDeviceRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final NetworkDeviceRepository networkDeviceRepository;
    private final DiscoverySeedService discoverySeedService;
    private final InventoryBackupManager inventoryBackupManager;


    public InventoryServiceImpl(NetworkDeviceRepository networkDeviceRepository,
                                DiscoverySeedService discoverySeedService,
                                @Lazy InventoryBackupManager inventoryBackupManager) {
        this.networkDeviceRepository = networkDeviceRepository;
        this.discoverySeedService = discoverySeedService;
        this.inventoryBackupManager = inventoryBackupManager;
    }

    @Override
    @Transactional
    public void importInventory(InventoryDTO dto) {
        networkDeviceRepository.deleteAll();
        discoverySeedService.deleteAllSeeds();

        List<NetworkDevice> importedDevices = dto.getDevices().stream()
                .map(NetworkDevice::cloneForImport)
                .toList();
        networkDeviceRepository.saveAll(importedDevices);

        if (dto.getDiscoverySeeds() != null) {
            for (DiscoverySeedDTO seedDTO : dto.getDiscoverySeeds()) {
                DiscoverySeed seed = discoverySeedService.fromDto(seedDTO);
                discoverySeedService.persistInventorySeed(seed);
            }
        }

        inventoryBackupManager.backup();
    }


    @Override
    public InventoryDTO exportInventory() {
        InventoryDTO dto = new InventoryDTO();
        dto.setDevices(networkDeviceRepository.findAll());
        dto.setDiscoverySeeds(
                discoverySeedService.toDtoList(discoverySeedService.findAllSeeds())
        );
        return dto;
    }
}
