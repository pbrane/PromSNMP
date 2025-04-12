package org.promsnmp.promsnmp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "community_agents")
public class CommunityAgent extends Agent {

    private String readCommunity;
    private String writeCommunity;

    @Override
    public String getType() {
        return "snmp-community";
    }
}
