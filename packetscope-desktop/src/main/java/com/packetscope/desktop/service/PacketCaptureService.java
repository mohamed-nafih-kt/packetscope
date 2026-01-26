package com.packetscope.desktop.service;

import com.packetscope.desktop.model.CapturedPacket;
import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.NifSelector;

import java.time.Instant;

public class PacketCaptureService {
    
    private final ObservablePacketStream stream;
    private volatile boolean running = false;
    private Thread captureThread;
    private PcapHandle handle;

    public PacketCaptureService(ObservablePacketStream stream) {
        this.stream = stream;
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
            
            CapturedPacket decoded =
                PacketDecoder.decode(
                    packet,
                    handle.getTimestamp().toInstant()
                );
            if (decoded != null) {
                stream.publish(decoded);
            }
        };

        try {
            handle.loop(-1, listener); // infinite loop
        } catch (InterruptedException ignored) {
            // expected on stop()
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
