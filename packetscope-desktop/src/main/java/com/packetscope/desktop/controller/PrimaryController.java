package com.packetscope.desktop.controller;

import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
    Label protectedLabel;

    @FXML
    ListView capturedList;

    private boolean running = false;
    private Timeline timeline;
    int MAX_ITEMS = 100;

    // caoture packets
    @FXML
    private void startCapture(ActionEvent e) {
        if (running) {
            startButton.setStyle("-fx-border-color: rgb(28,115,232)");
            startButton.setText("START");

            protectedLabel.setText("UnProtected");
            protectedLabel.setStyle("-fx-background-color: rgba(255,30,30,0.5)");

            stopCaptureLoop();
            running = false;
        } else {

            protectedLabel.setText("Protected");
            protectedLabel.setStyle("-fx-background-color: rgba(0, 225, 121, 0.51)");
            protectedLabel.setVisible(true);

            startButton.setStyle("-fx-border-color: rgb(255,62,72)");
            startButton.setText("STOP");

            startCaptureLoop();
            running = true;
        }
    }

    private void startCaptureLoop() {
        
    timeline = new Timeline(
        new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            
            int i= 0;
            @Override
            public void handle(ActionEvent e) {
                i++;
                capturedList.getItems().add(0,"new packet: 150" + i);
                
                //remove list items more than 1000
                if(capturedList.getItems().size() > 1000){
                    capturedList.getItems().remove(MAX_ITEMS, capturedList.getItems().size());
                }
            }
        })
    );
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
    }

    private void stopCaptureLoop() {
        if (timeline != null) {
            timeline.stop();
        }
    }
}
