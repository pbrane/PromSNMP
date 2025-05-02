package org.promsnmp.promsnmp.repositories.jpa;

import org.promsnmp.promsnmp.model.DiscoverySeed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DiscoverySeedRepository extends JpaRepository<DiscoverySeed, UUID> {
}
