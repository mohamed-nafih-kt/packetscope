package com.packetscope.desktop.service;

import com.packetscope.desktop.model.CapturedPacket;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ObservablePacketStream {
    
    private static final int MAX_ITEMS = 500;
    
    private final ObservableList<CapturedPacket> packets = 
            FXCollections.observableArrayList();
    
    public ObservableList<CapturedPacket> getPackets(){
            return packets;
    }
    
    public void publish(CapturedPacket packet){
        // Ensures UI-thread safety
        Platform.runLater(() -> {
            packets.add(0, packet);
            
            if(packets.size() > MAX_ITEMS){
                packets.remove(MAX_ITEMS, packets.size());
            }           
        });
    }
}
