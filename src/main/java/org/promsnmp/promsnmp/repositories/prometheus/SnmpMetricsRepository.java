package org.promsnmp.promsnmp.repositories.prometheus;

import org.promsnmp.promsnmp.mappers.AgentToTargetMapper;
import org.promsnmp.promsnmp.model.Agent;
import org.promsnmp.promsnmp.model.AgentEndpoint;
import org.promsnmp.promsnmp.model.CommunityAgent;
import org.promsnmp.promsnmp.model.UserAgent;
import org.promsnmp.promsnmp.repositories.PrometheusMetricsRepository;
import org.promsnmp.promsnmp.repositories.jpa.NetworkDeviceRepository;
import org.promsnmp.promsnmp.services.prometheus.PrometheusHistogramService;
import org.promsnmp.promsnmp.snmp.AuthProtocolMapper;
import org.promsnmp.promsnmp.snmp.PrivProtocolMapper;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository("SnmpMetricsRepo")
public class SnmpMetricsRepository implements PrometheusMetricsRepository {

    public static final String           SYS_UPTIME = "1.3.6.1.2.1.1.3.0";
    public static final String               ifName = "1.3.6.1.2.1.31.1.1.1.1";
    public static final String         ifHCInOctets = "1.3.6.1.2.1.31.1.1.1.6";
    public static final String      ifHCInUcastPkts = "1.3.6.1.2.1.31.1.1.1.7";
    public static final String  ifHCInMulticastPkts = "1.3.6.1.2.1.31.1.1.1.8";
    public static final String  ifHCInBroadcastPkts = "1.3.6.1.2.1.31.1.1.1.9";
    public static final String        ifHCOutOctets = "1.3.6.1.2.1.31.1.1.1.10";
    public static final String     ifHCOutUcastPkts = "1.3.6.1.2.1.31.1.1.1.11";
    public static final String ifHCOutMulticastPkts = "1.3.6.1.2.1.31.1.1.1.12";
    public static final String ifHCOutBroadcastPkts = "1.3.6.1.2.1.31.1.1.1.13";
    public static final String          ifHighSpeed = "1.3.6.1.2.1.31.1.1.1.15";
    public static final String              ifSpeed = "1.3.6.1.2.1.2.2.1.5";

    private final NetworkDeviceRepository deviceRepository;
    private final AgentToTargetMapper agentToTargetMapper;

    private final PrometheusHistogramService histogramService;

    public SnmpMetricsRepository(NetworkDeviceRepository deviceRepository, AgentToTargetMapper agentToTargetMapper, PrometheusHistogramService histogramService) {
        this.deviceRepository = deviceRepository;
        this.agentToTargetMapper = agentToTargetMapper;
        this.histogramService = histogramService;
    }

    @Override
    public Optional<String> readMetrics(String instance) {
        return deviceRepository.findBySysNameWithAgents(instance)
                .flatMap(device -> {
                    Agent agent = device.resolvePrimaryAgent();
                    if (agent == null) {
                        // Consider logging the situation for observability
                        return Optional.empty();
                    }

                    try (Snmp snmp = new Snmp(new DefaultUdpTransportMapping())) {
                        snmp.listen();

                        Target<UdpAddress> target = agentToTargetMapper.mapToTarget(agent);

                        // For SNMPv3, ensure USM user is registered
                        if (target instanceof UserTarget<?> && agent instanceof UserAgent userAgent) {
                            UsmUser user = new UsmUser(
                                    new OctetString(userAgent.getSecurityName()),
                                    AuthProtocolMapper.map(userAgent.getAuthProtocol()),
                                    userAgent.getAuthPassphrase() != null ? new OctetString(userAgent.getAuthPassphrase()) : null,
                                    PrivProtocolMapper.map(userAgent.getPrivProtocol()),
                                    userAgent.getPrivPassphrase() != null ? new OctetString(userAgent.getPrivPassphrase()) : null
                            );

                            snmp.getUSM().addUser(user);
                        }

                        return Optional.of(walkMetrics(snmp, target, instance));
                    } catch (IOException e) {
                        // Consider logging the error
                        return Optional.empty();
                    }
                });
    }

