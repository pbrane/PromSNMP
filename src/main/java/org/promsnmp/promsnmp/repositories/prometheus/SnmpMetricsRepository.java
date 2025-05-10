package org.promsnmp.promsnmp.repositories.prometheus;

import lombok.extern.slf4j.Slf4j;
import org.promsnmp.promsnmp.mappers.AgentToTargetMapper;
import org.promsnmp.promsnmp.model.Agent;
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

import static java.util.Map.entry;

@Slf4j
@Repository("SnmpMetricsRepo")
public class SnmpMetricsRepository implements PrometheusMetricsRepository {

    private record InterfaceInfo(int index, String ifDescr, String ifName, String ifAlias) {}
    private record MetricInfo(String name, String oid, String help, String type, boolean walkable) {}

    public static final String              SYS_UPTIME = "1.3.6.1.2.1.1.3.0";
    public static final String                 IF_NAME = "1.3.6.1.2.1.31.1.1.1.1";
    public static final String         IF_HC_IN_OCTETS = "1.3.6.1.2.1.31.1.1.1.6";
    public static final String      IF_HC_IN_UCAST_PKTS = "1.3.6.1.2.1.31.1.1.1.7";
    public static final String  IF_HC_IN_MULTICAST_PKTS = "1.3.6.1.2.1.31.1.1.1.8";
    public static final String  IF_HC_IN_BROADCAST_PKTS = "1.3.6.1.2.1.31.1.1.1.9";
    public static final String         IF_HC_OUT_OCTETS = "1.3.6.1.2.1.31.1.1.1.10";
    public static final String     IF_HC_OUT_UCAST_PKTS = "1.3.6.1.2.1.31.1.1.1.11";
    public static final String IF_HC_OUT_MULTICAST_PKTS = "1.3.6.1.2.1.31.1.1.1.12";
    public static final String IF_HC_OUT_BROADCAST_PKTS = "1.3.6.1.2.1.31.1.1.1.13";
    public static final String            IF_HIGH_SPEED = "1.3.6.1.2.1.31.1.1.1.15";
    public static final String                 IF_SPEED = "1.3.6.1.2.1.2.2.1.5";
    public static final String                 IF_DESCR = "1.3.6.1.2.1.2.2.1.2";
    public static final String                 IF_ALIAS = "1.3.6.1.2.1.31.1.1.1.18";

    private static final Map<String, MetricInfo> METRICS = Map.ofEntries(
            entry("sysUpTime", new MetricInfo("sysUpTime", SYS_UPTIME, "System uptime in hundredths of a second", "gauge", true)),
            entry("ifHCInOctets", new MetricInfo("ifHCInOctets", IF_HC_IN_OCTETS, "The total number of octets received on the interface, including framing characters", "counter", true)),
            entry("ifHCInUcastPkts", new MetricInfo("ifHCInUcastPkts", IF_HC_IN_UCAST_PKTS, "Packets received not addressed to multicast/broadcast", "counter", true)),
            entry("ifHCInMulticastPkts", new MetricInfo("ifHCInMulticastPkts", IF_HC_IN_MULTICAST_PKTS, "Multicast packets received", "counter", true)),
            entry("ifHCInBroadcastPkts", new MetricInfo("ifHCInBroadcastPkts", IF_HC_IN_BROADCAST_PKTS, "Broadcast packets received", "counter", true)),
            entry("ifHCOutOctets", new MetricInfo("ifHCOutOctets", IF_HC_OUT_OCTETS, "The total number of octets transmitted including framing characters", "counter", true)),
            entry("ifHCOutUcastPkts", new MetricInfo("ifHCOutUcastPkts", IF_HC_OUT_UCAST_PKTS, "Packets sent not addressed to multicast/broadcast", "counter", true)),
            entry("ifHCOutMulticastPkts", new MetricInfo("ifHCOutMulticastPkts", IF_HC_OUT_MULTICAST_PKTS, "Multicast packets sent", "counter", true)),
            entry("ifHCOutBroadcastPkts", new MetricInfo("ifHCOutBroadcastPkts", IF_HC_OUT_BROADCAST_PKTS, "Broadcast packets sent", "counter", true)),
            entry("ifHighSpeed", new MetricInfo("ifHighSpeed", IF_HIGH_SPEED, "Interface speed (Mbps)", "gauge", true)),
            entry("ifSpeed", new MetricInfo("ifSpeed", IF_SPEED, "Interface speed (bps)", "gauge", true)),
            entry("interface_utilization", new MetricInfo("interface_utilization", "custom.histogram", "Interface bandwidth utilization as histogram", "histogram", false))
    );

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
        Instant start = Instant.now();
        StringBuilder sb = new StringBuilder();

