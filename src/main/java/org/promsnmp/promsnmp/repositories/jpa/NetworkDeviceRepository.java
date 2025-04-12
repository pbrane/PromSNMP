package org.promsnmp.promsnmp.repositories.jpa;

import org.promsnmp.promsnmp.model.NetworkDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NetworkDeviceRepository extends JpaRepository<NetworkDevice, UUID> {
    Optional<NetworkDevice> findBySysName(String sysName);
}
