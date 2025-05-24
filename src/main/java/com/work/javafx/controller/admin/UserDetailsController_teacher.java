package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ShowMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
    private boolean isEditMode = false;
    
    @FXML private Label idLabel;
    @FXML private Label idLabel1;
    @FXML private Label usernameLabel;
    @FXML private Label sduidLabel;
    @FXML private Label sexLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label collegeLabel;
    @FXML private Label permissionLabel;
    
    @FXML private Label statusLabel;
    @FXML private Label admissionLabel;
    @FXML private Label ethnicLabel;
    @FXML private Label politicsStatusLabel;
    
    @FXML private TextField usernameField;
    @FXML private TextField sduidField;
    @FXML private ComboBox<String> sexComboBox;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField collegeField;
    @FXML private ComboBox<String> permissionComboBox;
    
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextField admissionField;
    @FXML private TextField ethnicField;
    @FXML private TextField politicsStatusField;
    
    @FXML private Button editButton;
    @FXML private Button closeButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始时禁用编辑按钮，后续可根据权限启用
        editButton.setDisable(true);
        
        // 初始化下拉选项
        sexComboBox.setItems(FXCollections.observableArrayList("男", "女"));
        permissionComboBox.setItems(FXCollections.observableArrayList("管理员", "教师", "学生"));
        statusComboBox.setItems(FXCollections.observableArrayList("在职"));
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
                            if (userInfo.has("id") && !userInfo.get("id").isJsonNull()) {
                                idLabel1.setText("ID: " + userInfo.get("id").getAsString());
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
                            
                            // 预设编辑字段的值
                            setTextFieldFromLabel(usernameField, usernameLabel);
                            setTextFieldFromLabel(sduidField, sduidLabel);
                            setComboBoxFromLabel(sexComboBox, sexLabel);
                            setTextFieldFromLabel(emailField, emailLabel);
                            setTextFieldFromLabel(phoneField, phoneLabel);
                            setTextFieldFromLabel(collegeField, collegeLabel);
                            setComboBoxFromLabel(permissionComboBox, permissionLabel);
                            setTextFieldFromLabel(ethnicField, ethnicLabel);
                            setTextFieldFromLabel(politicsStatusField, politicsStatusLabel);
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
                                    default: statusText = status;
                                }
                                statusLabel.setText(statusText);
                                setComboBoxFromLabel(statusComboBox, statusLabel);
                            }
                            
                            setLabelText(admissionLabel, statusInfo, "admission");
                            setTextFieldFromLabel(admissionField, admissionLabel);
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
    
    private void setTextFieldFromLabel(TextField textField, Label label) {
        if (label.getText() != null && !label.getText().equals("未知") && !label.getText().equals("加载中...") && !label.getText().equals("加载失败")) {
            textField.setText(label.getText());
        } else {
            textField.setText("");
        }
    }
    
    private void setComboBoxFromLabel(ComboBox<String> comboBox, Label label) {
        String text = label.getText();
        if (text != null && !text.equals("未知") && !text.equals("加载中...") && !text.equals("加载失败")) {
            comboBox.setValue(text);
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
        isEditMode = true;
        
        // 切换到编辑模式
        toggleEditMode(true);
    }
    
    @FXML
    public void handleSave() {
        // 从编辑控件获取修改后的值
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("id", userId);
        userInfo.put("username", usernameField.getText());
        userInfo.put("SDUId", sduidField.getText());
        userInfo.put("sex", sexComboBox.getValue());
        userInfo.put("email", emailField.getText());
        userInfo.put("phone", phoneField.getText());
        userInfo.put("college", collegeField.getText());
        userInfo.put("ethnic", ethnicField.getText());
        userInfo.put("PoliticsStatus", politicsStatusField.getText());
        userInfo.put("admission", admissionField.getText());

        // 发送更新请求到服务器
        saveUserData(userInfo);
    }
    
    private void saveUserData(Map<String, String> userInfo) {
        // 显示加载指示
        editButton.setDisable(true);
        saveButton.setDisable(true);
        cancelButton.setDisable(true);
        

        NetworkUtils.postAsync("/admin/updateUser", userInfo,null)
            .thenAcceptAsync(response -> Platform.runLater(() -> {
                try {
                    JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
                    
                    if (jsonResponse.has("code") && jsonResponse.get("code").getAsInt() == 200) {
                        ShowMessage.showInfoMessage("保存成功", "用户信息已成功更新");
                        
                        // 更新页面上显示的值
                        updateLabelsFromEditFields();
                        
                        // 退出编辑模式
                        toggleEditMode(false);
                    } else {
                        String errorMsg = "更新用户信息失败";
                        if (jsonResponse.has("msg")) {
                            errorMsg += ": " + jsonResponse.get("msg").getAsString();
                        }
                        ShowMessage.showErrorMessage("保存失败", errorMsg);
                    }
                } catch (Exception e) {
                    ShowMessage.showErrorMessage("处理错误", "无法处理服务器响应: " + e.getMessage());
                }
                
                editButton.setDisable(false);
                saveButton.setDisable(false);
                cancelButton.setDisable(false);
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    ShowMessage.showErrorMessage("网络错误", "无法连接到服务器: " + ex.getMessage());
                    
                    editButton.setDisable(false);
                    saveButton.setDisable(false);
                    cancelButton.setDisable(false);
                });
                return null;
            });
    }
    
    private void updateLabelsFromEditFields() {
        // 从编辑控件更新标签显示的值
        usernameLabel.setText(usernameField.getText());
        sduidLabel.setText(sduidField.getText());
        idLabel.setText("工号: " + sduidField.getText());
        sexLabel.setText(sexComboBox.getValue());
        emailLabel.setText(emailField.getText());
        phoneLabel.setText(phoneField.getText().isEmpty() ? "未设置" : phoneField.getText());
        collegeLabel.setText(collegeField.getText().isEmpty() ? "未设置" : collegeField.getText());
        permissionLabel.setText(permissionComboBox.getValue());
        statusLabel.setText(statusComboBox.getValue());
        admissionLabel.setText(admissionField.getText());
        ethnicLabel.setText(ethnicField.getText());
        politicsStatusLabel.setText(politicsStatusField.getText());
    }
    
    @FXML
    public void handleCancel() {
        // 重新加载原始用户数据
        toggleEditMode(false);
    }
    
    private void toggleEditMode(boolean editMode) {
        isEditMode = editMode;
        
        // 显示/隐藏编辑控件
        usernameField.setVisible(editMode);
        sduidField.setVisible(editMode);
        sexComboBox.setVisible(editMode);
        emailField.setVisible(editMode);
        phoneField.setVisible(editMode);
        collegeField.setVisible(editMode);
        permissionComboBox.setVisible(editMode);
        statusComboBox.setVisible(editMode);
        admissionField.setVisible(editMode);
        ethnicField.setVisible(editMode);
        politicsStatusField.setVisible(editMode);
        
        // 显示/隐藏标签
        usernameLabel.setVisible(!editMode);
        sduidLabel.setVisible(!editMode);
        sexLabel.setVisible(!editMode);
        emailLabel.setVisible(!editMode);
        phoneLabel.setVisible(!editMode);
        collegeLabel.setVisible(!editMode);
        permissionLabel.setVisible(!editMode);
        statusLabel.setVisible(!editMode);
        admissionLabel.setVisible(!editMode);
        ethnicLabel.setVisible(!editMode);
        politicsStatusLabel.setVisible(!editMode);
        
        // 显示/隐藏按钮
        editButton.setVisible(!editMode);
        saveButton.setVisible(editMode);
        cancelButton.setVisible(editMode);
        
        // 如果取消编辑，恢复编辑字段原始值
        if (!editMode) {
            setTextFieldFromLabel(usernameField, usernameLabel);
            setTextFieldFromLabel(sduidField, sduidLabel);
            setComboBoxFromLabel(sexComboBox, sexLabel);
            setTextFieldFromLabel(emailField, emailLabel);
            setTextFieldFromLabel(phoneField, phoneLabel);
            setTextFieldFromLabel(collegeField, collegeLabel);
            setComboBoxFromLabel(permissionComboBox, permissionLabel);
            setComboBoxFromLabel(statusComboBox, statusLabel);
            setTextFieldFromLabel(admissionField, admissionLabel);
            setTextFieldFromLabel(ethnicField, ethnicLabel);
            setTextFieldFromLabel(politicsStatusField, politicsStatusLabel);
        }
    }
    
    @FXML
    public void handleClose() {
        if (stage != null) {
            stage.close();
        }
    }
}
