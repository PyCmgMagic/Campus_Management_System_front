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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDetailsController_student implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(UserDetailsController_student.class.getName());
    private static final Gson gson = new Gson();
    
    private Stage stage;
    private String userId;
    private boolean isEditMode = false;
    
    @FXML private Label idLabel;
    @FXML private Label idLabel1;//id
    @FXML private Label usernameLabel;
    @FXML private Label sduidLabel;
    @FXML private Label sexLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label collegeLabel;
    @FXML private Label majorLabel;
    @FXML private Label permissionLabel;
    
    @FXML private Label gradeLabel;
    @FXML private Label sectionLabel;
    @FXML private Label statusLabel;
    @FXML private Label admissionLabel;
    @FXML private Label graduationLabel;
    @FXML private Label ethnicLabel;
    @FXML private Label nationLabel;
    @FXML private Label politicsStatusLabel;
    
    @FXML private TextField usernameField;
    @FXML private TextField sduidField;
    @FXML private ComboBox<String> sexComboBox;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField collegeField;
    
    @FXML private TextField gradeField;
    @FXML private TextField sectionField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextField admissionField;
    @FXML private TextField graduationField;
    @FXML private TextField ethnicField;
    @FXML private TextField nationField;
    @FXML private ComboBox<String> majorComboBox;
    @FXML private TextField politicsStatusField;
    
    @FXML private Button editButton;
    @FXML private Button closeButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始时禁用编辑按钮，后续根据权限启用
        editButton.setDisable(true);
        majorComboBox.setItems(FXCollections.observableArrayList("软件工程", "数字媒体技术", "大数据", "AI"));
        // 初始化下拉选项
        sexComboBox.setItems(FXCollections.observableArrayList("男", "女"));
        statusComboBox.setItems(FXCollections.observableArrayList("在校学习", "已毕业", "休学"));
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
                            idLabel1.setText("ID:"+userInfo.get("id").getAsString());
                            if (userInfo.has("sduid") && !userInfo.get("sduid").isJsonNull()) {
                                idLabel.setText("学号: " + userInfo.get("sduid").getAsString());
                            }
                            setLabelText(sexLabel, userInfo, "sex");
                            setLabelText(emailLabel, userInfo, "email");
                            setLabelText(phoneLabel, userInfo, "phone", "未设置");
                            setLabelText(collegeLabel, userInfo, "college", "未设置");
                            setLabelText(majorLabel, userInfo, "major", "未设置");
                            setLabelText(ethnicLabel, userInfo, "ethnic");
                            setLabelText(nationLabel, userInfo, "nation");
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
                            majorComboBox.getSelectionModel().select(majorLabel.getText());
                            setTextFieldFromLabel(ethnicField, ethnicLabel);
                            setTextFieldFromLabel(politicsStatusField, politicsStatusLabel);
                        }
                        
                        // 设置状态信息
                        if (data.has("status") && data.get("status").isJsonObject()) {
                            JsonObject statusInfo = data.getAsJsonObject("status");
                            
                            setLabelText(gradeLabel, statusInfo, "grade");
                            setLabelText(sectionLabel, statusInfo, "section");
                            
                            // 设置状态标签
                            if (statusInfo.has("status") && !statusInfo.get("status").isJsonNull()) {
                                String status = statusInfo.get("status").getAsString();
                                String statusText;
                                switch (status) {
                                    case "STUDYING": statusText = "在校学习"; break;
                                    case "GRADUATED": statusText = "已毕业"; break;
                                    case "SUSPENDED": statusText = "休学"; break;
                                    default: statusText = status;
                                }
                                statusLabel.setText(statusText);
                                setComboBoxFromLabel(statusComboBox, statusLabel);
                            }
                            
                            setLabelText(admissionLabel, statusInfo, "admission");
                            setLabelText(graduationLabel, statusInfo, "graduation");
                            
                            // 预设编辑字段的值
                            setTextFieldFromLabel(gradeField, gradeLabel);
                            setTextFieldFromLabel(sectionField, sectionLabel);
                            setTextFieldFromLabel(admissionField, admissionLabel);
                            setTextFieldFromLabel(graduationField, graduationLabel);
                            setTextFieldFromLabel(nationField, nationLabel);

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
            usernameLabel, sduidLabel, sexLabel, emailLabel, phoneLabel, collegeLabel, permissionLabel,majorLabel,
            gradeLabel, sectionLabel, statusLabel, admissionLabel, graduationLabel, ethnicLabel, politicsStatusLabel
        };
        
        for (Label label : allLabels) {
            label.setText("加载中...");
        }
    }
    
    private void resetLabelsToError() {
        // 设置所有标签为错误状态
        Label[] allLabels = { 
            usernameLabel, sduidLabel, sexLabel, emailLabel, phoneLabel, collegeLabel, permissionLabel,majorLabel,
            gradeLabel, sectionLabel, statusLabel, admissionLabel, graduationLabel, ethnicLabel, politicsStatusLabel
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
    private String transMajor(){
        String major = majorComboBox.getValue();
        switch (major){
            case "软件工程":
                return "MAJOR_0";
            case "数字媒体技术":
                return "MAJOR_1";
            case "大数据":
                return "MAJOR_2";
            case "AI":
                return "MAJOR_3";
            default:
                return "MAJOR_-1";
        }
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
        userInfo.put("nation", nationField.getText());
        userInfo.put("major", transMajor());
        userInfo.put("PoliticsStatus", politicsStatusField.getText());
        userInfo.put("admission", admissionField.getText());
        userInfo.put("graduation", graduationField.getText());
        userInfo.put("grade", gradeField.getText());
        userInfo.put("section", sectionField.getText());
        String statusValue = statusComboBox.getValue();
        String statusToSend = null;
        if (statusValue != null) {
             switch (statusValue) {
                case "在校学习": statusToSend = "STUDYING"; break;
                case "已毕业": statusToSend = "GRADUATED"; break;
                case "休学": statusToSend = "SUSPENDED"; break;
                default:
                 LOGGER.log(Level.WARNING, "Unknown status value selected: " + statusValue);
             }
        }
        if (statusToSend != null) {
            userInfo.put("status", statusToSend);
        } else {
             LOGGER.log(Level.INFO, "Status not sent as it was null or invalid.");
        }
        
        // 发送更新请求到服务器
        saveUserData(userInfo);
    }
    
    private void saveUserData(Map<String, String> userInfo) {
        // 显示加载指示
        editButton.setDisable(true);
        saveButton.setDisable(true);
        cancelButton.setDisable(true);
        
        NetworkUtils.postAsync("/admin/updateUser", userInfo, null)
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
                        saveButton.setDisable(false);
                        cancelButton.setDisable(false);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to process update response", e);
                    ShowMessage.showErrorMessage("处理错误", "无法处理服务器响应: " + e.getMessage());
                    saveButton.setDisable(false);
                    cancelButton.setDisable(false);
                }
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Network error during update", ex);
                    ShowMessage.showErrorMessage("网络错误", "无法连接到服务器: " + ex.getMessage());
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
        idLabel.setText("学号: " + sduidField.getText());
        sexLabel.setText(sexComboBox.getValue());
        emailLabel.setText(emailField.getText());
        phoneLabel.setText(phoneField.getText().isEmpty() ? "未设置" : phoneField.getText());
        collegeLabel.setText(collegeField.getText().isEmpty() ? "未设置" : collegeField.getText());
        ethnicLabel.setText(ethnicField.getText());
        politicsStatusLabel.setText(politicsStatusField.getText());
        nationLabel.setText(nationField.getText());
        majorLabel.setText(majorLabel.getText());
        // 从编辑控件更新标签显示的值
        gradeLabel.setText(gradeField.getText());
        sectionLabel.setText(sectionField.getText());
        statusLabel.setText(statusComboBox.getValue());
        admissionLabel.setText(admissionField.getText());
        majorLabel.setText(majorComboBox.getValue());
        graduationLabel.setText(graduationField.getText());
    }
    
    @FXML
    public void handleCancel() {
        // 退出编辑模式，不保存更改
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
        majorComboBox.setVisible(editMode);
        // 显示/隐藏编辑控件
        gradeField.setVisible(editMode);
        sectionField.setVisible(editMode);
        statusComboBox.setVisible(editMode);
        admissionField.setVisible(editMode);
        graduationField.setVisible(editMode);
        ethnicField.setVisible(editMode);
        nationField.setVisible(editMode);
        politicsStatusField.setVisible(editMode);
        
        // 显示/隐藏标签
        usernameLabel.setVisible(!editMode);
        sduidLabel.setVisible(!editMode);
        sexLabel.setVisible(!editMode);
        emailLabel.setVisible(!editMode);
        phoneLabel.setVisible(!editMode);
        collegeLabel.setVisible(!editMode);
        majorLabel.setVisible(!editMode);
        // 显示/隐藏标签
        gradeLabel.setVisible(!editMode);
        sectionLabel.setVisible(!editMode);
        statusLabel.setVisible(!editMode);
        admissionLabel.setVisible(!editMode);
        graduationLabel.setVisible(!editMode);
        ethnicLabel.setVisible(!editMode);
        nationLabel.setVisible(!editMode);
        politicsStatusLabel.setVisible(!editMode);
        
        // 显示/隐藏按钮
        editButton.setVisible(!editMode);
        editButton.setManaged(!editMode);
        saveButton.setVisible(editMode);
        saveButton.setManaged(editMode);
        cancelButton.setVisible(editMode);
        cancelButton.setManaged(editMode);
        
        // 如果取消编辑，恢复编辑字段原始值
        if (!editMode) {
            setTextFieldFromLabel(usernameField, usernameLabel);
            setTextFieldFromLabel(sduidField, sduidLabel);
            setComboBoxFromLabel(sexComboBox, sexLabel);
            setTextFieldFromLabel(emailField, emailLabel);
            setTextFieldFromLabel(phoneField, phoneLabel);
            setTextFieldFromLabel(collegeField, collegeLabel);
            setTextFieldFromLabel(ethnicField, ethnicLabel);
            setTextFieldFromLabel(politicsStatusField, politicsStatusLabel);
            
            setTextFieldFromLabel(gradeField, gradeLabel);
            setTextFieldFromLabel(sectionField, sectionLabel);
            setComboBoxFromLabel(statusComboBox, statusLabel);
            setComboBoxFromLabel(majorComboBox, majorLabel);
            setTextFieldFromLabel(admissionField, admissionLabel);
            setTextFieldFromLabel(graduationField, graduationLabel);
            
            editButton.setDisable(false);
            saveButton.setDisable(true);
            cancelButton.setDisable(true);
        } else {
            editButton.setDisable(true);
            saveButton.setDisable(false);
            cancelButton.setDisable(false);
        }
    }
    
    private void setTextFieldFromLabel(TextField textField, Label label) {
        String text = label.getText();
        if (text != null && !text.equals("未知") && !text.equals("加载中...") && !text.equals("加载失败") && !text.equals("未设置")) {
            textField.setText(text);
        } else {
            textField.setText("");
        }
    }
    
    private void setComboBoxFromLabel(ComboBox<String> comboBox, Label label) {
        String text = label.getText();
        if (text != null && !text.equals("未知") && !text.equals("加载中...") && !text.equals("加载失败")) {
             if (comboBox.getItems().contains(text)) {
                comboBox.setValue(text);
             } else {
                 LOGGER.log(Level.WARNING, "Label text '" + text + "' not found in ComboBox items for " + comboBox.getId());
                 comboBox.getSelectionModel().clearSelection();
             }
        } else {
            comboBox.getSelectionModel().clearSelection();
        }
    }
    
    @FXML
    public void handleClose() {
        if (stage != null) {
            stage.close();
        }
    }
}
