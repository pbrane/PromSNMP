package org.promsnmp.metrics.repositories.jpa;

import org.promsnmp.metrics.model.NetworkDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NetworkDeviceRepository extends JpaRepository<NetworkDevice, UUID> {
    Optional<NetworkDevice> findBySysName(String sysName);

    @Query("SELECT d FROM NetworkDevice d LEFT JOIN FETCH d.agents")
    List<NetworkDevice> findAllWithAgents();

    @Query("SELECT d FROM NetworkDevice d LEFT JOIN FETCH d.agents WHERE d.sysName = :sysName")
    Optional<NetworkDevice> findBySysNameWithAgents(@Param("sysName") String sysName);

}
