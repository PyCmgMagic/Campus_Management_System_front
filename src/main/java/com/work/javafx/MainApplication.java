package com.work.javafx;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {
    private static Stage stage;
    // 定义应用统一窗口尺寸
    private static final double APP_WIDTH = 900.0;
    private static final double APP_HEIGHT = 600.0;
    
    @Override
    public void start(Stage stage) throws Exception {
        MainApplication.stage = stage;
        stage.setTitle("教务管理系统");
        
        // 设置窗口固定尺寸
        stage.setWidth(APP_WIDTH);
        stage.setHeight(APP_HEIGHT);
        stage.setMinWidth(APP_WIDTH);
        stage.setMinHeight(APP_HEIGHT);
        
        changeView("Login.fxml","css/Login.css");
        stage.show();
    }
    
    public static void changeView(String fxml, String css) throws IOException {
        Parent root = null;
        try {
            // 保存当前窗口尺寸
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            root = FXMLLoader.load(Objects.requireNonNull(MainApplication.class.getResource((fxml))));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(MainApplication.class.getResource(css)).toExternalForm());
            stage.setScene(scene);
            
            // 恢复窗口尺寸
            stage.setWidth(currentWidth);
            stage.setHeight(currentHeight);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}