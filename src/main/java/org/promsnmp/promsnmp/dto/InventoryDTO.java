package org.promsnmp.promsnmp.dto;

import lombok.Data;
import org.promsnmp.promsnmp.model.NetworkDevice;

import java.util.List;

@Data
public class InventoryDTO {
    private List<NetworkDevice> devices;
}