    private String walkMetrics(Snmp snmp, Target<UdpAddress> target, String instance) throws IOException {
        StringBuilder sb = new StringBuilder();

        // --- System Uptime ---
        sb.append("# HELP sysUpTime system uptime in hundredths of a second\n");
        sb.append("# TYPE sysUpTime gauge\n");
        PDU sysPdu = new PDU();
        sysPdu.add(new VariableBinding(new OID(SnmpMetricsRepository.SYS_UPTIME)));
        sysPdu.setType(PDU.GET);

        ResponseEvent<UdpAddress> sysResp = snmp.get(sysPdu, target);
        if (sysResp.getResponse() != null && !sysResp.getResponse().getVariableBindings().isEmpty()) {
            Variable uptime = sysResp.getResponse().get(0).getVariable();
            sb.append(String.format("sysUpTime{instance=\"%s\"} %s\n", instance, uptime));
        }

        // --- Interface Metrics ---
        Map<Integer, String> ifNamesMap = walkStringColumn(snmp, target, ifName);
        Map<Integer, Long> inOctetsMap = walkLongColumn(snmp, target, ifHCInOctets);
        Map<Integer, Long> outOctetsMap = walkLongColumn(snmp, target, ifHCOutOctets);
        Map<Integer, Long> inUcastPktsMap = walkLongColumn(snmp, target, ifHCInUcastPkts);
        Map<Integer, Long> inMulticastPktsMap = walkLongColumn(snmp, target, ifHCInMulticastPkts);
        Map<Integer, Long> inBroadcastPktsMap = walkLongColumn(snmp, target, ifHCInBroadcastPkts);
        Map<Integer, Long> outUcastPktsMap = walkLongColumn(snmp, target, ifHCOutUcastPkts);
        Map<Integer, Long> outMulticastPktsMap = walkLongColumn(snmp, target, ifHCOutMulticastPkts);
        Map<Integer, Long> outBroadcastPktsMap = walkLongColumn(snmp, target, ifHCOutBroadcastPkts);
        Map<Integer, Long> highSpeedsMap = walkLongColumn(snmp, target, ifHighSpeed); // Mbps
        Map<Integer, Long> fallbackSpeedsMap = walkLongColumn(snmp, target, ifSpeed); // bps

        // --- Histogram HELP/TYPE ---
        sb.append("# HELP interface_utilization Bandwidth utilization percentage over time\n");
        sb.append("# TYPE interface_utilization histogram\n");

        for (Integer idx : ifNamesMap.keySet()) {
            String ifName = ifNamesMap.get(idx);
            long inOctets = inOctetsMap.getOrDefault(idx, 0L);
            long outOctets = outOctetsMap.getOrDefault(idx, 0L);
            long total = inOctets + outOctets;

            long inUcastPkts = inUcastPktsMap.getOrDefault(idx, 0L);
            long inMulticastPkts = inMulticastPktsMap.getOrDefault(idx, 0L);
            long inBroadcastPkts = inBroadcastPktsMap.getOrDefault(idx, 0L);
            long outUcastPkts = outUcastPktsMap.getOrDefault(idx, 0L);
            long outMulticastPkts = outMulticastPktsMap.getOrDefault(idx, 0L);
            long outBroadcastPkts = outBroadcastPktsMap.getOrDefault(idx, 0L);
            long highSpeedMbps = highSpeedsMap.getOrDefault(idx, 0L);
            long fallbackSpeedBps = fallbackSpeedsMap.getOrDefault(idx, 0L);
            long speedBps = highSpeedMbps > 0 ? highSpeedMbps * 1_000_000L : fallbackSpeedBps;

            sb.append(String.format("ifHCInOctets{instance=\"%s\",interface=\"%s\"} %d\n", instance, ifName, inOctets));
            sb.append(String.format("ifHCInUcastPkts{instance=\"%s\",interface=\"%s\"} %d\n", instance, ifName, inUcastPkts));
            sb.append(String.format("ifHCInMulticastPkts{instance=\"%s\",interface=\"%s\"} %d\n", instance, ifName, inMulticastPkts));
            sb.append(String.format("ifHCInBroadcastPkts{instance=\"%s\",interface=\"%s\"} %d\n", instance, ifName, inBroadcastPkts));
            sb.append(String.format("ifHCOutOctets{instance=\"%s\",interface=\"%s\"} %d\n", instance, ifName, outOctets));
            sb.append(String.format("ifHCOutUcastPkts{instance=\"%s\",interface=\"%s\"} %d\n", instance, ifName, outUcastPkts));
            sb.append(String.format("ifHCOutMulticastPkts{instance=\"%s\",interface=\"%s\"} %d\n", instance, ifName, outMulticastPkts));
            sb.append(String.format("ifHCOutBroadcastPkts{instance=\"%s\",interface=\"%s\"} %d\n", instance, ifName, outBroadcastPkts));
            sb.append(String.format("ifSpeed_bps{instance=\"%s\",interface=\"%s\"} %d\n", instance, ifName, speedBps));
            sb.append(String.format("if_total_octets{instance=\"%s\",interface=\"%s\"} %d\n", instance, ifName, total));

            histogramService.renderUtilizationHistogram(instance, ifName, total, speedBps).ifPresent(sb::append);

        }

        return sb.toString();
    }

    private Map<Integer, String> walkStringColumn(Snmp snmp, Target<UdpAddress> target, String baseOid) throws IOException {
        Map<Integer, String> results = new HashMap<>();
        TreeUtils tree = new TreeUtils(snmp, new DefaultPDUFactory());
        List<TreeEvent> events = tree.getSubtree(target, new OID(baseOid));
        for (TreeEvent e : events) {
            if (e.getVariableBindings() != null) {
                for (VariableBinding vb : e.getVariableBindings()) {
                    results.put(vb.getOid().last(), vb.getVariable().toString());
                }
            }
        }
        return results;
    }

    private Map<Integer, Long> walkLongColumn(Snmp snmp, Target<UdpAddress> target, String baseOid) throws IOException {
        Map<Integer, Long> results = new HashMap<>();
        TreeUtils tree = new TreeUtils(snmp, new DefaultPDUFactory());
        List<TreeEvent> events = tree.getSubtree(target, new OID(baseOid));
        for (TreeEvent e : events) {
            if (e.getVariableBindings() != null) {
                for (VariableBinding vb : e.getVariableBindings()) {
                    try {
                        results.put(vb.getOid().last(), Long.parseLong(vb.getVariable().toString()));
                    } catch (NumberFormatException ignored) {} //fixme: need to do something much better here
                }
            }
        }
        return results;
    }

}
