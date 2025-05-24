package org.promsnmp.metrics.repositories.jpa;

import org.promsnmp.metrics.model.CommunityAgent;
import org.promsnmp.metrics.model.AgentEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommunityAgentRepository extends JpaRepository<CommunityAgent, UUID> {
    Optional<CommunityAgent> findByEndpoint(AgentEndpoint endpoint);
}
