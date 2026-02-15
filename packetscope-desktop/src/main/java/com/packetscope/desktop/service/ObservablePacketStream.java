package com.packetscope.desktop.service;

import com.packetscope.desktop.model.CapturedPacket;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ObservablePacketStream {
    
    private static final int MAX_ITEMS = 500;
    
    private final ObservableList<CapturedPacket> packets = 
            FXCollections.observableArrayList();
    
    public ObservableList<CapturedPacket> getPackets() {
        return packets;
    }
    
    /**
     * Publishes a new packet to the observable list.
     * Ensures UI-thread safety and maintains the maximum buffer size.
     */
    public void publish(CapturedPacket packet) {
        Platform.runLater(() -> {
            packets.add(packet);
            
            // Maintain buffer size by removing the oldest packet
            if (packets.size() > MAX_ITEMS) {
                packets.remove(0);
            }           
        });
    }
}