package com.packetscope.desktop.service;

import java.time.Instant;
import java.net.InetAddress;
import java.util.Set;
import org.pcap4j.packet.*;

import com.packetscope.desktop.model.CapturedPacket;
import com.packetscope.desktop.model.TransportProtocol;
import com.packetscope.desktop.model.PacketDirection;

public class PacketDecoder {
    
    private static final Set<InetAddress> LOCAL_IPS = LocalIpResolver.resolve();

    public static CapturedPacket decode(Packet packet, Instant timestamp, String interfaceName) {
        if (packet == null) return null;



        // Ethernet is the base for DLT 1
        EthernetPacket eth = packet.get(EthernetPacket.class);
        if (eth == null || eth.getPayload() == null) {
            return null;
        }

        Packet l3 = eth.getPayload();
        CapturedPacket result = new CapturedPacket();
        result.direction = PacketDirection.UNKNOWN;
        result.interfaceName = interfaceName;      
        result.packetSize = packet.length();
        result.timestamp = timestamp;
        
        // IPv4
        if (l3 instanceof IpV4Packet ipv4) {
            
            populateNetworkData(ipv4.getHeader().getSrcAddr(), ipv4.getHeader().getDstAddr(), 4, result);
            decodeTransport(ipv4.getPayload(), result);
            return result;
        }

        // IPv6
        if (l3 instanceof IpV6Packet ipv6) {
           
            populateNetworkData(ipv6.getHeader().getSrcAddr(), ipv6.getHeader().getDstAddr(), 6, result);
            decodeTransport(ipv6.getPayload(), result);
            return result;
        }

        return null;
    }

    private static void decodeTransport(Packet l4, CapturedPacket result) {
        if (l4 == null) {
            result.protocol = TransportProtocol.UNKNOWN;
            return;
        }

        if (l4 instanceof TcpPacket tcp) {
            result.protocol = TransportProtocol.TCP;
            result.sourcePort = tcp.getHeader().getSrcPort().valueAsInt();
            result.destinationPort = tcp.getHeader().getDstPort().valueAsInt();
            return;
        }else if (l4 instanceof UdpPacket udp) {
            result.protocol = TransportProtocol.UDP;
            result.sourcePort = udp.getHeader().getSrcPort().valueAsInt();
            result.destinationPort = udp.getHeader().getDstPort().valueAsInt();
            return;
        }else{
            result.protocol = TransportProtocol.UNKNOWN;
        }

        
    }
    
    private static void populateNetworkData(InetAddress src, InetAddress dst, int version, CapturedPacket result) {
        result.ipVersion = version;
        result.sourceIp = src.getAddress();
        result.destinationIp = dst.getAddress();

        // Classify traffic flow direction
        if (LOCAL_IPS.contains(src))
            result.direction = PacketDirection.OUTBOUND;
        else if (LOCAL_IPS.contains(dst))
            result.direction = PacketDirection.INBOUND;
        else
            result.direction = PacketDirection.UNKNOWN;
    }
}

