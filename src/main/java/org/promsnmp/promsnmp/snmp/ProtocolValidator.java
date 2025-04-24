package org.promsnmp.promsnmp.snmp;

public class ProtocolValidator {

    public static AuthProtocolType validateAuthProtocol(String protocol) {
        try {
            return AuthProtocolType.valueOf(protocol.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Unsupported authentication protocol: " + protocol);
        }
    }

    public static PrivProtocolType validatePrivProtocol(String protocol) {
        try {
            return PrivProtocolType.valueOf(protocol.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Unsupported privacy protocol: " + protocol);
        }
    }
}
