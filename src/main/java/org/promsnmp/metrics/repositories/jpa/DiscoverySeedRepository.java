package org.promsnmp.metrics.repositories.jpa;

import org.promsnmp.common.model.DiscoverySeed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DiscoverySeedRepository extends JpaRepository<DiscoverySeed, UUID> {
}
