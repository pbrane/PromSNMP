package org.promsnmp.promsnmp.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class NetworkDevice {

    @Id
    @GeneratedValue
    private UUID id;

    private String sysName;
    private String sysDescr;
    private String sysContact;
    private String sysLocation;

    @Column(nullable = false)
    private Instant discoveredAt = Instant.now();

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Agent> agents = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "primary_agent_id")
    private Agent primaryAgent;

    public void addAgent(Agent agent) {
        agents.add(agent);
        agent.setDevice(this);
        if (primaryAgent == null) {
            primaryAgent = agent;
        }
    }

    public void removeAgent(Agent agent) {
        agents.remove(agent);
        agent.setDevice(null);
        if (primaryAgent != null && primaryAgent.equals(agent)) {
            primaryAgent = null;
        }
    }

    public Agent resolvePrimaryAgent() {
        return (primaryAgent != null) ? primaryAgent :
                (!agents.isEmpty() ? agents.getFirst() : null);
    }
}
