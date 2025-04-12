package org.promsnmp.promsnmp.repositories.jpa;

import org.promsnmp.promsnmp.model.CommunityAgent;
import org.promsnmp.promsnmp.model.AgentEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommunityAgentRepository extends JpaRepository<CommunityAgent, UUID> {
    Optional<CommunityAgent> findByEndpoint(AgentEndpoint endpoint);
}
