package com.work.javafx;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.AutoUpdater;
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
        AutoUpdater.checkAndUpdate(stage);
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

    /**
     * 获取主视图加载器，用于预加载主视图
     * @return FXMLLoader 根据用户权限返回相应的加载器
     */
    public static FXMLLoader getMainViewLoader() {
        FXMLLoader loader = null;
        switch (UserSession.getInstance().getIdentity()) {
            case 2:
                loader = new FXMLLoader(MainApplication.class.getResource("student/StudentBaseView.fxml"));
                break;
            case 1:
                loader = new FXMLLoader(MainApplication.class.getResource("teacher/TeacherBaseView.fxml"));
                break;
            case 0:
                loader = new FXMLLoader(MainApplication.class.getResource("admin/AdminBaseView.fxml"));
                break;
            default:
                break;
        }
        return loader;
    }
    
    /**
     * 完成主视图转场后的设置
     * @param mainView 已加载的主视图根节点
     * @param loader 已使用的加载器
     */
    public static void completeMainViewTransition(Parent mainView, FXMLLoader loader) throws IOException {
        // 确保节点未被添加到任何场景图中
        if (mainView.getScene() != null) {
            System.out.println("警告：节点已在场景图中，可能导致问题");
        }

        // 创建新场景并应用CSS
        Scene scene = new Scene(mainView, APP_WIDTH, APP_HEIGHT);
        
        // 如果主视图已经有样式表，保留它们
        if (!mainView.getStylesheets().isEmpty()) {
            scene.getStylesheets().addAll(mainView.getStylesheets());
        }
        
        // 获取相应的CSS
        String cssPath = null;
        switch (UserSession.getInstance().getIdentity()) {
            case 2:
                cssPath = "/com/work/javafx/css/student/BaseView.css";
                break;
            case 1:
                cssPath = "/com/work/javafx/css/teacher/TeacherBaseView.css";
                break;
            case 0:
                cssPath = "/com/work/javafx/css/admin/AdminBaseView.css";
                break;
            default:
                break;
        }
        
        // 始终添加基础视图的样式表，确保基本样式正确加载
        if (cssPath != null) {
            String cssUrl = Objects.requireNonNull(MainApplication.class.getResource(cssPath)).toExternalForm();
            if (!scene.getStylesheets().contains(cssUrl)) {
                scene.getStylesheets().add(cssUrl);
            }
        }
        
        // 如果是基础视图，将控制器实例保存到场景的userData中
        scene.setUserData(loader.getController());
        
        // 设置新场景
        stage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}