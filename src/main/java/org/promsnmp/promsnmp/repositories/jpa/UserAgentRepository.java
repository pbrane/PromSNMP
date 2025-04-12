package org.promsnmp.promsnmp.repositories.jpa;

import org.promsnmp.promsnmp.model.UserAgent;
import org.promsnmp.promsnmp.model.AgentEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAgentRepository extends JpaRepository<UserAgent, UUID> {
    Optional<UserAgent> findByEndpoint(AgentEndpoint endpoint);
}
