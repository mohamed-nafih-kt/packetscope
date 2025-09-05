package com.packetscope.desktop.service;

import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.NifSelector;

public class PacketCaptureService {

    private PcapHandle handle;

    public PacketCaptureService() throws Exception {

        PcapNetworkInterface nif = Pcaps.getDevByName("en0");
        int snapshotLength = 65536;
        int readTimeout = 1000;

        handle = nif.openLive(snapshotLength, PromiscuousMode.PROMISCUOUS, readTimeout);

        String filter = "tcp port 80";
        handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);

        // listener to listen to the packets and defines what to do, overrides gotPacket(Packet packet)
        PacketListener listener = packet -> {
            System.out.println("capture/time stamp : " + handle.getTimestamp());
            System.out.println("capture/packet data: " + packet);
        };

        try {
            int maxPackets = 30;
            handle.loop(maxPackets, listener);
        } catch (Exception ex) {
            System.out.println("capture/handle loop error: "+ex.getMessage());
        }
        
        handle.close();      
    }
    

    public PcapHandle getHandle() {
        return handle;
    }
}
