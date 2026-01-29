package com.packetscope.desktop.controller;

import com.packetscope.desktop.service.PacketCaptureService;
import com.packetscope.desktop.service.ObservablePacketStream;
import com.packetscope.desktop.model.CapturedPacket;
import com.packetscope.desktop.persistence.PacketWriteQueue;
import com.packetscope.desktop.persistence.PacketPersistenceWorker;
import com.packetscope.desktop.DbConnection;

import java.sql.Connection;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;


public class PrimaryController {

    @FXML
    private StackPane rootStack;

    @FXML
    private StackPane overlayPane;

    @FXML
    private VBox rootPane;

    @FXML
    private Button startButton;

    @FXML
    private ListView<CapturedPacket> packetListView; 
    
    private ObservablePacketStream packetStream;
    
    private PacketCaptureService packetCaptureService;
    
    private PacketPersistenceWorker worker ;
    

    
    @FXML
    Label protectedLabel;
    
    private boolean running = false;
    private Timeline timeline;
    int MAX_ITEMS = 100;
    
    // capture packets
    @FXML
    private void startCapture(ActionEvent e) {
        
        if (packetCaptureService == null) {
            System.err.println("Capture service not initialised yet");
            return;
        }
        if (running) {
            startButton.setStyle("-fx-border-color: rgb(28,115,232)");
            startButton.setText("START");

            protectedLabel.setText("UnProtected");
            protectedLabel.setStyle("-fx-background-color: rgba(255,30,30,0.5)");

            packetCaptureService.stop();
            running = false;
        } else {

            protectedLabel.setText("Protected");
            protectedLabel.setStyle("-fx-background-color: rgba(0, 225, 121, 0.51)");
            protectedLabel.setVisible(true);

            startButton.setStyle("-fx-border-color: rgb(255,62,72)");
            startButton.setText("STOP");
            
            packetCaptureService.start();
            running = true;
        }
    }

    public void setPacketStream(ObservablePacketStream packetStream) {
        this.packetStream = packetStream;    
        
        PacketWriteQueue writeQueue = new PacketWriteQueue(1000);
        packetCaptureService =  new PacketCaptureService(packetStream, writeQueue);
        
        DbConnection dbConnection = new DbConnection();
        Connection con = dbConnection.getConnection();
        
        packetListView.setItems(packetStream.getPackets());
        startButton.setDisable(false);
        
        packetStream.getPackets().addListener((ListChangeListener<CapturedPacket>) c-> {
            if(!packetStream.getPackets().isEmpty()){
                packetListView.scrollTo(0);
            }
        });
                
        worker = new PacketPersistenceWorker(writeQueue.getQueue(),con);
        
        Thread workerThread = new Thread(worker, "packet-db-writer");
        workerThread.setDaemon(true);
        workerThread.start();

        

    }
    
    @FXML
    public void initialize() {
        startButton.setDisable(true);
    }
        
}
