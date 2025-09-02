package com.packetscope.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.text.*;



public class MainApp extends Application{
    @Override
    public void start(Stage stage) throws Exception{
        
        // resources
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/primary.fxml"));        
        Image image = new Image(getClass().getResourceAsStream("/images/packetscopeLogo.jpeg"));
        
        // scene setup
        Parent parent = loader.load();
        Group root = new Group();
        Scene scene = new Scene(parent,400,720);
       
        // stage setup
        stage.setResizable(false); 
        stage.setScene(scene);
        stage.setTitle("PacketScope Desktop");
        stage.getIcons().add(image);
        stage.show();   
    }
    
    public static void main(String args[]){
        launch();
    }
}
