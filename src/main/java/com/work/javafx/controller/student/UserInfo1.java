package com.work.javafx.controller.student;

import com.work.javafx.entity.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class UserInfo1 {

    // 左侧字段
    @FXML private TextField nameField;
    @FXML private TextField idField;
    @FXML private TextField nationField;
    @FXML private TextField politicsField;

    // 右侧字段
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField emergencyContactField;
    @FXML private TextField emergencyPhoneField;
    
    private Stage stage;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        //获取个人信息
        fetchUserInfo();
        //显示用户信息
        loadUserInfo_1();
    }

    private void fetchUserInfo() {
    }

    public void loadUserInfo_1(){
        phoneField.setText(UserSession.getInstance().getPhone());
        emailField.setText(UserSession.getInstance().getEmail());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // 左侧取消按钮
    @FXML
    private void handleCancelLeft(ActionEvent event) {
        closeWindow();
    }

    // 左侧保存按钮
    @FXML
    private void handleSaveLeft(ActionEvent event) {
        String name = nameField.getText();
        String id = idField.getText();
        String nation = nationField.getText();
        String politics = politicsField.getText();

        System.out.println("保存个人信息：");
        System.out.println("姓名：" + name);
        System.out.println("身份证号：" + id);
        System.out.println("民族：" + nation);
        System.out.println("政治面貌：" + politics);

        closeWindow();
    }

    // 右侧取消按钮
    @FXML
    private void handleCancelRight(ActionEvent event) {
        closeWindow();
    }

    // 右侧保存按钮
    @FXML
    private void handleSaveRight(ActionEvent event) {
        String phone = phoneField.getText();
        String email = emailField.getText();
        String emergencyName = emergencyContactField.getText();
        String emergencyPhone = emergencyPhoneField.getText();

        System.out.println("保存联系方式：");
        System.out.println("手机号：" + phone);
        System.out.println("邮箱：" + email);
        System.out.println("紧急联系人：" + emergencyName);
        System.out.println("紧急联系人号码：" + emergencyPhone);

        closeWindow();
    }

    // 公共关闭窗口方法
    private void closeWindow() {
        if (stage != null) {
            stage.close();
        } else if (nameField != null) {
            Stage currentStage = (Stage) nameField.getScene().getWindow();
            currentStage.close();
        }
    }
}
