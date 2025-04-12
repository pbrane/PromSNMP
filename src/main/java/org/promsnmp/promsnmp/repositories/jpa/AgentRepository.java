package org.promsnmp.promsnmp.repositories.jpa;

import org.promsnmp.promsnmp.model.Agent;
import org.promsnmp.promsnmp.model.AgentEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AgentRepository extends JpaRepository<Agent, UUID> {
    Optional<Agent> findByEndpoint(AgentEndpoint endpoint);
}
