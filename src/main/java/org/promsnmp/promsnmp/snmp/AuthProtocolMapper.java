package org.promsnmp.promsnmp.snmp;

import org.snmp4j.smi.OID;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;

public class AuthProtocolMapper {

    public static OID map(String protocolName) {
        if (protocolName == null) return null;

        return switch (protocolName.trim().toUpperCase()) {
            case "SHA" -> AuthSHA.ID;
            case "MD5" -> AuthMD5.ID;
            default -> throw new IllegalArgumentException("Unsupported auth protocol: " + protocolName);
        };
    }
}