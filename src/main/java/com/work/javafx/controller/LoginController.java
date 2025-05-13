package com.work.javafx.controller;

import com.almasb.fxgl.core.util.Platform;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private boolean togglestate = false;
    private boolean togglestate1 = false;

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
//        //缺省登录用户名和密码
//        usernameField.setText("student");
//        passwordField.setText("student123");
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
<<<<<<< Updated upstream
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
=======
private  void fetchCurrentTerm(){
    NetworkUtils.get("/term/getCurrentTerm", new NetworkUtils.Callback<String>() {
        @Override
        public void onSuccess(String result) throws IOException {
            JsonObject res = gson.fromJson(result, JsonObject.class);
            if(res.has("code")&& res.get("code").getAsInt()==200){
                System.out.println("chenggong");
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
        }
    });
}
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
                            fecthSemesters();//获取学期列表
=======
                            fecthSemesters();//获取学期
>>>>>>> Stashed changes
                            fetchCurrentTerm();//获取当前学期
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
            // 如果没有错误标签，则使用对话框显示错误
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
            MainApplication.showMainView();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("无法加载主界面");
        }
    }

    public void handleClick(ActionEvent actionEvent) {
        if (togglestate) {
            usernameField.setPromptText("请输入学号或工号");
            passwordField.setPromptText("请输入密码");
            adminLogin.setText("教工或管理员登录");
            togglestate = false;
        } else {
            usernameField.setPromptText("请输入管理员账号");
            passwordField.setPromptText("请输入管理员密码");
            adminLogin.setText("学生登录");
            togglestate = true;
        }
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
//测试用快捷登录

    public void studentlogin(ActionEvent actionEvent) {
        usernameField.setText("202400000001");
        passwordField.setText("123456");
        handleLogin(actionEvent);
    }

    public void teacherlogin(ActionEvent actionEvent) {
        usernameField.setText("2401");
        passwordField.setText("123456");
        handleLogin(actionEvent);

    }

    public void adminlogin(ActionEvent actionEvent) {
        usernameField.setText("1");
        passwordField.setText("123456");
        handleLogin(actionEvent);

    }

}
