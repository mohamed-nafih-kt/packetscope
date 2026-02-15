package com.packetscope.desktop.controller;

import com.packetscope.desktop.service.LoginService;
import com.packetscope.desktop.service.ObservablePacketStream;
import java.io.IOException;
import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Duration;
import com.packetscope.desktop.view.LoginView;

public class LoginController {

    private Stage stage;
    private LoginView view;

    public LoginController(Stage stage) {
        this.stage = stage;
        this.view = new LoginView();
        attachHandlers();
        stage.setScene(view.getLoginScene());
        stage.show();
    }

    private void attachHandlers() {       
        view.getLoginBtn().setOnAction(ae -> {
            String user = view.getUserField().getText();
            String pass = view.getPassField().getText();
            
            LoginService lc = new LoginService();
            boolean authenticated = lc.validateUser(user, pass);

            if (authenticated) {
                view.getToastMsg().setVisible(false);
                try {
                    loadMainInterface();
                } catch (Exception ex) {
                    System.err.println("load interface error: " + ex.getMessage());
                }
            } else {
                System.err.println("failed to authenticate");
                view.getToastMsg().setVisible(true);
                PauseTransition delay = new PauseTransition(Duration.seconds(3));
                delay.setOnFinished(e -> {
                    view.getToastMsg().setVisible(false);
                });
                delay.play();
            }
        });
    }
    
    private void loadMainInterface(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/primary.fxml"));
            Parent root = loader.load();
            
            ObservablePacketStream packetStream = new ObservablePacketStream();
            PrimaryController controller = loader.getController();
            controller.setPacketStream(packetStream);
            
            Scene mainScene = new Scene(root);
            stage.setScene(mainScene);
            stage.centerOnScreen();
            
        } catch (IOException ex) {
            System.err.println("Critical Error: Unable to load main UI layout. " + ex.getMessage());
        }
    }

}
