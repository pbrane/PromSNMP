package org.promsnmp.metrics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "discovery_seeds")
public class DiscoverySeed {

    @Id
    private UUID id = UUID.randomUUID();

    @ElementCollection
    @CollectionTable(name = "discovery_seed_targets", joinColumns = @JoinColumn(name = "seed_id"))
    @Column(name = "target")
    private List<String> potentialTargets;
    
    private int port;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false)
    private String agentType; // "snmp-community" or "snmp-user"

    // SNMPv2c
    private String readCommunity;

    // SNMPv3
    private String securityName;
    private int securityLevel;
    private String authProtocol;
    private String authPassphrase;
    private String privProtocol;
    private String privPassphrase;
}
