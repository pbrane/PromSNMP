package org.promsnmp.metrics.repositories.jpa;

import org.promsnmp.metrics.model.Agent;
import org.promsnmp.metrics.model.AgentEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AgentRepository extends JpaRepository<Agent, UUID> {
    Optional<Agent> findByEndpoint(AgentEndpoint endpoint);
}
