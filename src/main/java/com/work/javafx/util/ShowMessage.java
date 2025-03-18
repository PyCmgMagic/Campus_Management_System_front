package com.work.javafx.util;

import javafx.scene.control.Alert;

public class ShowMessage {

    public static void showErrorMessage(String message, String errorTitle) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(errorTitle);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
    }
    
    public static void showInfoMessage(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
    }
    
    public static void showWarningMessage(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
    }
}
