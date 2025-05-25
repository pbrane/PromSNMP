package org.promsnmp.metrics.snmp;

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
