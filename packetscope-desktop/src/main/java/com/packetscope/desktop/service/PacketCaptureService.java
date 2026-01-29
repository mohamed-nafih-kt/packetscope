package com.packetscope.desktop.service;

import com.packetscope.desktop.model.CapturedPacket;
import com.packetscope.desktop.persistence.PacketWriteQueue;

import java.util.concurrent.atomic.AtomicLong;

import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;

public class PacketCaptureService {
    
    private final ObservablePacketStream stream;
    private volatile boolean running = false;
    private Thread captureThread;
    private PcapHandle handle;
    private PacketWriteQueue writeQueue;
    private final AtomicLong droppedPackets = new AtomicLong();


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
                e.printStackTrace();
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
                e.printStackTrace();
            }
            handle.close();            
        }
        
        if(captureThread!=null){
            captureThread.interrupt();
        }
    
    }
        
    private void runCaptureLoop() throws Exception {

        PcapNetworkInterface nif = Pcaps.getDevByName("en0");
        int snapshotLength = 65536;
        int readTimeout = 1000;
        
        try{
            handle = nif.openLive(snapshotLength, PromiscuousMode.PROMISCUOUS, readTimeout);
            String filter = "tcp";
            handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
       
        // listener to listen to the packets and defines what to do, overrides gotPacket(Packet packet)
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
                boolean accepted = writeQueue.offer(capturedPacket);    
                if (!accepted) {
                    droppedPackets.incrementAndGet();
                }
            }
        };

        try {
            handle.loop(-1, listener);
        } catch (InterruptedException ignored) {
            
        } finally {
            if (handle.isOpen()) {
                handle.close();
            } 
        }
    }
    

    public PcapHandle getHandle() {
        return handle;
    }
}
