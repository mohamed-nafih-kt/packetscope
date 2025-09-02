package com.packetscope.desktop;

import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV6Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;

public class CookedPacketAnalyser {

    public static void analysePacket(Packet packet) {
        byte[] data = packet.getRawData();
        String srcIp;
        String dstIp;
        int srcPort;
        int dstPort;
        int length;
        byte[] payload = null;
        TcpPacket tcp;
        // too short packets
        if (data.length < 14) {
            System.out.println("unknown packet");
            return;
        }

        int etherType = ((data[12] & 0xFF) << 8) | (data[13] & 0xFF);

        switch (etherType) {
            case 0x0800:
                System.out.println("IPv4 packet");
                break;
            case 0x86DD:
                System.out.println("IPv6 packet");
                break;
            default:
                System.out.println("Unknown/other packet");
                return;
        }

        int offset = 14;

        if (etherType == 0x0800) {
            try {
                IpV4Packet ip = IpV4Packet.newPacket(data, offset, data.length - offset);
                srcIp = ip.getHeader().getSrcAddr().getHostAddress();
                dstIp = ip.getHeader().getDstAddr().getHostAddress();
                tcp = ip.get(TcpPacket.class);
                srcPort = tcp.getHeader().getSrcPort().valueAsInt();
                dstPort = tcp.getHeader().getDstPort().valueAsInt();
                length = tcp.length();
                payload = tcp.getPayload() != null ? tcp.getPayload().getRawData() : null;
            } catch (IllegalRawDataException ex) {

                manualParseIp(packet);
                System.out.println("failed to convert to IP packet");
            }
        } else if (etherType == 0x86DD) {
            try {
                manualParseIp(packet);
            } catch (Exception ex) {
                System.out.println("failed to convert to IP packet" + ex);
            }
        }
        System.out.println();

        if (payload != null) {
            System.out.println(getDomainName(payload));
        } else {
            System.out.println("no payload");
        }

    }

    public static String getDomainName(byte[] payload) {
        String host = null;
        if (payload != null) {
            String dataStr = new String(payload);
            if (dataStr.startsWith("GET") || dataStr.startsWith("POST")) {
                int hostIndex = dataStr.indexOf("Host:");
                if (hostIndex >= 0) {
                    int endLine = dataStr.indexOf("\r\n", hostIndex);
                    host = dataStr.substring(hostIndex + 5, endLine).trim();
                }
            }
        }
        return host;
    }

    public static void manualParseIp(Packet packet) {
        byte[] data = packet.getRawData();

        String srcIp = String.format("%d.%d.%d.%d",
                data[12] & 0xFF,
                data[13] & 0xFF,
                data[14] & 0xFF,
                data[15] & 0xFF
        );

        String dstIp = String.format("%d.%d.%d.%d",
                data[16] & 0xFF,
                data[17] & 0xFF,
                data[18] & 0xFF,
                data[19] & 0xFF
        );
        System.out.println("source ip: " + srcIp);
        System.out.println("destination ip: " + srcIp);
    }

}
