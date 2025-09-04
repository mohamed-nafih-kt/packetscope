package com.packetscope.desktop.controller;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

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
    Label protectedLabel ;

    private boolean running = false;

    @FXML
    private void startCapture(ActionEvent e) {
        if (running) {
            startButton.setStyle("-fx-border-color: rgb(28,115,232)");
            startButton.setText("START");

            protectedLabel.setText("UnProtected");
            protectedLabel.setStyle("-fx-background-color: rgb(255,30,30,0.5)");
            
            running = false;
        } else {
                      
            protectedLabel.setText("Protected");
            protectedLabel.setStyle("-fx-background-color: rgba(0, 225, 121, 0.51)");
            protectedLabel.setVisible(true);
            
            startButton.setStyle("-fx-border-color: rgb(255,62,72)");
            startButton.setText("STOP");

            running = true;
        }
    }

}
