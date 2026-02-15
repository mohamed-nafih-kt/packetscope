package com.packetscope.desktop.service;

import java.time.Instant;
import java.net.InetAddress;
import java.util.Set;
import org.pcap4j.packet.*;

import com.packetscope.desktop.model.CapturedPacket;
import com.packetscope.desktop.model.TransportProtocol;
import com.packetscope.desktop.model.PacketDirection;

public class PacketDecoder {
    
    private static final Set<InetAddress> LOCAL_IPS =
            LocalIpResolver.resolve();

    public static CapturedPacket decode(Packet packet, Instant timestamp, String interfaceName) {

        CapturedPacket result = new CapturedPacket();

        result.direction = PacketDirection.UNKNOWN;
        result.interfaceName = interfaceName;      
        result.packetSize = packet.length();
        result.timestamp = timestamp;

        // Ethernet is guaranteed at DLT 1
        EthernetPacket eth = packet.get(EthernetPacket.class);
        if (eth == null) {
            return null; // undecodable, ignore
        }

        Packet l3 = eth.getPayload();
        if (l3 == null) {
            return null;
        }

        // IPv4
        if (l3 instanceof IpV4Packet ipv4) {
            
            InetAddress src = ipv4.getHeader().getSrcAddr();
            InetAddress dst = ipv4.getHeader().getDstAddr();
            
            result.ipVersion = 4;
            result.sourceIp = src.getAddress();
            result.destinationIp = dst.getAddress();
                     
            classifyDirection(src, dst, result);
            decodeTransport(ipv4.getPayload(), result);
            return result;
        }

        // IPv6
        if (l3 instanceof IpV6Packet ipv6) {
           
            InetAddress src = ipv6.getHeader().getSrcAddr();
            InetAddress dst = ipv6.getHeader().getDstAddr();

            result.ipVersion = 6;
            result.sourceIp = src.getAddress();
            result.destinationIp = dst.getAddress();

            classifyDirection(src, dst, result);
            decodeTransport(ipv6.getPayload(), result);
            return result;
        }

        return null;
    }

    private static void decodeTransport(Packet l4, CapturedPacket result) {

        if (l4 instanceof TcpPacket tcp) {
            result.protocol = TransportProtocol.TCP;
            result.sourcePort = tcp.getHeader().getSrcPort().valueAsInt();
            result.destinationPort = tcp.getHeader().getDstPort().valueAsInt();
            return;
        }

        if (l4 instanceof UdpPacket udp) {
            result.protocol = TransportProtocol.UDP;
            result.sourcePort = udp.getHeader().getSrcPort().valueAsInt();
            result.destinationPort = udp.getHeader().getDstPort().valueAsInt();
            return;
        }

        result.protocol = TransportProtocol.UNKNOWN;
    }
    
    private static void classifyDirection(InetAddress src, InetAddress dst, CapturedPacket r) {

        if (LOCAL_IPS.contains(src))
            r.direction = PacketDirection.OUTBOUND;
        else if (LOCAL_IPS.contains(dst))
            r.direction = PacketDirection.INBOUND;
        else
            r.direction = PacketDirection.UNKNOWN;
    }
}

