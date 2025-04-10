package org.promsnmp.promsnmp.services.prometheus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.promsnmp.promsnmp.model.AgentEndpoint;
import org.promsnmp.promsnmp.repositories.jpa.CommunityAgentRepository;
import org.promsnmp.promsnmp.repositories.jpa.UserAgentRepository;
import org.promsnmp.promsnmp.services.PrometheusDiscoveryService;
import org.promsnmp.promsnmp.model.NetworkDevice;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("jpaDiscoveryService")
public class JpaPrometheusDiscoveryService implements PrometheusDiscoveryService {

    private final CommunityAgentRepository communityAgentRepo;
    private final UserAgentRepository userAgentRepo;

    public JpaPrometheusDiscoveryService(
            CommunityAgentRepository communityRepo,
            UserAgentRepository userAgentRepo) {

        this.communityAgentRepo = communityRepo;
        this.userAgentRepo = userAgentRepo;
    }

    @Override
    public Optional<String> getServices() {
        List<Map<String, Object>> targetGroups = new ArrayList<>();

        communityAgentRepo.findAll().forEach(agent -> {
            Map<String, Object> group = new HashMap<>();
            group.put("targets", List.of(formatTarget(agent.getEndpoint())));
            group.put("labels", Map.of(
                    "job", "snmp",
                    "agent_type", "community",
                    "device_name", Optional.ofNullable(agent.getDevice())
                            .map(NetworkDevice::getSysName)
                            .orElse("unknown")
            ));
            targetGroups.add(group);
        });

        userAgentRepo.findAll().forEach(agent -> {
            Map<String, Object> group = new HashMap<>();
            group.put("targets", List.of(formatTarget(agent.getEndpoint())));
            group.put("labels", Map.of(
                    "job", "snmp",
                    "agent_type", "user",
                    "device_name", Optional.ofNullable(agent.getDevice())
                            .map(NetworkDevice::getSysName)
                            .orElse("unknown")
            ));
            targetGroups.add(group);
        });

        try {
            String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(targetGroups);
            return Optional.of(json);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String formatTarget(AgentEndpoint endpoint) {
        return endpoint.getAddress().getHostAddress() + ":" + endpoint.getPort();
    }
}
