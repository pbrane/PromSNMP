package org.promsnmp.metrics.snmp;

import org.promsnmp.common.model.UserAgent;
import org.snmp4j.Snmp;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;

import java.io.IOException;

public class Snmpv3Utils {

    public static OctetString resolveOrDiscoverEngineId(Snmp snmp, UserAgent userAgent) throws IOException {
        if (userAgent.getEngineId() != null) {
            return OctetString.fromHexString(userAgent.getEngineId());
        }

        Address address = new UdpAddress(userAgent.getEndpoint().getAddress(), userAgent.getEndpoint().getPort());
        OctetString discoveredEngineId = OctetString.fromByteArray(snmp.discoverAuthoritativeEngineID(address, 3000));

        if (discoveredEngineId != null) {
            userAgent.setEngineId(discoveredEngineId.toHexString());
            // Consider persisting this here with your repository if you're in a transactional context
        }

        return discoveredEngineId;
    }

    public static void registerUser(Snmp snmp, UserAgent userAgent) throws IOException {
        OctetString engineId = resolveOrDiscoverEngineId(snmp, userAgent);

        UsmUser user = new UsmUser(
                new OctetString(userAgent.getSecurityName()),
                AuthProtocolMapper.map(userAgent.getAuthProtocol()),
                userAgent.getAuthPassphrase() != null ? new OctetString(userAgent.getAuthPassphrase()) : null,
                PrivProtocolMapper.map(userAgent.getPrivProtocol()),
                userAgent.getPrivPassphrase() != null ? new OctetString(userAgent.getPrivPassphrase()) : null
        );

        snmp.getUSM().addUser(engineId, user);
    }
}
