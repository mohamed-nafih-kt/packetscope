package com.packetscope.desktop.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class PrimaryController {

    @FXML
    private void handleCapture() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("capture");
        alert.setHeaderText(null);
        alert.setContentText("Starting packet capture...");
        alert.showAndWait();
    }

}
