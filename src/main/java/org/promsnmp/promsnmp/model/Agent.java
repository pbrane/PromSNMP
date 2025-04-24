package org.promsnmp.promsnmp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CommunityAgent.class, name = "snmp-community"),
        @JsonSubTypes.Type(value = UserAgent.class, name = "snmp-user")
})
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
    @JsonBackReference
    private NetworkDevice device;

    public abstract String getType();

}