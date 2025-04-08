package org.promsnmp.promsnmp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "agents", uniqueConstraints = @UniqueConstraint(columnNames = {"address", "port"}))
public abstract class Agent {

    @Id
    private UUID id = UUID.randomUUID();

    @Setter
    @Embedded
    private AgentEndpoint endpoint;

    @Setter
    @Column(nullable = false)
    private int version;

    @Setter
    private long timeout = 1500;
    @Setter
    private int retries = 1;

    @Setter
    private Instant discoveredAt;

    public abstract String getType();
}