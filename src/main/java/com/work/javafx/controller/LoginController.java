package com.work.javafx.controller;

import com.almasb.fxgl.core.util.Platform;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.work.javafx.DataResponse.Res;
import com.work.javafx.MainApplication;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.StringUtil;
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
import java.util.HashMap;
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
    private Label errorMessageLabel;

    private boolean togglestate = false;
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
            if(event.getCode()== KeyCode.ENTER || event.getCode()==KeyCode.DOWN){
                passwordField.requestFocus();
            }
        });
        passwordField.setOnKeyPressed(event -> {
            if(event.getCode()== KeyCode.ENTER){
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
 * 处理管理员登录按钮点击事件
 * @param event 事件对象
 */

    /**
     * 验证用户凭据
     * @param username 用户名
     * @param password 密码
     * @return 验证是否成功
     */
    private boolean authenticateUser(String username, String password) {
        Map<String,String> requestBody = new HashMap<>();
        requestBody.put("stuId",username);
        requestBody.put("password",password);
        String requetBodyJson = gson.toJson(requestBody);
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
                        System.out.println("登录成功: " + result);
                         navigateToMainPage(); // 导航到主页面
                    } else {
                        String message = responseJson.has("message") ? responseJson.get("message").getAsString() : "用户名或密码错误";
                        showErrorMessage(message);
                    }
                } catch (Exception e) {
                    // 处理 JSON 解析错误或其他处理响应时的问题
                    System.err.println("处理登录响应时出错: " + e.getMessage());
                    showErrorMessage("处理登录响应时出错");
                }
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("登录失败: " + e.getMessage());
                showErrorMessage("处理登录响应时出错");
            }
        });
//        if (username.equals("admin") && password.equals("admin123")) {
//            UserSession.getInstance().setIdentity(-1);
//            return true;
//        };
//        if (username.equals("teacher") && password.equals("teacher123")) {
//            UserSession.getInstance().setIdentity(1);
//            return true;
//
//        }
//        if (username.equals("student") && password.equals("student123")) {
//            UserSession.getInstance().setIdentity(0);
//            return true;
//
//        }
        return false;
    }

    /**
     * 显示错误消息
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
        if(togglestate){
            usernameField.setPromptText("请输入学号或工号");
            passwordField.setPromptText("请输入密码");
            adminLogin.setText("教工或管理员登录");
            togglestate = false;
        }else {
            usernameField.setPromptText("请输入管理员账号");
            passwordField.setPromptText("请输入管理员密码");
            adminLogin.setText("学生登录");
            togglestate = true;
        }

    }
}