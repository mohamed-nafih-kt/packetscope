package com.packetscope.desktop.service;

import com.packetscope.desktop.model.CapturedPacket;
import com.packetscope.desktop.persistence.PacketWriteQueue;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;

public class PacketCaptureService {
    
    private static final Logger LOGGER = Logger.getLogger(PacketCaptureService.class.getName());
    private final ObservablePacketStream stream;
    private final PacketWriteQueue writeQueue;
    private final AtomicLong droppedPackets = new AtomicLong();
    
    private volatile boolean running = false;
    private Thread captureThread;
    private PcapHandle handle;


    public PacketCaptureService(ObservablePacketStream stream, PacketWriteQueue writeQueue) {
        this.stream = stream;
        this.writeQueue = writeQueue;
    }
    
    public void start(){
        if(running) return;
        running = true;
        
        captureThread = new Thread(() ->{
            try{
                runCaptureLoop();
            }catch(Exception e){
                LOGGER.log(Level.SEVERE, "Packet capture loop terminated unexpectedly", e);
            }finally {
                running = false;
            }
        },"packet-capture-thread");
        
        captureThread.setDaemon(true);
        captureThread.start();
        
    }
    
    public void stop(){
        running=false;
        
        if(handle!=null && handle.isOpen()){
            try{
                handle.breakLoop();
            }catch(Exception e){
                LOGGER.log(Level.WARNING, "Error breaking pcap loop", e);
            }
                      
        }
    
    }
        
    private void runCaptureLoop() throws Exception {

        PcapNetworkInterface nif = selectInterface();        
        if (nif == null) {
            throw new RuntimeException("No capture interface found");
        }

        
        int snapshotLength = 65536;
        int readTimeout = 1000;
        
        try{
            handle = nif.openLive(snapshotLength, PromiscuousMode.PROMISCUOUS, readTimeout);
            String filter = "tcp";
            handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to open network interface. Verify Npcap/libpcap installation.", e);
            running = false;
            return;
        }
        
       
        PacketListener listener = (Packet packet) -> {
            if (!running) return;
            
            CapturedPacket capturedPacket =
                PacketDecoder.decode(
                    packet,
                    handle.getTimestamp().toInstant(),
                    nif.getName()
                );
            if (capturedPacket != null) {
                stream.publish(capturedPacket);   
                if (!writeQueue.offer(capturedPacket)) {
                    droppedPackets.incrementAndGet();
                }
            }
        };

        try {
            LOGGER.info("Starting capture on: " + nif.getDescription());
            handle.loop(-1, listener);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            if (handle != null && handle.isOpen()) {
                handle.close();
            } 
        }
    }
    

    public PcapHandle getHandle() {
        return handle;
    }
    
    private PcapNetworkInterface selectInterface() throws Exception {
        for (PcapNetworkInterface dev : Pcaps.findAllDevs()) {
            if (!dev.isLoopBack() && dev.isUp() && !dev.getAddresses().isEmpty()) {
                return dev;
            }
        }
        return null;
    }
    
    public boolean isCaptureAvailable() {
        try {
            return !Pcaps.findAllDevs().isEmpty();
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Pcaps check failed", e);
            return false;
        }
    }
    
    public long getDroppedPacketCount() {
        return droppedPackets.get();
    }


}
