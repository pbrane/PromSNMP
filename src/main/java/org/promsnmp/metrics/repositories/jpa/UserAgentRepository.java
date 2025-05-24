package org.promsnmp.metrics.repositories.jpa;

import org.promsnmp.metrics.model.UserAgent;
import org.promsnmp.metrics.model.AgentEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAgentRepository extends JpaRepository<UserAgent, UUID> {
    Optional<UserAgent> findByEndpoint(AgentEndpoint endpoint);
}