        // --- sysUpTime ---
        sb.append("# HELP sysUpTime system uptime in hundredths of a second - 1.3.6.1.2.1.1.3.0\n");
        sb.append("# TYPE sysUpTime gauge\n");
        PDU sysPdu = new PDU();
        sysPdu.add(new VariableBinding(new OID(SYS_UPTIME)));
        sysPdu.setType(PDU.GET);
        ResponseEvent<UdpAddress> sysResp = snmp.get(sysPdu, target);
        if (sysResp.getResponse() != null && !sysResp.getResponse().getVariableBindings().isEmpty()) {
            Variable uptime = sysResp.getResponse().get(0).getVariable();

            if (uptime instanceof TimeTicks) {
                long hundredths = ((TimeTicks) uptime).toMilliseconds() / 10;
                double seconds = hundredths / 100.0;
                sb.append(String.format("sysUpTime{instance=\"%s\"} %.2f\n", instance, seconds));
            }

        }

        // --- Walk identifiers ---
        Map<Integer, String> ifDescr = walkStringColumn(snmp, target, IF_DESCR);
        Map<Integer, String> ifName  = walkStringColumn(snmp, target, IF_NAME);
        Map<Integer, String> ifAlias = walkStringColumn(snmp, target, IF_ALIAS);

        Map<Integer, InterfaceInfo> interfaces = new HashMap<>();
        for (Integer idx : ifDescr.keySet()) {
            interfaces.put(idx, new InterfaceInfo(
                    idx,
                    ifDescr.getOrDefault(idx, ""),
                    ifName.getOrDefault(idx, ""),
                    ifAlias.getOrDefault(idx, "")
            ));
        }

        // --- Emit metrics grouped by type ---
        for (var entry : METRICS.entrySet()) {
            String metricName = entry.getKey();
            MetricInfo info = entry.getValue();
            if (info.oid().startsWith("custom.")) continue; // Skip custom metrics (e.g., histograms)

            sb.append(String.format("# HELP %s %s - %s\n", metricName, info.help(), info.oid()));
            sb.append(String.format("# TYPE %s %s\n", metricName, info.type()));

            Map<Integer, Long> values = walkLongColumn(snmp, target, info.oid());
            for (var idx : values.keySet()) {
                InterfaceInfo iface = interfaces.get(idx);
                if (iface != null) {
                    sb.append(render(metricName, iface, instance, values.get(idx)));
                }
            }
        }

        // --- Histograms ---
        for (var iface : interfaces.values()) {
            long in  = walkLongColumn(snmp, target, METRICS.get("ifHCInOctets").oid())
                    .getOrDefault(iface.index(), 0L);
            long out = walkLongColumn(snmp, target, METRICS.get("ifHCOutOctets").oid())
                    .getOrDefault(iface.index(), 0L);
            long totalOctets = in + out;

            long highSpeed = walkLongColumn(snmp, target, METRICS.get("ifHighSpeed").oid())
                    .getOrDefault(iface.index(), 0L);
            long speed = highSpeed > 0
                    ? highSpeed * 1_000_000L
                    : walkLongColumn(snmp, target, METRICS.get("ifSpeed").oid())
                    .getOrDefault(iface.index(), 0L);

            histogramService.renderUtilizationHistogram(instance, iface.ifName(), totalOctets, speed)
                    .ifPresent(sb::append);
        }

        // --- Scrape duration ---
        sb.append("# HELP snmp_scrape_duration_seconds Total SNMP time scrape took (walk and processing).\n");
        sb.append("# TYPE snmp_scrape_duration_seconds gauge\n");
        sb.append(String.format("snmp_scrape_duration_seconds{module=\"promsnmp\"} %.6f\n",
                Duration.between(start, Instant.now()).toMillis() / 1000.0));

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
                        results.put(vb.getOid().last(), vb.getVariable().toLong());
                    } catch (NumberFormatException ignored) {} //fixme: need to do something much better here
                }
            }
        }
        return results;
    }

    private String render(String metric, InterfaceInfo iface, String instance, long value) {
        return String.format(
                "%s{instance=\"%s\",ifDescr=\"%s\",ifName=\"%s\",ifIndex=\"%d\",ifAlias=\"%s\"} %d\n",
                metric,
                instance,
                iface.ifDescr(),
                iface.ifName(),
                iface.index(),
                iface.ifAlias(),
                value
        );
    }

    private void emitMetricHeader(StringBuilder sb, String metric, Map<?, ?> values) {
        if (!values.isEmpty()) {
            MetricInfo info = METRICS.get(metric);
            if (info != null) {
                sb.append(String.format("# HELP %s %s - %s\n", info.name(), info.help(), info.oid()));
                sb.append(String.format("# TYPE %s %s\n", info.name(), info.type()));
            }
        }
    }

    private String getOidForMetric(String metric) {
        return METRICS.getOrDefault(metric, new MetricInfo(metric, "unknown", "", "", false)).oid();
    }

}
