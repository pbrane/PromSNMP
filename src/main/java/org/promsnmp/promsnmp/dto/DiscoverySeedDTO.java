package org.promsnmp.promsnmp.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DiscoverySeedDTO {
    private UUID id;
    private List<String> potentialTargets;
    private int port;
    private String version; // "v2c" or "v3"
    private String agentType;

    private String readCommunity;

    private String securityName;
    private int securityLevel;
    private String authProtocol;
    private String authPassphrase;
    private String privProtocol;
    private String privPassphrase;
}
