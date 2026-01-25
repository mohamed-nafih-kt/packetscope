package com.packetscope.desktop.service;

import com.packetscope.desktop.model.CapturedPacket;
import java.time.Instant;
import org.pcap4j.core.*;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.util.NifSelector;



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

        System.out.println("\n[INFO]"+"-".repeat(20)+" program started "+"-".repeat(20)+"\n");

        PcapNetworkInterface device = null;
        
        device = getNetworkInterface();
        System.out.println("[INFO]You chose: " + device);

        if (device == null) {
            System.out.print("[INFO]no device selected");
            System.exit(1);
        }

        int snapshotLength = 65536;
        int readTimeout = 1000;
        final PcapHandle handle;

        // handler to handle the device(en0)
        try {
            System.out.println("waiting for packets...\n");
            handle = device.openLive(snapshotLength, PromiscuousMode.PROMISCUOUS, readTimeout);
            String filter = "tcp";          
            handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("DLT: " + handle.getDlt());

       
        PacketListener listener = packet -> {

            Instant ts = handle.getTimestamp().toInstant();

            CapturedPacket decoded =
                PacketDecoder.decode(packet, ts);

            if (decoded != null) {
                System.out.println("decoded: "+decoded);
            }
        };
        
        try {
            int maxPackets = 30;
            handle.loop(maxPackets, listener);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            handle.close();
        }

        System.out.println("program ended successfully");
    }

}
