package org.promsnmp.promsnmp.services;

import org.promsnmp.promsnmp.dto.DiscoverySeedDTO;
import org.promsnmp.promsnmp.dto.InventoryDTO;
import org.promsnmp.promsnmp.model.DiscoverySeed;
import org.promsnmp.promsnmp.model.NetworkDevice;
import org.promsnmp.promsnmp.repositories.jpa.NetworkDeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final NetworkDeviceRepository networkDeviceRepository;
    private final DiscoverySeedService discoverySeedService;

    public InventoryServiceImpl(NetworkDeviceRepository networkDeviceRepository, DiscoverySeedService discoverySeedService) {
        this.networkDeviceRepository = networkDeviceRepository;
        this.discoverySeedService = discoverySeedService;
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
