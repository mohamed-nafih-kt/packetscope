package com.packetscope.desktop.service;

import java.util.ArrayList;
import java.util.List;
import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.NifSelector;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.IpV4Packet;



public class PacketCapturePlayground {

    // selecting network interface
    static PcapNetworkInterface getNetworkInterface() {
        PcapNetworkInterface device = null;
        try {
            device = new NifSelector().selectNetworkInterface();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return device;
    }

    public static void main(String[] args) {

        System.out.println("program started");

        PcapNetworkInterface device = null;
        
        device = getNetworkInterface();
        System.out.println("You chose: " + device);

        if (device == null) {
            System.out.print("no device selected");
            System.exit(1);
        }

        int snapshotLength = 65536;
        int readTimeout = 1000;
        final PcapHandle handle;
        List<Packet> capturedPackets = new ArrayList<>();

        // handler to handle the device(en0)
        try {
            System.out.println("waiting for packets...");
            handle = device.openLive(snapshotLength, PromiscuousMode.PROMISCUOUS, readTimeout);
            String filter = "tcp";          
            handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("DLT: " + handle.getDlt());

        // listener to listen to the packets and defines what to do, overrides gotPacket(Packet packet)
        PacketListener packetListener = new PacketListener() {
            @Override
            public void gotPacket(Packet packet) {
                // This is the "Raw" Ethernet frame
                System.out.println("got packet");
                EthernetPacket ether = packet.get(EthernetPacket.class);

                if (ether != null) {
                    // Now you can dive into Layer 3 (IP)
                    System.out.println("got ether");
                    IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
                    

                    if (ipV4Packet != null) {
                        System.out.println("got packet ipv4");
                        // Now Layer 4 (TCP/UDP)
                        TcpPacket tcpPacket = packet.get(TcpPacket.class);
                        // ... and so on
                    }
                }
            }
        };
        packetListener = packet -> {
        System.out.println("time stamp : " + handle.getTimestamp());
            
        if (!packet.contains(EthernetPacket.class)) {
            System.out.println("Non-Ethernet packet");
            System.out.println("----");
            return;
        }
        System.out.println("Is an Ethernet packet");

        if (packet.contains(org.pcap4j.packet.IpV4Packet.class)) {
            var ipv4 = packet.get(org.pcap4j.packet.IpV4Packet.class);
            System.out.println("IPv4 SRC: " + ipv4.getHeader().getSrcAddr());
            System.out.println("IPv4 DST: " + ipv4.getHeader().getDstAddr());
        }
        EthernetPacket eth = packet.get(EthernetPacket.class);
        Packet payload = eth.getPayload();

        if (packet.contains(org.pcap4j.packet.IpV6Packet.class)) {
            var ipv6 = packet.get(org.pcap4j.packet.IpV6Packet.class);
            System.out.println("IPv6 SRC: " + ipv6.getHeader().getSrcAddr());
            System.out.println("IPv6 DST: " + ipv6.getHeader().getDstAddr());
        }

        if (packet.contains(org.pcap4j.packet.TcpPacket.class)) {
            var tcp = packet.get(org.pcap4j.packet.TcpPacket.class);
            System.out.println("TCP SRC PORT: " + tcp.getHeader().getSrcPort());
            System.out.println("TCP DST PORT: " + tcp.getHeader().getDstPort());
        }

        if (packet.contains(org.pcap4j.packet.UdpPacket.class)) {
            var udp = packet.get(org.pcap4j.packet.UdpPacket.class);
            System.out.println("UDP SRC PORT: " + udp.getHeader().getSrcPort());
            System.out.println("UDP DST PORT: " + udp.getHeader().getDstPort());
        }
        
        if (packet.contains(org.pcap4j.packet.IpPacket.class)) {
            var ip = packet.get(org.pcap4j.packet.IpPacket.class);
            System.out.println(
                ip.getHeader().getSrcAddr() + " -> " +
                ip.getHeader().getDstAddr()
            );
        }
        System.out.println("----");
        };


        try {
            int maxPackets = 30;
            handle.loop(maxPackets, packetListener);
        } catch (Exception ex) {
            System.getLogger(PacketCapturePlayground.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

        capturedPackets.forEach(packet -> {
            // raw Data
            try {
                System.out.println("------NEW PACKET---------");
                byte[] raw = packet.getRawData();
                if (raw != null) {
                    String rawData = new String(raw);
                    System.out.println("raw: " + rawData);
                } else {
                    System.out.println("no raw found !");
                }
            } catch (Exception e) {
                System.out.println(e);
            }

            // cooked packet analyser data
            CookedPacketAnalyser.analysePacket(packet);

        });

        //end program
        handle.close();
        System.out.println("program ended successfully");
    }

}
