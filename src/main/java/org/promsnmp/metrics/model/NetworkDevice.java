package org.promsnmp.metrics.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public NetworkDevice cloneForImport() {
        NetworkDevice copy = new NetworkDevice();
        copy.setId(null); // Let JPA generate a new ID
        copy.setSysName(this.getSysName());
        copy.setSysDescr(this.getSysDescr());
        copy.setSysContact(this.getSysContact());
        copy.setSysLocation(this.getSysLocation());
        copy.setDiscoveredAt(Instant.now());

        // Clone agents with a reference to the new device
        List<Agent> clonedAgents = this.getAgents().stream()
                .map(original -> original.cloneForImport(copy))
                .toList();
        copy.setAgents(clonedAgents);

        // Re-link the primary agent if present
        if (this.getPrimaryAgent() != null) {
            Optional<Agent> matching = clonedAgents.stream()
                    .filter(a -> a.matches(this.getPrimaryAgent()))
                    .findFirst();

            matching.ifPresent(copy::setPrimaryAgent);
        }

        return copy;
    }
}
