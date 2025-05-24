package org.promsnmp.metrics.dto;

import lombok.Data;
import org.promsnmp.metrics.model.NetworkDevice;

import java.util.List;

@Data
public class InventoryDTO {
    private List<NetworkDevice> devices;
    private List<DiscoverySeedDTO> discoverySeeds;
}
