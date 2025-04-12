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

    @Override
    public String getType() {
        return "snmp-user";
    }
}
