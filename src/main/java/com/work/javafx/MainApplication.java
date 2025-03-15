package com.work.javafx;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {
    private static  Stage stage;
    @Override
    public void start(Stage stage) throws Exception {
        MainApplication.stage = stage;
        stage.setTitle("教务管理系统");
        changeView("Login.fxml","css/Login.css");
        stage.show();
    }
    public static void  changeView(String fxml,String css) throws IOException {
        Parent root = null;
        try {
            root = FXMLLoader.load(Objects.requireNonNull(MainApplication.class.getResource((fxml))));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(MainApplication.class.getResource(css)).toExternalForm());
            stage.setScene(scene);
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}