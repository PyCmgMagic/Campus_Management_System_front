package com.work.javafx.controller;

import com.google.gson.Gson;
import com.work.javafx.DataResponse.Res;
import com.work.javafx.MainApplication;
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
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

public class LoginController {

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
            adminLogin.setOnAction(this::handleAdminButtonClicked);
        }
        //缺省登录用户名和密码
        usernameField.setText("admin");
        passwordField.setText("admin123");
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
        if (authenticateUser(username, password)) {
//            //网络请求test ————post
//            Gson gson = new Gson();
//            class Body {
//                private String content ;
//                public String getContent() {
//                    return content;
//                }
//
//                public void setContent(String content) {
//                    this.content = content;
//                }
//            }
//            Body body = new Body();
//            body.setContent(username);
//            String json = gson.toJson(body);
//            String url = "https://uapis.cn/api/fanyi?text=" + username;
//            NetworkUtils.get(url, new NetworkUtils.Callback<String>() {
//                @Override
//                public void onSuccess(String result) {
//                    Res res = gson.fromJson(result,Res.class);
//                    System.out.println(res.getTranslate());
//                }
//                @Override
//                public void onFailure(Exception e) {
//                    System.out.println(e);
//                }
//            });
            //网络请求
//            NetworkUtils.get("https://uapis.cn/api/say", new NetworkUtils.Callback<String>() {
//                @Override
//                public void onSuccess(String result) {
//                    System.out.println(result);
//                    System.out.println("请求成功");
//                }
//
//                @Override
//                public void onFailure(Exception e) {
//                    System.out.println(e);
//                    System.out.println("请求失败");
//                }
//            });
//             登录成功，跳转到主界面
            navigateToMainPage();
        } else {
            // 登录失败，显示错误消息
            showErrorMessage("用户名或密码错误，请重试");
        }
    }
/**
 * 处理管理员登录按钮点击事件
 * @param event 事件对象
 */
private void handleAdminButtonClicked(ActionEvent event){
    usernameField.setPromptText("请输入管理员账号");
    passwordField.setPromptText("请输入管理员密码");
}

    /**
     * 验证用户凭据
     * @param username 用户名
     * @param password 密码
     * @return 验证是否成功
     */
    private boolean authenticateUser(String username, String password) {
        //TODO 登陆验证
//
//        return true;
        //测试
        return (username.equals("admin") && password.equals("admin123")) ||
                (username.equals("teacher") && password.equals("teacher123")) ||
                (username.equals("student") && password.equals("student123"));
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
            MainApplication.changeView("MainView.fxml","css/MainView.css");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("无法加载主界面");
        }
    }
}