package com.work.javafx;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.Refresh;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

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
        InputStream iconStream = getClass().getResourceAsStream("/icons/favicon.png");
        if (iconStream == null) {
            System.err.println("图标加载失败：资源未找到！");
        } else {
            stage.getIcons().add(new Image(iconStream));
        }

        // 初始加载登录页面
        changeView("Login.fxml","css/Login.css");
        stage.show();

    }
    @Override
    public void stop() {
        stopTokenRefreshTimer(); // 停止定时器
        UserSession.getInstance().clearSession(); // 清理数据
        System.out.println("应用关闭，用户数据已清除");
        NetworkUtils.shutdown();
        Platform.exit();
        System.exit(0);
    }
    /**
     * 切换视图方法
     * 用于切换整个场景的视图
     */
    public static void changeView(String fxml, String css) throws IOException {

        Parent root = null;
        try {
            // 保存当前窗口尺寸
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            // 加载新视图
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(fxml));
            root = loader.load();
            
            // 创建新场景
            Scene scene = new Scene(root);
            
            // 如果有CSS样式表，则添加
            if (css != null && !css.isEmpty()) {
                scene.getStylesheets().add(
                        Objects.requireNonNull(MainApplication.class.getResource(css)).toExternalForm());
            }
            
            // 如果加载的是基础视图，将控制器实例保存到场景的userData中，使内容控制器可以访问
            if (fxml.equals("StudentBaseView.fxml")||fxml.equals("Teacher/TeacherBaseView.fxml")||fxml.equals("AdminBaseView.fxml")) {
                scene.setUserData(loader.getController());
            }
            
            // 设置新场景
            stage.setScene(scene);
            
            // 恢复窗口尺寸
            stage.setWidth(currentWidth);
            stage.setHeight(currentHeight);
            
        } catch (IOException e){
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 登录成功后切换到主界面
     * 使用基础视图作为主框架
     */
    public static void showMainView() throws IOException {
        switch (UserSession.getInstance().getIdentity()){
            case 2:
                changeView("student/StudentBaseView.fxml", "css/student/BaseView.css");
                break;
            case 1:
                changeView("teacher/TeacherBaseView.fxml", "css/teacher/TeacherBaseView.css");
                break;
            case 0:
                changeView("admin/AdminBaseView.fxml", "css/admin/AdminBaseView.css");
                break;
            default:

        }



    }
    /**
     * 定时刷新token
     */
    private static Timer tokenRefreshTimer;

    public static void startTokenRefreshTimer() {
        // 如果已有定时器，先取消
        stopTokenRefreshTimer();

        tokenRefreshTimer = new Timer();
        tokenRefreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Refresh.refreshtoken();
            }
        }, 0, 29* 60 * 1000); // 每29分钟刷新一次
    }

    public static void stopTokenRefreshTimer() {
        if (tokenRefreshTimer != null) {
            tokenRefreshTimer.cancel();
            tokenRefreshTimer = null;
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}