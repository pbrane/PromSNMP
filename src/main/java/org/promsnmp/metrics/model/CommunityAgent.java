package org.promsnmp.metrics.model;

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

    @Override
    public Agent copy() {
        CommunityAgent copy = new CommunityAgent();
        copy.setEndpoint(new AgentEndpoint(this.getEndpoint()));
        copy.setVersion(this.getVersion());
        copy.setTimeout(this.getTimeout());
        copy.setRetries(this.getRetries());
        copy.setReadCommunity(this.getReadCommunity());
        copy.setWriteCommunity(this.getWriteCommunity());
        return copy;
    }

}
