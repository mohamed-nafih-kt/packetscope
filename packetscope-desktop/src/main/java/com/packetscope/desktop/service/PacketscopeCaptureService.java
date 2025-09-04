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

import com.packetscope.desktop.service.CookedPacketAnalyser;

public class PacketScopeCaptureService {

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
            String filter = "tcp port 80";
            handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("DLT: " + handle.getDlt());

        // listener to listen to the packets and defines what to do, overrides gotPacket(Packet packet)
        PacketListener listener = packet -> {
            capturedPackets.add(packet);
            System.out.println("time stamp : " + handle.getTimestamp());
            System.out.println("packet data: " + packet);
        };

        try {
            int maxPackets = 30;
            handle.loop(maxPackets, listener);
        } catch (Exception ex) {
            System.getLogger(PacketScopeCaptureService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

        capturedPackets.forEach(packet -> {
            // raw Data
            try {
                System.out.println("------NEW PACKET---------");
                byte[] header = packet.getHeader().getRawData();
                if (header != null) {
                    String headerData = new String(header);
                    System.out.println("header: " + headerData);
                } else {
                    System.out.println("no header found !");
                }
            } catch (Exception e) {
                System.out.println(e);
            }
            // Direct Decoded Data
            try {
                System.out.println("--------------------");
                System.out.println("decode packet: " + decodePacket(packet));
                System.out.println("--------------------");
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

    static private String decodePacket(Packet packet) {
        byte[] data = packet.getRawData();
        if (data == null) {
            return "";
        }

        StringBuilder string = new StringBuilder();
        for (byte b : data) {
            string.append((char) (b & 0xFF));
        }

        return string.toString();
    }



}
