package org.promsnmp.promsnmp.model;

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
    private List<Agent> agents = new ArrayList<>();


    public void addAgent(Agent agent) {
        agents.add(agent);
        agent.setDevice(this);
    }

    public void removeAgent(Agent agent) {
        agents.remove(agent);
        agent.setDevice(null);
    }
}
