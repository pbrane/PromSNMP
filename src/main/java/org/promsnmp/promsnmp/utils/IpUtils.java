package org.promsnmp.promsnmp.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
public class IpUtils {

    /**
     * Attempts to resolve a list of IP address strings into valid InetAddress objects.
     * Invalid or unresolvable addresses are skipped with a warning.
     *
     * @param potentialTargets List of IP address/Host strings (usually from client input)
     * @param contextId UUID to help correlate logs (e.g., discovery context)
     * @return List of successfully resolved InetAddress objects
     */
    public static List<InetAddress> toInetAddressList(List<String> potentialTargets, UUID contextId) {
        if (potentialTargets == null || potentialTargets.isEmpty()) return Collections.emptyList();

        List<InetAddress> results = new ArrayList<>();
        for (String ip : potentialTargets) {
            try {
                InetAddress address = InetAddress.getByName(ip);
                results.add(address);
            } catch (Exception ex) {
                log.warn("Context {} — Unable to resolve IP '{}': {}", contextId, ip, ex.getMessage());
            }
        }
        return results;
    }

    /**
     * Returns only valid, resolved IPs as strings.
     * This is typically used for validating and storing seeds.
     */
    public static List<String> resolveValidAddresses(List<String> potentialTargets, UUID contextId) {
        return toInetAddressList(potentialTargets, contextId).stream()
                .map(InetAddress::getHostAddress)
                .toList();
    }
}
