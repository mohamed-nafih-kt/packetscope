package com.packetscope.desktop;

import com.packetscope.desktop.controller.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        /* --- stage setup --- */
        stage.setResizable(false);
        stage.setTitle("PacketScope Desktop");
        new LoginController(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
