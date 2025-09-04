package com.packetscope.desktop;

import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.text.*;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // resources
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/primary.fxml"));
        Image image = new Image(getClass().getResourceAsStream("/images/packetscopeLogo.jpeg"));

        // scene setup
        Parent parent = loader.load();
        Scene scene = new Scene(parent, 400, 720);

        loadStyles(scene);

        // Refresh
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case F5 -> {
                    try {
                        System.out.println("Reloading scene and CSS...");
                        Parent newRoot = FXMLLoader.load(getClass().getResource("/fxml/primary.fxml"));
                        scene.setRoot(newRoot);
                        loadStyles(scene);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // stage setup 
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle("PacketScope Desktop");
        stage.getIcons().add(image);
        stage.show();
    }

    private void loadStyles(Scene scene) {
        scene.getStylesheets().clear();
        File cssFile = new File("src/main/resources/styles/primary.css");
        scene.getStylesheets().add(cssFile.toURI().toString());

        scene.getRoot().applyCss();
        scene.getRoot().layout();
    }

    public static void main(String args[]) {
        launch();
    }
}
