package org.promsnmp.metrics.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.net.InetAddress;

@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class AgentEndpoint {
    @Column(nullable = false)
    private InetAddress address;

    @Column(nullable = false)
    private int port;

    public AgentEndpoint(AgentEndpoint other) {
        this.address = other.address;
        this.port = other.port;
    }

    @Override
    public String toString() {
        return "Agent address: "+address+", port: "+port;
    }
}
