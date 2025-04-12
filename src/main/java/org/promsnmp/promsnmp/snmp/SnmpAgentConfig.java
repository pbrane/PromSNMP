package org.promsnmp.promsnmp.snmp;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.InetAddress;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SnmpAgentConfig {
    private InetAddress address;
    private int port;
    private int timeout;
    private int retries;
    private int retriesDelay;
    private byte[] readCommunity;
}
