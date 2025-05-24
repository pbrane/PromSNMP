package org.promsnmp.metrics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class DiscoveryRequestDTO {

    @Schema(description = "Target hostnames or IP addresses (IPv4, IPv6, or DNS names)", example = "[\"192.168.1.1\", \"fe80::1\", \"my-device.local\"]", required = true)
    @NotEmpty(message = "At least one target is required")
    private List<String> potentialTargets;

    @Schema(description = "SNMP agent type", example = "snmp-community", allowableValues = {"snmp-community", "snmp-user"}, required = true)
    @NotBlank
    private String agentType;

    @Schema(description = "SNMP version to use", example = "v2c", allowableValues = {"v2c", "v3"}, required = true)
    @Pattern(regexp = "v2c|v3", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String version;

    @Schema(description = "SNMP port", example = "161", defaultValue = "161")
    @Min(1)
    @Max(65535)
    private int port;

    // SNMPv2c fields
    @Schema(description = "Read community string for SNMPv2c", example = "public")
    private String readCommunity;

    // SNMPv3 fields
    @Schema(description = "Security name for SNMPv3", example = "monitor")
    private String securityName;

    @Schema(description = "Security level for SNMPv3", example = "3")
    private Integer securityLevel;

    @Schema(description = "Authentication protocol for SNMPv3", example = "SHA")
    private String authProtocol;

    @Schema(description = "Authentication passphrase for SNMPv3", example = "secureAuth")
    private String authPassphrase;

    @Schema(description = "Privacy protocol for SNMPv3", example = "AES")
    private String privProtocol;

    @Schema(description = "Privacy passphrase for SNMPv3", example = "securePriv")
    private String privPassphrase;
}
