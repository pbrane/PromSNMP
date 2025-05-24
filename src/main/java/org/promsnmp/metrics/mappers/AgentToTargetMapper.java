package org.promsnmp.metrics.mappers;

import org.promsnmp.metrics.model.Agent;
import org.promsnmp.metrics.model.AgentEndpoint;
import org.promsnmp.metrics.model.CommunityAgent;
import org.promsnmp.metrics.model.UserAgent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.Target;
import org.snmp4j.UserTarget;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.springframework.stereotype.Component;

@Component
public class AgentToTargetMapper {

    public Target<UdpAddress> mapToTarget(Agent agent) {
        if (agent instanceof CommunityAgent ca) {
            return mapCommunityTarget(ca);
        } else if (agent instanceof UserAgent ua) {
            return mapUserTarget(ua);
        } else {
            throw new IllegalArgumentException("Unsupported agent type: " + agent.getClass());
        }
    }

    private CommunityTarget<UdpAddress> mapCommunityTarget(CommunityAgent agent) {
        CommunityTarget<UdpAddress> target = new CommunityTarget<>();
        target.setCommunity(new OctetString(agent.getReadCommunity()));
        target.setVersion(agent.getVersion());
        target.setTimeout(agent.getTimeout());
        target.setRetries(agent.getRetries());
        target.setAddress(toUdpAddress(agent.getEndpoint()));
        return target;
    }

    private UserTarget<UdpAddress> mapUserTarget(UserAgent agent) {
        UserTarget<UdpAddress> target = new UserTarget<>();
        target.setSecurityName(new OctetString(agent.getSecurityName()));
        target.setSecurityLevel(agent.getSecurityLevel());
        target.setVersion(agent.getVersion());
        target.setTimeout(agent.getTimeout());
        target.setRetries(agent.getRetries());
        target.setAddress(toUdpAddress(agent.getEndpoint()));
        return target;
    }

    public Agent mapFromTarget(Target<UdpAddress> target) {
        if (target instanceof CommunityTarget<?> ct) {
            CommunityAgent agent = new CommunityAgent();
            agent.setReadCommunity(ct.getCommunity().toString());
            agent.setVersion(ct.getVersion());
            agent.setTimeout(ct.getTimeout());
            agent.setRetries(ct.getRetries());
            agent.setEndpoint(fromUdpAddress((UdpAddress) ct.getAddress())); //fixme: maybe need to handle this abstraction better
            return agent;
        } else if (target instanceof UserTarget<?> ut) {
            UserAgent agent = new UserAgent();
            agent.setSecurityName(ut.getSecurityName().toString());
            agent.setSecurityLevel(ut.getSecurityLevel());
            agent.setVersion(ut.getVersion());
            agent.setTimeout(ut.getTimeout());
            agent.setRetries(ut.getRetries());
            agent.setEndpoint(fromUdpAddress((UdpAddress) ut.getAddress())); //fixme: maybe need to handle this abstraction better
            return agent;
        } else {
            throw new IllegalArgumentException("Unsupported target type: " + target.getClass());
        }
    }

    private UdpAddress toUdpAddress(AgentEndpoint endpoint) {
        return new UdpAddress(endpoint.getAddress(), endpoint.getPort());
    }

    private AgentEndpoint fromUdpAddress(UdpAddress address) {
        return new AgentEndpoint(address.getInetAddress(), address.getPort());
    }
}
