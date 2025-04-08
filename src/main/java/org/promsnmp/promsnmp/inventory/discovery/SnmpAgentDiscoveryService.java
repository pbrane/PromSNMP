package org.promsnmp.promsnmp.inventory.discovery;

import org.promsnmp.promsnmp.model.*;
import org.promsnmp.promsnmp.repositories.jpa.CommunityAgentRepository;
import org.promsnmp.promsnmp.repositories.jpa.UserAgentRepository;
import org.promsnmp.promsnmp.repositories.jpa.NetworkDeviceRepository;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class SnmpAgentDiscoveryService {

    private static final OID SYS_OBJECT_ID_OID = new OID("1.3.6.1.2.1.1.2.0");
    private static final OID SYS_NAME_OID = new OID("1.3.6.1.2.1.1.5.0");
    private static final OID SYS_DESCR_OID = new OID("1.3.6.1.2.1.1.1.0");
    private static final OID SYS_CONTACT_OID = new OID("1.3.6.1.2.1.1.4.0");
    private static final OID SYS_LOCATION_OID = new OID("1.3.6.1.2.1.1.6.0");

    private final CommunityAgentRepository communityRepo;
    private final UserAgentRepository userRepo;
    private final NetworkDeviceRepository deviceRepo;

    public SnmpAgentDiscoveryService(CommunityAgentRepository communityRepo, UserAgentRepository userRepo,
                                     NetworkDeviceRepository deviceRepo) {
        this.communityRepo = communityRepo;
        this.userRepo = userRepo;
        this.deviceRepo = deviceRepo;
    }

    private Optional<NetworkDevice> loadDeviceFromMib2(Snmp snmp, Target<UdpAddress> target) throws Exception {
        PDU pdu = new PDU();
        pdu.setType(PDU.GET);
        pdu.add(new VariableBinding(SYS_NAME_OID));
        pdu.add(new VariableBinding(SYS_DESCR_OID));
        pdu.add(new VariableBinding(SYS_CONTACT_OID));
        pdu.add(new VariableBinding(SYS_LOCATION_OID));

        ResponseEvent<UdpAddress> event = snmp.get(pdu, target);
        if (event.getResponse() != null && !event.getResponse().getVariableBindings().isEmpty()) {
            String sysName = null;
            NetworkDevice device = new NetworkDevice();

            for (VariableBinding vb : event.getResponse().getVariableBindings()) {
                if (SYS_NAME_OID.equals(vb.getOid())) {
                    sysName = vb.getVariable().toString();
                    device.setSysName(sysName);
                }
                if (SYS_DESCR_OID.equals(vb.getOid())) device.setSysDescr(vb.getVariable().toString());
                if (SYS_CONTACT_OID.equals(vb.getOid())) device.setSysContact(vb.getVariable().toString());
                if (SYS_LOCATION_OID.equals(vb.getOid())) device.setSysLocation(vb.getVariable().toString());
            }

            if (sysName != null) {
                return deviceRepo.findBySysName(sysName).or(() -> {
                    device.setDiscoveredAt(Instant.now());
                    return Optional.of(deviceRepo.save(device));
                });
            }
        }
        return Optional.empty();
    }

    @Async("snmpExecutor")
    public CompletableFuture<Optional<CommunityAgent>> discoverCommunityAgent(InetAddress address, int port, String community) {
        try {
            CommunityTarget<UdpAddress> target = new CommunityTarget<>();
            target.setCommunity(new OctetString(community));
            target.setAddress(new UdpAddress(address, port));
            target.setRetries(1);
            target.setTimeout(1000);
            target.setVersion(SnmpConstants.version2c);

            try (Snmp snmp = new Snmp(new DefaultUdpTransportMapping())) {
                snmp.listen();

                PDU pdu = new PDU();
                pdu.add(new VariableBinding(SYS_OBJECT_ID_OID));
                pdu.setType(PDU.GET);

                ResponseEvent<UdpAddress> event = snmp.get(pdu, target);
                if (event.getResponse() != null && !event.getResponse().getVariableBindings().isEmpty()) {
                    VariableBinding vb = event.getResponse().get(0);
                    if (!vb.getVariable().isException()) {
                        AgentEndpoint endpoint = new AgentEndpoint(address, port);
                        if (communityRepo.findByEndpoint(endpoint).isEmpty()) {
                            CommunityAgent agent = new CommunityAgent();
                            agent.setEndpoint(endpoint);
                            agent.setRetries(1);
                            agent.setTimeout(1000);
                            agent.setVersion(SnmpConstants.version2c);
                            agent.setReadCommunity(community);
                            agent.setDiscoveredAt(Instant.now());

                            loadDeviceFromMib2(snmp, target).ifPresent(agent::setDevice);

                            return CompletableFuture.completedFuture(Optional.of(communityRepo.save(agent)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            // log as needed
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Async("snmpExecutor")
    public CompletableFuture<Optional<UserAgent>> discoverUserAgent(InetAddress address, int port,
                                                                    String username,
                                                                    String authPass,
                                                                    String privPass) {
        try {
            OctetString user = new OctetString(username);
            OID authProtocol = AuthSHA.ID;
            OID privProtocol = PrivAES128.ID;

            UserTarget<UdpAddress> target = new UserTarget<>();
            target.setAddress(new UdpAddress(address, port));
            target.setRetries(1);
            target.setTimeout(1000);
            target.setVersion(SnmpConstants.version3);
            target.setSecurityName(user);
            target.setSecurityLevel(SecurityLevel.AUTH_PRIV);

            TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            snmp.listen();

            USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
            SecurityModels.getInstance().addSecurityModel(usm);
            snmp.getUSM().addUser(user,
                    new UsmUser(user, authProtocol, new OctetString(authPass), privProtocol, new OctetString(privPass)));

            ScopedPDU pdu = new ScopedPDU();
            pdu.setType(PDU.GET);
            pdu.add(new VariableBinding(SYS_OBJECT_ID_OID));

            ResponseEvent event = snmp.get(pdu, target);
            if (event.getResponse() != null && !event.getResponse().getVariableBindings().isEmpty()) {
                VariableBinding vb = event.getResponse().get(0);
                if (!vb.getVariable().isException()) {
                    AgentEndpoint endpoint = new AgentEndpoint(address, port);
                    if (userRepo.findByEndpoint(endpoint).isEmpty()) {
                        UserAgent agent = new UserAgent();
                        agent.setEndpoint(endpoint);
                        agent.setRetries(1);
                        agent.setTimeout(1000);
                        agent.setVersion(SnmpConstants.version3);
                        agent.setSecurityName(username);
                        agent.setSecurityLevel(SecurityLevel.AUTH_PRIV);
                        agent.setAuthPassphrase(authPass);
                        agent.setPrivPassphrase(privPass);
                        agent.setDiscoveredAt(Instant.now());

                        loadDeviceFromMib2(snmp, target).ifPresent(agent::setDevice);

                        return CompletableFuture.completedFuture(Optional.of(userRepo.save(agent)));
                    }
                }
            }

        } catch (Exception e) {
            // log as needed
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    public CompletableFuture<List<CommunityAgent>> discoverMultiple(List<InetAddress> addresses, int port, String community) {
        List<CompletableFuture<Optional<CommunityAgent>>> futures = addresses.stream()
                .map(addr -> discoverCommunityAgent(addr, port, community))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList()));
    }

    public CompletableFuture<List<UserAgent>> discoverMultipleV3(List<InetAddress> addresses, int port,
                                                                 String username,
                                                                 String authPass,
                                                                 String privPass) {
        List<CompletableFuture<Optional<UserAgent>>> futures = addresses.stream()
                .map(addr -> discoverUserAgent(addr, port, username, authPass, privPass))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList()));
    }
}
