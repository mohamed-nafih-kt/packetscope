package com.packetscope.desktop.service;

import java.time.Instant;

import org.pcap4j.packet.*;
import com.packetscope.desktop.model.CapturedPacket;

public class PacketDecoder {

    public static CapturedPacket decode(Packet packet, Instant timestamp) {

        CapturedPacket result = new CapturedPacket();
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
            result.ipVersion = "IPv4";
            result.srcIp = ipv4.getHeader().getSrcAddr().getHostAddress();
            result.dstIp = ipv4.getHeader().getDstAddr().getHostAddress();

            decodeTransport(ipv4.getPayload(), result);
            return result;
        }

        // IPv6
        if (l3 instanceof IpV6Packet ipv6) {
            result.ipVersion = "IPv6";
            result.srcIp = ipv6.getHeader().getSrcAddr().getHostAddress();
            result.dstIp = ipv6.getHeader().getDstAddr().getHostAddress();

            decodeTransport(ipv6.getPayload(), result);
            return result;
        }

        return null;
    }

    private static void decodeTransport(Packet l4, CapturedPacket result) {

        if (l4 instanceof TcpPacket tcp) {
            result.protocol = "TCP";
            result.srcPort = tcp.getHeader().getSrcPort().valueAsInt();
            result.dstPort = tcp.getHeader().getDstPort().valueAsInt();
            return;
        }

        if (l4 instanceof UdpPacket udp) {
            result.protocol = "UDP";
            result.srcPort = udp.getHeader().getSrcPort().valueAsInt();
            result.dstPort = udp.getHeader().getDstPort().valueAsInt();
            return;
        }

        result.protocol = "OTHER";
    }
}
