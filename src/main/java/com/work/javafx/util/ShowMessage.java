package com.work.javafx.util;

import javafx.scene.control.Alert;

public class ShowMessage {

    public static void showErrorMessage(String message,String errorTitle) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(errorTitle);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
    }
}
