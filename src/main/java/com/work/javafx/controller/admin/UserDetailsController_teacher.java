package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ShowMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDetailsController_teacher implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(UserDetailsController_teacher.class.getName());
    private static final Gson gson = new Gson();
    
    private Stage stage;
    private String userId;
    
    // Basic Information
    @FXML private Label idLabel;
    @FXML private Label usernameLabel;
    @FXML private Label sduidLabel;
    @FXML private Label sexLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label collegeLabel;
    @FXML private Label permissionLabel;
    
    // Additional Information
    @FXML private Label statusLabel;
    @FXML private Label admissionLabel;
    @FXML private Label ethnicLabel;
    @FXML private Label politicsStatusLabel;
    
    // Buttons
    @FXML private Button editButton;
    @FXML private Button closeButton;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始时禁用编辑按钮，后续可根据权限启用
        editButton.setDisable(true);
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void loadUserData(String userId) {
        this.userId = userId;
        fetchUserDetails();
    }
    
    private void fetchUserDetails() {
        Map<String, String> params = new HashMap<>();
        if (userId != null && !userId.isEmpty()) {
            params.put("userId", userId);
        }
        
        Platform.runLater(() -> {
            // 显示加载状态
            resetLabelsToLoading();
        });
        
        NetworkUtils.getAsync("/admin/getUserInfo", params)
            .thenAcceptAsync(response -> Platform.runLater(() -> {
                try {
                    JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
                    
                    if (jsonResponse.has("code") && jsonResponse.get("code").getAsInt() == 200 && 
                        jsonResponse.has("data") && jsonResponse.get("data").isJsonObject()) {
                        
                        JsonObject data = jsonResponse.getAsJsonObject("data");
                        
                        if (data.has("user") && data.get("user").isJsonObject()) {
                            JsonObject userInfo = data.getAsJsonObject("user");
                            
                            // 显示基本信息
                            setLabelText(usernameLabel, userInfo, "username");
                            setLabelText(sduidLabel, userInfo, "sduid");
                            if (userInfo.has("sduid") && !userInfo.get("sduid").isJsonNull()) {
                                idLabel.setText("工号: " + userInfo.get("sduid").getAsString());
                            }
                            setLabelText(sexLabel, userInfo, "sex");
                            setLabelText(emailLabel, userInfo, "email");
                            setLabelText(phoneLabel, userInfo, "phone", "未设置");
                            setLabelText(collegeLabel, userInfo, "college", "未设置");
                            setLabelText(ethnicLabel, userInfo, "ethnic");
                            setLabelText(politicsStatusLabel, userInfo, "politicsStatus");
                            
                            // 设置权限标签
                            if (userInfo.has("permission") && !userInfo.get("permission").isJsonNull()) {
                                int permission = userInfo.get("permission").getAsInt();
                                String permissionText = "未知";
                                switch (permission) {
                                    case 0: permissionText = "管理员"; break;
                                    case 1: permissionText = "教师"; break;
                                    case 2: permissionText = "学生"; break;
                                }
                                permissionLabel.setText(permissionText);
                            }
                        }
                        
                        // 设置状态信息
                        if (data.has("status") && data.get("status").isJsonObject()) {
                            JsonObject statusInfo = data.getAsJsonObject("status");
                            

                            // 设置状态标签
                            if (statusInfo.has("status") && !statusInfo.get("status").isJsonNull()) {
                                String status = statusInfo.get("status").getAsString();
                                String statusText;
                                switch (status) {
                                    case "STUDYING": statusText = "在职"; break;
//                                    case "GRADUATED": statusText = "已毕业"; break;
//                                    case "SUSPENDED": statusText = "休学"; break;
                                    default: statusText = status;
                                }
                                statusLabel.setText(statusText);
                            }
                            
                            setLabelText(admissionLabel, statusInfo, "admission");
                        }
                        
                        // 数据加载成功后可以启用编辑按钮
                        editButton.setDisable(false);
                        
                    } else {
                        String errorMsg = "获取用户信息失败";
                        if (jsonResponse.has("msg")) {
                            errorMsg += ": " + jsonResponse.get("msg").getAsString();
                        }
                        LOGGER.log(Level.WARNING, "API Error: " + errorMsg);
                        ShowMessage.showErrorMessage("加载失败", errorMsg);
                        resetLabelsToError();
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "解析用户数据失败", e);
                    ShowMessage.showErrorMessage("处理错误", "无法处理服务器响应: " + e.getMessage());
                    resetLabelsToError();
                }
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "网络请求失败", ex);
                    ShowMessage.showErrorMessage("网络错误", "无法连接到服务器: " + ex.getMessage());
                    resetLabelsToError();
                });
                return null;
            });
    }
    
    private void setLabelText(Label label, JsonObject json, String key) {
        setLabelText(label, json, key, "未知");
    }
    
    private void setLabelText(Label label, JsonObject json, String key, String defaultValue) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            label.setText(json.get(key).getAsString());
        } else {
            label.setText(defaultValue);
        }
    }
    
    private void resetLabelsToLoading() {
        // 设置所有标签为"加载中..."
        Label[] allLabels = { 
            usernameLabel, sduidLabel, sexLabel, emailLabel, phoneLabel, collegeLabel, permissionLabel,
             statusLabel, admissionLabel,  ethnicLabel, politicsStatusLabel
        };
        
        for (Label label : allLabels) {
            label.setText("加载中...");
        }
    }
    
    private void resetLabelsToError() {
        // 设置所有标签为错误状态
        Label[] allLabels = { 
            usernameLabel, sduidLabel, sexLabel, emailLabel, phoneLabel, collegeLabel, permissionLabel,
             statusLabel, admissionLabel, ethnicLabel, politicsStatusLabel
        };
        
        for (Label label : allLabels) {
            label.setText("加载失败");
        }
    }
    
    @FXML
    public void handleEdit() {
        ShowMessage.showInfoMessage("功能开发中", "编辑用户信息功能尚未实现");
    }
    
    @FXML
    public void handleClose() {
        if (stage != null) {
            stage.close();
        }
    }
}
