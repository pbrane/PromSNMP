package org.promsnmp.promsnmp.utils;

import org.promsnmp.promsnmp.snmp.AuthProtocolType;
import org.promsnmp.promsnmp.snmp.PrivProtocolType;

import java.util.Arrays;
import java.util.List;

public class ProtocolOptions {

    public static List<String> getSupportedAuthProtocols() {
        return Arrays.stream(AuthProtocolType.values())
                .map(Enum::name)
                .toList();
    }

    public static List<String> getSupportedPrivProtocols() {
        return Arrays.stream(PrivProtocolType.values())
                .map(Enum::name)
                .toList();
    }
}
