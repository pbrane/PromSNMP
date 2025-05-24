package org.promsnmp.metrics.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.promsnmp.metrics.dto.DiscoveryRequestDTO;
import org.promsnmp.metrics.dto.DiscoverySeedDTO;
import org.promsnmp.metrics.model.DiscoverySeed;
import org.promsnmp.metrics.repositories.jpa.DiscoverySeedRepository;
import org.promsnmp.metrics.utils.Snmp4jUtils;
import org.springframework.stereotype.Service;
import org.promsnmp.metrics.utils.IpUtils;
import static org.promsnmp.metrics.utils.Snmp4jUtils.resolveSnmpVersion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscoverySeedService {

    private final DiscoverySeedRepository seedRepo;

    public void persistInventorySeed(DiscoverySeed seed) {
        if (seed.getPotentialTargets() == null || seed.getPotentialTargets().isEmpty()) {
            throw new IllegalArgumentException("Seed must include at least one target.");
        }
        if (seed.getId() == null) {
            seed.setId(UUID.randomUUID());
        }

        seedRepo.save(seed);
        log.info("Persisted imported discovery seed with ID: {}", seed.getId());
    }

    public void saveDiscoverySeed(DiscoveryRequestDTO dto) {
        UUID contextId = UUID.randomUUID();
        List<String> targets = IpUtils.resolveValidAddresses(dto.getPotentialTargets(), contextId);

        if (targets.isEmpty()) {
            throw new IllegalArgumentException("No valid IP addresses to save in discovery seed.");
        }

        DiscoverySeed seed = new DiscoverySeed();
        seed.setId(contextId);
        seed.setPotentialTargets(targets);
        seed.setPort(dto.getPort());
        seed.setVersion(resolveSnmpVersion(dto.getVersion())); // ✅
        seed.setAgentType(dto.getAgentType());

        if ("snmp-community".equals(dto.getAgentType())) {
            seed.setReadCommunity(dto.getReadCommunity());

        } else if ("snmp-user".equals(dto.getAgentType())) {
            seed.setSecurityName(dto.getSecurityName());
            seed.setSecurityLevel(dto.getSecurityLevel() != null ? dto.getSecurityLevel() : 0);
            seed.setAuthProtocol(dto.getAuthProtocol());
            seed.setAuthPassphrase(dto.getAuthPassphrase());
            seed.setPrivProtocol(dto.getPrivProtocol());
            seed.setPrivPassphrase(dto.getPrivPassphrase());

        } else {
            throw new IllegalArgumentException("Unsupported agent type: " + dto.getAgentType());
        }

        seedRepo.save(seed);
        log.info("Saved discovery seed with ID {} for agentType={} and IPs={}",
                seed.getId(), seed.getAgentType(), String.join(", ", targets));
    }

    public List<DiscoverySeed> findAllSeeds() {
        return seedRepo.findAll();
    }

    public void deleteAllSeeds() {
        seedRepo.deleteAll();
        log.info("Deleted all discovery seeds.");
    }

    public Optional<DiscoverySeed> findSeedById(UUID id) {
        return seedRepo.findById(id);
    }

    public boolean deleteSeedById(UUID id) {
        if (seedRepo.existsById(id)) {
            seedRepo.deleteById(id);
            log.info("Deleted discovery seed {}", id);
            return true;
        }
        return false;
    }

    public DiscoverySeedDTO toDto(DiscoverySeed seed) {
        DiscoverySeedDTO dto = new DiscoverySeedDTO();
        dto.setId(seed.getId());
        dto.setPotentialTargets(seed.getPotentialTargets());
        dto.setPort(seed.getPort());
        dto.setVersion(switch (seed.getVersion()) {
            case 1 -> "v2c";
            case 3 -> "v3";
            default -> "unknown";
        });
        dto.setAgentType(seed.getAgentType());

        dto.setReadCommunity(seed.getReadCommunity());
        dto.setSecurityName(seed.getSecurityName());
        dto.setSecurityLevel(seed.getSecurityLevel());
        dto.setAuthProtocol(seed.getAuthProtocol());
        dto.setAuthPassphrase(seed.getAuthPassphrase());
        dto.setPrivProtocol(seed.getPrivProtocol());
        dto.setPrivPassphrase(seed.getPrivPassphrase());

        return dto;
    }

    public List<DiscoverySeedDTO> toDtoList(List<DiscoverySeed> seeds) {
        return seeds.stream().map(this::toDto).toList();
    }

    //fixme: probably not needed but leaving it here for now
    public DiscoverySeed fromDto(DiscoverySeedDTO dto) {
        DiscoverySeed seed = new DiscoverySeed();

        seed.setId(dto.getId() != null ? dto.getId() : UUID.randomUUID());
        seed.setPotentialTargets(dto.getPotentialTargets());
        seed.setPort(dto.getPort());

        // Convert version string to SNMP4J-compatible int
        seed.setVersion(Snmp4jUtils.resolveSnmpVersion(dto.getVersion()));
        seed.setAgentType(dto.getAgentType());

        seed.setReadCommunity(dto.getReadCommunity());
        seed.setSecurityName(dto.getSecurityName());
        seed.setSecurityLevel(dto.getSecurityLevel());
        seed.setAuthProtocol(dto.getAuthProtocol());
        seed.setAuthPassphrase(dto.getAuthPassphrase());
        seed.setPrivProtocol(dto.getPrivProtocol());
        seed.setPrivPassphrase(dto.getPrivPassphrase());

        return seed;
    }

}
