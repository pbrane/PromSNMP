package org.promsnmp.metrics.repositories.jpa;

import org.promsnmp.metrics.model.DiscoverySeed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DiscoverySeedRepository extends JpaRepository<DiscoverySeed, UUID> {
}
