package org.promsnmp.metrics.snmp;

import org.snmp4j.smi.OID;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.PrivAES128;

public class PrivProtocolMapper {

    public static OID map(String protocolName) {
        if (protocolName == null) return null;

        return switch (protocolName.trim().toUpperCase()) {
            case "DES" -> PrivDES.ID;
            case "AES", "AES128" -> PrivAES128.ID;
            default -> throw new IllegalArgumentException("Unsupported priv protocol: " + protocolName);
        };
    }
}
