package org.promsnmp.promsnmp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "agents", uniqueConstraints = @UniqueConstraint(columnNames = {"address", "port"}))
public abstract class Agent {

    @Id
    private UUID id = UUID.randomUUID();

    @Embedded
    private AgentEndpoint endpoint;

    @Column(nullable = false)
    private int version;

    private long timeout = 1500;

    private int retries = 1;

    private Instant discoveredAt;

    @ManyToOne
    @JoinColumn(name = "device_id")
    private NetworkDevice device;

    public abstract String getType();

}