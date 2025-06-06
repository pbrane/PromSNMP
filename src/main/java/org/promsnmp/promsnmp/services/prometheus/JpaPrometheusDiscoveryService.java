package org.promsnmp.promsnmp.services.prometheus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.promsnmp.promsnmp.model.NetworkDevice;
import org.promsnmp.promsnmp.repositories.jpa.NetworkDeviceRepository;
import org.promsnmp.promsnmp.services.PrometheusDiscoveryService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("jpaDiscoveryService")
public class JpaPrometheusDiscoveryService implements PrometheusDiscoveryService {

    private final NetworkDeviceRepository networkDeviceRepository;
    private final ObjectMapper objectMapper;

    public JpaPrometheusDiscoveryService(NetworkDeviceRepository networkDeviceRepository, ObjectMapper objectMapper) {
        this.networkDeviceRepository = networkDeviceRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<String> getTargets() {
        List<String> sysNames = networkDeviceRepository.findAll().stream()
                .map(NetworkDevice::getSysName)
                .filter(name -> name != null && !name.isBlank())
                .toList();

        Map<String, List<String>> targetsMap = Map.of("targets", sysNames);
        List<Map<String, List<String>>> discoveryList = Collections.singletonList(targetsMap);

        try {
            String json = objectMapper.writeValueAsString(discoveryList);
            return Optional.of(json);
        } catch (Exception e) {
            return Optional.empty(); // Log this if desired
        }
    }}
