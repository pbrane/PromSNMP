package org.promsnmp.metrics.repositories.prometheus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.promsnmp.common.model.AgentEndpoint;
import org.promsnmp.common.model.NetworkDevice;
import org.promsnmp.metrics.repositories.PrometheusDiscoveryRepository;
import org.promsnmp.metrics.repositories.jpa.CommunityAgentRepository;
import org.promsnmp.metrics.repositories.jpa.UserAgentRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository("jpaPrometheusRepository")
public class JpaPrometheusDiscoveryRepository implements PrometheusDiscoveryRepository {

    private final CommunityAgentRepository communityAgentRepo;
    private final UserAgentRepository userAgentRepo;

    public JpaPrometheusDiscoveryRepository(CommunityAgentRepository communityAgentRepo, UserAgentRepository userAgentRepo) {
        this.communityAgentRepo = communityAgentRepo;
        this.userAgentRepo = userAgentRepo;
    }

    @Override
    public Optional<String> readServices() {

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
