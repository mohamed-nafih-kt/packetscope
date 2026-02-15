package com.packetscope.desktop.controller;

import com.packetscope.desktop.service.PacketCaptureService;
import com.packetscope.desktop.service.ObservablePacketStream;
import com.packetscope.desktop.model.CapturedPacket;
import com.packetscope.desktop.persistence.PacketWriteQueue;
import com.packetscope.desktop.persistence.PacketPersistenceWorker;
import com.packetscope.desktop.DbConnection;

import java.sql.Connection;
import java.sql.SQLException;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class PrimaryController {

    @FXML private Button startButton;
    @FXML private ListView<CapturedPacket> packetListView; 
    @FXML private Label protectedLabel;
    
    private ObservablePacketStream packetStream;
    private PacketCaptureService packetCaptureService;
    private PacketPersistenceWorker worker;
    private boolean running = false;

    @FXML
    public void initialize() {
        startButton.setDisable(true);
    }

    public void setPacketStream(ObservablePacketStream packetStream) {
        this.packetStream = packetStream;
        packetListView.setItems(packetStream.getPackets());
        
        setupCaptureEngine();
        
        // Auto-scroll to top when new packets arrive
        packetStream.getPackets().addListener((ListChangeListener<CapturedPacket>) c -> {
            if (!packetStream.getPackets().isEmpty()) {
                packetListView.scrollTo(0);
            }
        });
    }

    private void setupCaptureEngine() {
        PacketWriteQueue writeQueue = new PacketWriteQueue(5000);
        packetCaptureService = new PacketCaptureService(packetStream, writeQueue);
        
        if (!packetCaptureService.isCaptureAvailable()) {
            showCaptureError();
            return;
        }

        startButton.setDisable(false);
        initializeDatabaseWorker(writeQueue);
    }

    private void initializeDatabaseWorker(PacketWriteQueue writeQueue) {
        try {
            Connection con = DbConnection.getInstance().getConnection();
            worker = new PacketPersistenceWorker(writeQueue.getQueue(), con);

            Thread workerThread = new Thread(worker, "packet-db-writer");
            workerThread.setDaemon(true);
            workerThread.start();
        } catch (SQLException ex) {
            System.err.println("Persistence Error: " + ex.getMessage());
        }
    }

    @FXML
    private void startCapture(ActionEvent e) {
        if (packetCaptureService == null) return;

        if (running) {
            stopCaptureSession();
        } else {
            startCaptureSession();
        }
    }

    private void startCaptureSession() {
        packetCaptureService.start();
        updateUIState(true);
        running = true;
    }
    
    private void stopCaptureSession() {
        packetCaptureService.stop();
        updateUIState(false);
        running = false;
    }

    private void updateUIState(boolean isRunning) {
        if (isRunning) {
            protectedLabel.setText("Protected");
            protectedLabel.setStyle("-fx-background-color: rgba(0, 225, 121, 0.51)");
            startButton.setText("STOP");
            startButton.setStyle("-fx-border-color: rgb(255,62,72)");
        } else {
            protectedLabel.setText("UnProtected");
            protectedLabel.setStyle("-fx-background-color: rgba(255,30,30,0.5)");
            startButton.setText("START");
            startButton.setStyle("-fx-border-color: rgb(28,115,232)");
        }
    }

    private void showCaptureError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Packet Capture Unavailable");
        alert.setHeaderText("Capture driver not detected");
                alert.setContentText(
            "PacketScope requires libpcap/Npcap to capture traffic.\n\n" +
            "Please install Wireshark or Npcap and restart the application."
        );
        alert.showAndWait();
    }
}