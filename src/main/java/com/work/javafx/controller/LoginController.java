package com.work.javafx.controller;

import javafx.application.Platform;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.work.javafx.DataResponse.Res;
import com.work.javafx.MainApplication;
import com.work.javafx.entity.Data;
import com.work.javafx.entity.UserSession;
import com.work.javafx.model.term;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.StringUtil;
import com.work.javafx.util.ViewTransitionAnimation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoginController {

    Gson gson = new Gson();

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink adminLogin;
    @FXML
    private Hyperlink sduLogin;

    @FXML
    private Label errorMessageLabel;

    @FXML
    private Label versionLabel;

    private boolean togglestate = false;
    private boolean togglestate1 = false;
    private Thread pollingThread;
    private final AtomicBoolean isPolling = new AtomicBoolean(false);

    /**
     * 初始化控制器
     */
    @FXML
    public void initialize() {
        // 隐藏错误消息标签
        if (errorMessageLabel != null) {
            errorMessageLabel.setVisible(false);
        }

        // 为登录按钮添加事件处理
        if (loginButton != null) {
            loginButton.setOnAction(this::handleLogin);
        }
        //管理员登陆按钮
        if (adminLogin != null) {

        }

        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.DOWN) {
                passwordField.requestFocus();
            }
        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String username = usernameField.getText();
                String password = passwordField.getText();

                // 验证用户名和密码是否为空
                if (StringUtil.isEmpty(username) || StringUtil.isEmpty(password)) {
                    showErrorMessage("用户名和密码不能为空");
                    return;
                }
                // 用户验证逻辑
                authenticateUser(username, password);
            }
        });
        versionLabel.setText("Version: " + getAppVersion());
    }

    /**
     * 处理登录按钮点击事件
     *
     * @param event 事件对象
     */
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // 验证用户名和密码是否为空
        if (StringUtil.isEmpty(username) || StringUtil.isEmpty(password)) {
            showErrorMessage("用户名和密码不能为空");
            return;
        }
        // 用户验证逻辑
        authenticateUser(username, password);

    }

    /**
     * 加载学期列表
     */
    private void fecthSemesters() {
        NetworkUtils.get("/term/getTermList", new NetworkUtils.Callback<String>() {
            ObservableList<String> semesterList = FXCollections.observableArrayList();

            @Override
            public void onSuccess(String result) {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    JsonArray dataArray = res.getAsJsonArray("data");
                    List<String> loadedSemesters = new ArrayList<>();
                    for (int i = 0; i < dataArray.size(); i++) {
                        loadedSemesters.add(dataArray.get(i).getAsJsonObject().get("term").getAsString());
                    }
                    if (semesterList != null) {
                        semesterList.clear();
                    }
                    semesterList.addAll(loadedSemesters);
                    Data.getInstance().setSemesterList(semesterList);
                } else {
                    System.out.println("加载学期列表失败: " + (res.has("msg") ? res.get("msg").getAsString() : "未知错误"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("加载学期列表错误");
                e.printStackTrace();
            }
        });
    }
    /**
     * 获取当前学期
     * */
    private  void fetchCurrentTerm(){
        NetworkUtils.get("/term/getCurrentTerm", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) throws IOException {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if(res.has("code")&& res.get("code").getAsInt()==200){
                    String currentTerm = res.get("data").getAsString();
                    System.out.println(currentTerm);
                    Data.getInstance().setCurrentTerm(currentTerm);
                    System.out.println(res.get("msg").getAsString());
                }else {
                    System.err.println(res.get("msg").getAsString());
                }
            }

            @Override
            public void onFailure(Exception e) {
                JsonObject res = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")), JsonObject.class);
                System.err.println(res.get("msg").getAsString());
                System.out.println("错误");
            }
        });
    }
    /**
     * 获取教室列表
     */
    private void fetchClassRoom(){
        NetworkUtils.get("/Teacher/getClassRoom", new NetworkUtils.Callback<String>() {
            ObservableList<String> classRoomList = FXCollections.observableArrayList();
            @Override
            public void onSuccess(String result) throws IOException {
                JsonObject  res=  gson.fromJson(result, JsonObject.class);
                if(res.get("code").getAsInt() == 200){
                    List<String> Roomlist = new ArrayList<>();
                    JsonArray data = res.getAsJsonArray("data");
                    for (int i = 0; i < data.size(); i++) {
                        Roomlist.add(data.get(i).getAsJsonObject().get("location").getAsString());
                    }
                    if(classRoomList != null) {
                        classRoomList.clear();
                    }
                    assert classRoomList != null;
                    classRoomList.addAll(Roomlist);
                    Data.getInstance().setClassRoomList(classRoomList);
                }
            }

            @Override
            public void onFailure(Exception e) {
                JsonObject res =gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")),JsonObject.class);
                System.err.println(res.get("msg").getAsString());
            }
        });
    }
    /**
     * 验证用户凭据
     *
     * @param username 用户名
     * @param password 密码
     * @return 验证是否成功
     */
    private boolean authenticateUser(String username, String password) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("stuId", username);
        requestBody.put("password", password);
        String requetBodyJson = gson.toJson(requestBody);
        if(togglestate1){
            NetworkUtils.post("/login/SDULogin", requetBodyJson, new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JsonObject responseJson = gson.fromJson(result, JsonObject.class);
                        if (responseJson.has("code") && responseJson.get("code").getAsInt() == 200) {
                            JsonObject dataJson = responseJson.getAsJsonObject("data");
                            int identity = dataJson.get("permission").getAsInt();
                            String token = dataJson.get("accessToken").getAsString();
                            String username = dataJson.get("username").getAsString();
                            String refreshToken = dataJson.get("refreshToken").getAsString();
                            UserSession.getInstance().setIdentity(identity);
                            UserSession.getInstance().setToken(token);
                            UserSession.getInstance().setRefreshToken(refreshToken);
                            UserSession.getInstance().setUsername(username);
                            fecthSemesters();//获取学期列表
                            fetchCurrentTerm();//获取当前学期
                            if(identity != 2){
                                fetchClassRoom();
                            }//获取教室列表
                            System.out.println("登录成功: " + result);
                            MainApplication.startTokenRefreshTimer();
                            navigateToMainPage(); // 导航到主页面
                        } else {
                            String message = responseJson.has("msg") ? responseJson.get("msg").getAsString() : "用户名或密码错误";
                            showErrorMessage(message);
                        }
                    } catch (Exception e) {
                        JsonObject responseJson = gson.fromJson(result, JsonObject.class);
                        showErrorMessage(responseJson.get("msg").getAsString());
                        System.err.println("处理登录响应时出错: " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    System.err.println("登录失败: " + e.getMessage());
                    int i = e.getMessage().indexOf("msg");
                    showErrorMessage(e.getMessage().substring(i + 6, e.getMessage().length() - 2));
                }
            });
        } else {
            NetworkUtils.post("/login/simpleLogin", requetBodyJson, new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JsonObject responseJson = gson.fromJson(result, JsonObject.class);
                        if (responseJson.has("code") && responseJson.get("code").getAsInt() == 200) {
                            JsonObject dataJson = responseJson.getAsJsonObject("data");
                            int identity = dataJson.get("permission").getAsInt();
                            String token = dataJson.get("accessToken").getAsString();
                            String username = dataJson.get("username").getAsString();
                            String refreshToken = dataJson.get("refreshToken").getAsString();
                            UserSession.getInstance().setIdentity(identity);
                            UserSession.getInstance().setToken(token);
                            UserSession.getInstance().setRefreshToken(refreshToken);
                            UserSession.getInstance().setUsername(username);
                            fecthSemesters();//获取学期列表
                            fetchCurrentTerm();//获取当前学期
                            if(identity != 2){
                                fetchClassRoom();
                            }//获取教室列表
                            System.out.println("登录成功: " + result);
                            MainApplication.startTokenRefreshTimer();
                            navigateToMainPage(); // 导航到主页面
                        } else {
                            String message = responseJson.has("msg") ? responseJson.get("msg").getAsString() : "用户名或密码错误";
                            showErrorMessage(message);
                        }
                    } catch (Exception e) {
                        JsonObject responseJson = gson.fromJson(result, JsonObject.class);
                        showErrorMessage(responseJson.get("msg").getAsString());
                        System.err.println("处理登录响应时出错: " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                   JsonObject res = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")), JsonObject.class);
                    showErrorMessage(res.get("msg").getAsString());
                }
            });
        }

        return false;
    }

    /**
     * 显示错误消息
     *
     * @param message 错误消息内容
     */
    private void showErrorMessage(String message) {
        if (errorMessageLabel != null) {
            errorMessageLabel.setText(message);
            errorMessageLabel.setVisible(true);
        } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("登录错误");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    /**
     * 导航到主界面
     */
    private void navigateToMainPage() {
        try {
            // 获取当前场景
            Scene currentScene = loginButton.getScene();
            
            // 获取当前场景的样式表
            ObservableList<String> currentStylesheets = currentScene.getStylesheets();
            
            // 保存当前根节点，用于执行动画
            Parent currentRoot = currentScene.getRoot();
            
            // 将当前根节点放入StackPane，便于执行动画
            StackPane container = new StackPane();
            container.getChildren().add(currentRoot);
            
            // 获取当前场景尺寸，确保动画过程中尺寸不变
            double sceneWidth = currentScene.getWidth();
            double sceneHeight = currentScene.getHeight();
            
            // 创建新场景，使用StackPane作为容器
            Scene newScene = new Scene(container, sceneWidth, sceneHeight);
            
            // 应用原场景的样式表
            if (currentStylesheets != null && !currentStylesheets.isEmpty()) {
                newScene.getStylesheets().addAll(currentStylesheets);
            }
            
            // 获取当前舞台
            Stage stage = (Stage) currentScene.getWindow();
            
            // 设置新场景
            stage.setScene(newScene);
            
            // 预加载主界面，但还不显示
            FXMLLoader loader = MainApplication.getMainViewLoader();
            Parent mainView = loader.load();
            
            // 获取对应的CSS路径
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
            }

            // 预先应用样式
            if (cssPath != null) {
                String fullCssPath = Objects.requireNonNull(
                        getClass().getResource(cssPath)).toExternalForm();
                if (!mainView.getStylesheets().contains(fullCssPath)) {
                    mainView.getStylesheets().add(fullCssPath);
                }
                
                // 检查内嵌的样式表是否被正确应用
                FXMLLoader contentLoader = null;
                Parent contentView = null;
                
                try {
                    // 尝试获取基础视图中的内容区域，并确保其样式也被加载
                    if (loader.getController() != null) {
                        // 这里针对不同的控制器类型，获取内容区域
                        // 这需要根据你的代码结构调整
                        if (UserSession.getInstance().getIdentity() == 0) {
                            // 如果是管理员视图
                            contentLoader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/AdminHomePage.fxml"));
                            contentView = contentLoader.load();
                        } else if (UserSession.getInstance().getIdentity() == 1) {
                            // 如果是教师视图
                            contentLoader = new FXMLLoader(getClass().getResource("/com/work/javafx/teacher/TeacherHomePage.fxml"));
                            contentView = contentLoader.load();
                        } else if (UserSession.getInstance().getIdentity() == 2) {
                            // 如果是学生视图
                            contentLoader = new FXMLLoader(getClass().getResource("/com/work/javafx/student/StudentHomePage.fxml"));
                            contentView = contentLoader.load();
                        }
                        
                        // 检查并应用内容视图的样式
                        if (contentView != null) {
                            mainView.getProperties().put("contentView", contentView);
                        }
                    }
                } catch (Exception ex) {
                    // 忽略错误，让主视图继续加载
                    System.out.println("预加载内容视图时出错: " + ex.getMessage());
                }
            }
            
            // 为主视图设置初始状态
            mainView.setOpacity(0);
            mainView.setScaleX(1.05);
            mainView.setScaleY(1.05);
            
            // 创建淡出动画
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setInterpolator(Interpolator.EASE_OUT);
            
            // 创建淡入动画
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), mainView);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.setInterpolator(Interpolator.EASE_IN);
            
            // 为当前视图添加缩小动画
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(300), currentRoot);
            scaleOut.setToX(0.95);
            scaleOut.setToY(0.95);
            scaleOut.setInterpolator(Interpolator.EASE_OUT);
            
            // 为主视图添加放大动画
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), mainView);
            scaleIn.setFromX(1.05);
            scaleIn.setFromY(1.05);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            scaleIn.setInterpolator(Interpolator.EASE_OUT);
            
            // 创建并行动画组
            ParallelTransition fadeOutTransition = new ParallelTransition(fadeOut, scaleOut);
            ParallelTransition fadeInTransition = new ParallelTransition(fadeIn, scaleIn);
            
            // 设置动画完成后的操作
            fadeOutTransition.setOnFinished(event -> {
                // 移除登录视图，添加主视图
                container.getChildren().clear();
                container.getChildren().add(mainView);
                
                // 开始淡入动画
                fadeInTransition.play();
            });
            
            // 淡入动画完成后
            fadeInTransition.setOnFinished(event -> {
                // 完成动画后，设置正常的主界面（由MainApplication处理后续逻辑）
                try {
                    // 从当前容器中移除节点，避免节点重用错误
                    container.getChildren().clear();
                    
                    // 重新加载主视图，而不是重用已经在场景图中的节点
                    FXMLLoader newLoader = MainApplication.getMainViewLoader();
                    Parent newMainView = newLoader.load();
                    
                    // 使用新加载的视图完成转场
                    MainApplication.completeMainViewTransition(newMainView, newLoader);
                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorMessage("无法完成主界面跳转");
                }
            });
            
            // 开始淡出动画
            fadeOutTransition.play();
            
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("无法加载主界面");
        }
    }
    /**
     * 生成设备码，用于第三方登录认证
     * @return 设备码
     */
        public static String generateDeviceId() {
            // 生成一个随机UUID，截取其前12位字符
            String randomPart = UUID.randomUUID().toString().substring(0, 12);

            //设备用户名首位
            String osInfo = System.getProperty("user.name").substring(0, 1);

            // 添加时间戳的最后5位
            String timePart = String.valueOf(System.currentTimeMillis()).substring(8);

            // 组合成最终设备码
            return osInfo + randomPart + timePart;
    }

    public void handleClick(ActionEvent actionEvent) {
        String url = "http://110.42.38.155:8081/login/toLogin?deviceId=";
        String deviceId = generateDeviceId();
        String fullUrl = url + deviceId;
        try {
            // 使用默认浏览器打开URL
            Platform.runLater(() -> {
                try {
                    java.awt.Desktop.getDesktop().browse(java.net.URI.create(fullUrl));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            
            // 启动轮询线程，检查OAuth登录状态
            startPolling(deviceId);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("无法打开浏览器");
        }
    }

    /**
     * 启动轮询，检查第三方登录状态
     * @param deviceId 设备ID
     */
    private void startPolling(String deviceId) {
        if (isPolling.get()) {
            isPolling.set(false); // 终止旧线程
            if (pollingThread != null && pollingThread.isAlive()) {
                pollingThread.interrupt();
            }
        }

        isPolling.set(true);
        Thread pollingThread = new Thread(() -> {
            int attempts = 0;
            final int MAX_ATTEMPTS = 600; // 最多轮询600次
            
            while (isPolling.get() && attempts < MAX_ATTEMPTS) {
                try {
                    Thread.sleep(1000); // 每1秒轮询一次
                    
                    // 如果已经停止轮询，则退出循环
                    if (!isPolling.get()) {
                        break;
                    }
                    
                    Map<String, String> params = new HashMap<>();
                    params.put("state", deviceId);
                    
                    // 使用final变量传递给匿名内部类
                    final int currentAttempt = attempts;
                    
                    NetworkUtils.get("/login/getOAuthToken?state=" + deviceId, new NetworkUtils.Callback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            // 如果已经停止轮询，直接返回
                            if (!isPolling.get()) {
                                return;
                            }
                            
                            JsonObject responseJson = gson.fromJson(result, JsonObject.class);
                            if (responseJson.has("code") && responseJson.get("code").getAsInt() == 200) {
                                // 立即停止轮询
                                isPolling.set(false);
                                // 检查data是否存在且不是null
                                if (responseJson.has("data") && !responseJson.get("data").isJsonNull()) {
                                    JsonObject dataJson = responseJson.getAsJsonObject("data");
                                    int identity = dataJson.get("permission").getAsInt();
                                    String token = dataJson.get("accessToken").getAsString();
                                    String username = dataJson.get("username").getAsString();
                                    String refreshToken = dataJson.get("refreshToken").getAsString();
                                    
                                    UserSession.getInstance().setIdentity(identity);
                                    UserSession.getInstance().setToken(token);
                                    UserSession.getInstance().setRefreshToken(refreshToken);
                                    UserSession.getInstance().setUsername(username);
                                    
                                    fecthSemesters();
                                    fetchCurrentTerm();
                                    if(identity != 2){
                                        fetchClassRoom();
                                    }

                                    MainApplication.startTokenRefreshTimer();
                                    
                                    Platform.runLater(() -> {
                                        try {
                                            navigateToMainPage();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                                    
                                    System.out.println("登录成功");
                                } else {
                                    //code虽然为200，但是data为null
                                    System.out.println("等待用户在浏览器中完成授权...");
                                    isPolling.set(true);
                                }
                            } else {
                                // 第一次请求时data可能为null，继续轮询
                                System.out.println("等待第三方登录验证...");
                                isPolling.set(true); // 继续轮询
                            }
                        }
                        
                        @Override
                        public void onFailure(Exception e) {
                            // 轮询失败不做特殊处理，继续下一次轮询
                            if (currentAttempt % 10 == 0) {
                                System.out.println("轮询等待中...已尝试" + currentAttempt + "次");
                            }
                        }
                    });
                    attempts++;
                } catch (InterruptedException e) {
                    isPolling.set(false);
                    break;
                }
            }
            
            if (attempts >= MAX_ATTEMPTS && isPolling.get()) {
                // 超时处理
                isPolling.set(false);
                Platform.runLater(() -> showErrorMessage("登录超时，请重试"));
            }
        });
        
        pollingThread.setDaemon(true); // 设为守护线程，应用退出时自动结束
        pollingThread.start();
    }

    public void handleSduloginClick(ActionEvent actionEvent) {
        if (togglestate1) {
            usernameField.setPromptText("请输入学号或工号");
            passwordField.setPromptText("请输入密码");
            sduLogin.setText("山大统一认证登录");
            togglestate1 = false;
        } else {
            usernameField.setPromptText("请输入山大账号");
            passwordField.setPromptText("请输入密码");
            sduLogin.setText("普通登录");
            togglestate1 = true;
        }
    }

    private String getAppVersion() {
        try (InputStream in = getClass().getResourceAsStream("/version.properties")) {
            Properties props = new Properties();
            props.load(in);
            return props.getProperty("app.version", "unknown");
        } catch (Exception e) {
            return "unknown";
        }
    }
//测试用快捷登录

    public void studentlogin(ActionEvent actionEvent) {
        usernameField.setText("202400000001");
        passwordField.setText("123456");
        handleLogin(actionEvent);
    }

    public void teacherlogin(ActionEvent actionEvent) {
        usernameField.setText("190100000000");
        passwordField.setText("123456");
        handleLogin(actionEvent);

    }
    public void adminlogin(ActionEvent actionEvent) {
        usernameField.setText("1");
        passwordField.setText("123456");
        handleLogin(actionEvent);

    }

}
