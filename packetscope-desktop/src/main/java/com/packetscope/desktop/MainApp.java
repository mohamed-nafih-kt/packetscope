package com.packetscope.desktop;

import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainApp extends Application {

    public static void main(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        File loginCss = null;
        Image image = null;
        try {
            loginCss = new File("src/main/resources/styles/login.css");
            image = new Image(getClass().getResourceAsStream("/images/packetscopeLogo.jpeg"));
        } catch (Exception ex) {
            System.out.println("error: " + ex.getMessage());
        }

        // Login Form Items
        StackPane toastContainer = new StackPane();
        toastContainer.setPadding(new Insets(20, 0, 0, 0));
        Label failedMsg = new Label("Incorrect Credentials");
        failedMsg.getStyleClass().add("error-message");
        failedMsg.setVisible(false);
        toastContainer.getChildren().add(failedMsg);

        Label titleLabel = new Label("PacketScope");
        titleLabel.getStyleClass().add("title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        Label usernameLabel = new Label("User ID");
        usernameLabel.getStyleClass().add("place-label");
        TextField userField = new TextField();
        userField.setPromptText("Enter User ID");
        userField.getStyleClass().add("input-field");

        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("place-label");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter Password");
        passField.getStyleClass().add("input-field");

        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("login-btn");

        HBox buttonWrapper = new HBox();
        buttonWrapper.getChildren().add(loginBtn);
        buttonWrapper.setMaxWidth(Double.MAX_VALUE);
        buttonWrapper.setAlignment(Pos.CENTER);

        //Login Form setup
        VBox loginForm = new VBox();
        loginForm.setPadding(new Insets(0, 40, 0, 40));
        loginForm.getChildren().add(toastContainer);
        loginForm.getChildren().add(titleLabel);
        loginForm.getChildren().add(usernameLabel);
        loginForm.getChildren().add(userField);
        loginForm.getChildren().add(passLabel);
        loginForm.getChildren().add(passField);
        loginForm.getChildren().add(buttonWrapper);
        loginForm.setMargin(buttonWrapper, new Insets(30, 0, 0, 0));

        Scene loginScene = new Scene(loginForm, 400, 720);
        loginScene.getStylesheets().add(loginCss.toURI().toString());

        // stage setup 
        stage.setResizable(false);
        stage.setScene(loginScene);
        stage.setTitle("PacketScope Desktop");
        stage.getIcons().add(image);
        stage.show();

        loginBtn.setOnAction(eh -> {
            String username = userField.getText();
            String password = passField.getText();

            boolean authenticated = authenticate(username, password);

            if (authenticated) {
                try {
                    Scene mainScene = loadMainInterface();
                    stage.setScene(mainScene);
                } catch (IOException ex) {
                    System.out.println("custom error: " + ex.getMessage());
                }
            } else {
                System.out.println("failed to authenticate");
                failedMsg.setVisible(true);
            }
        });

    }

    private Scene loadMainInterface() throws IOException {

        // resources
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/primary.fxml"));
        File cssFile = new File("src/main/resources/styles/primary.css");

        // scene setup
        Parent parent = loader.load();
        Scene mainScene = new Scene(parent, 400, 720);

        return mainScene;

    }

    private boolean authenticate(String username, String password) {

        return false;
    }

//    private void refresh(Scene scene) {
//        scene.setOnKeyPressed(event -> {
//            switch (event.getCode()) {
//                case F5 -> {
//                    try {
//                        System.out.println("Reloading scene and CSS...");
//                        Parent newRoot = FXMLLoader.load(getClass().getResource("/fxml/primary.fxml"));
//                        scene.setRoot(newRoot);
//                        loadStyles(scene);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//
//    }
    private void loadStyles(Scene scene) {
        scene.getStylesheets().clear();
        File cssFile = new File("src/main/resources/styles/primary.css");

        scene.getStylesheets().add(cssFile.toURI().toString());
        scene.getRoot().applyCss();
        scene.getRoot().layout();
    }

}
