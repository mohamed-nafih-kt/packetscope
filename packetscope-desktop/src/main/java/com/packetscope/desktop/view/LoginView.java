package com.packetscope.desktop.view;

import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginView {

    private Scene loginScene;
    private TextField userField;
    private PasswordField passField;
    private Button loginBtn;
    private Label toastMsg;

    public LoginView() {

        File loginCss = null;
        try {
            loginCss = new File("src/main/resources/styles/login.css");
        } catch (Exception ex) {
            System.out.println("error: " + ex.getMessage());
        }

        // Login Form Items
        StackPane toastContainer = new StackPane();
        toastContainer.setPadding(new Insets(20, 0, 0, 0));
        toastMsg = new Label("Incorrect Credentials");
        toastMsg.getStyleClass().add("error-message");
        toastMsg.setVisible(false);
        toastContainer.getChildren().add(toastMsg);

        Label titleLabel = new Label("PacketScope");
        titleLabel.getStyleClass().add("title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        Label usernameLabel = new Label("User ID");
        usernameLabel.getStyleClass().add("place-label");
        userField = new TextField();
        userField.setPromptText("Enter User ID");
        userField.getStyleClass().add("input-field");

        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("place-label");
        passField = new PasswordField();
        passField.setPromptText("Enter Password");
        passField.getStyleClass().add("input-field");

        loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("login-btn");

        HBox buttonWrapper = new HBox();
        buttonWrapper.getChildren().add(loginBtn);
        buttonWrapper.setMaxWidth(Double.MAX_VALUE);
        buttonWrapper.setAlignment(Pos.CENTER);

        //Login Form setup
        VBox root = new VBox();
        root.setPadding(new Insets(0, 40, 0, 40));
        root.getChildren().add(toastContainer);
        root.getChildren().add(titleLabel);
        root.getChildren().add(usernameLabel);
        root.getChildren().add(userField);
        root.getChildren().add(passLabel);
        root.getChildren().add(passField);
        root.getChildren().add(buttonWrapper);
        root.setMargin(buttonWrapper, new Insets(30, 0, 0, 0));

        loginScene = new Scene(root, 400, 720);
        loginScene.getStylesheets().add(loginCss.toURI().toString());

    }

    public Scene getLoginScene() {
        return loginScene;
    }

    public TextField getUserField() {
        return userField;
    }

    public PasswordField getPassField() {
        return passField;
    }

    public Button getLoginBtn() {
        return loginBtn;
    }

    public Label getToastMsg() {
        return toastMsg;
    }

}
