package org.promsnmp.promsnmp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "user_agents")
public class UserAgent extends Agent {

    private String securityName;
    private int securityLevel;
    private String authProtocol;
    private String authPassphrase;
    private String privProtocol;
    private String privPassphrase;
    private String engineId;

    @Override
    public String getType() {
        return "snmp-user";
    }

    @Override
    public Agent copy() {
        UserAgent copy = new UserAgent();
        copy.setEndpoint(new AgentEndpoint(this.getEndpoint()));
        copy.setVersion(this.getVersion());
        copy.setTimeout(this.getTimeout());
        copy.setRetries(this.getRetries());
        copy.setSecurityName(this.getSecurityName());
        copy.setSecurityLevel(this.getSecurityLevel());
        copy.setAuthProtocol(this.getAuthProtocol());
        copy.setAuthPassphrase(this.getAuthPassphrase());
        copy.setPrivProtocol(this.getPrivProtocol());
        copy.setPrivPassphrase(this.getPrivPassphrase());
        return copy;
    }

}
