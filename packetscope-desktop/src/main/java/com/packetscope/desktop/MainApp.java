package com.packetscope.desktop;

import com.packetscope.desktop.controller.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {

//        Image image = null;
//        try {
//            image = new Image(getClass().getResourceAsStream("/images/packetscopeLogo.jpeg"));
//        } catch (Exception ex) {
//            System.out.println("error: " + ex.getMessage());
//        }

        /* --- stage setup --- */
        stage.setResizable(false);
        stage.setTitle("PacketScope Desktop");
//        stage.getIcons().add(image);
        new LoginController(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }

    // --- archived for reference --- 
    /*
    private void refresh(Scene scene) {
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

    }
    private void loadStyles(Scene scene) {
        scene.getStylesheets().clear();
        File cssFile = new File("src/main/resources/styles/primary.css");

        scene.getStylesheets().add(cssFile.toURI().toString());
        scene.getRoot().applyCss();
        scene.getRoot().layout();
    }
     */
}
