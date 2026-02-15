package com.packetscope.desktop;

import com.packetscope.desktop.controller.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point for the PacketScope Desktop application.
 * Initializes the primary stage and hands over control to the LoginController.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setResizable(false);
        stage.setTitle("PacketScope Desktop");
        
        // Initialize the login sequence
        LoginController loginController = new LoginController(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
