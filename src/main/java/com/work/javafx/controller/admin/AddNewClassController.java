package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.work.javafx.util.NetworkUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AddNewClassController implements Initializable {

    @FXML private VBox rootPane;
    @FXML private TextField advisorIdField;
    @FXML private TextField idField;
    @FXML private TextField numberField;
    @FXML private ComboBox<String> majorComboBox;
    @FXML private ComboBox<String> gradeComboBox;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;
    @FXML private Label titleLabel;

    private boolean isSaving = false;
    private  int ClassID = -1;
    private Stage stage;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        majorComboBox.setItems(FXCollections.observableArrayList(
                "软件工程", "数字媒体技术", "大数据", "AI"
        ));
        
        gradeComboBox.setItems(FXCollections.observableArrayList(
                "2021", "2022", "2023", "2024"
        ));
        
        majorComboBox.getSelectionModel().selectFirst();
        gradeComboBox.getSelectionModel().selectFirst();
        
        setupValidationListeners();
        
        cancelButton.setOnAction(e -> closeDialog());
        submitButton.setOnAction(e -> submitForm());
        if(ClassID != -1){
            titleLabel.setText("编辑班级");
        }
    }
    /**
     * 接受班级id
     */
    public void initClassId(int ClassID){
        this.ClassID = ClassID;
        if(ClassID != -1){
            titleLabel.setText("编辑班级");
        }
    }
    private void setupValidationListeners() {
        numberField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                numberField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        advisorIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                advisorIdField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }
    private String transformMajor(String major){
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
    private void submitForm() {
        if (isSaving) {
            return;
        }
        if (!validateForm()) {
            return;
        }
        
        isSaving = true;
        submitButton.setDisable(true);
        statusLabel.setText("正在保存...");
        Map<String, String> params = new HashMap<>();
        params.put("major", transformMajor(majorComboBox.getValue()));
        params.put("advisorId", advisorIdField.getText());
        params.put("grade", gradeComboBox.getValue());
        params.put("number", numberField.getText());
        Gson gson = new Gson();
        if(ClassID == -1){
            params.put("id","");
        NetworkUtils.post("/section/addSection",params, "", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                Platform.runLater(() -> {
                    try {
                        JsonObject response = gson.fromJson(result, JsonObject.class);
                        
                        if (response.has("code") && response.get("code").getAsInt() == 200) {
                            showAlert(Alert.AlertType.INFORMATION, "添加成功", "班级创建成功");
                            closeDialog();
                        } else {
                            String errorMsg = response.has("msg") ? response.get("msg").getAsString() : "未知错误";
                            showAlert(Alert.AlertType.ERROR, "添加失败", errorMsg);
                            isSaving = false;
                            submitButton.setDisable(false);
                            statusLabel.setText("保存失败: " + errorMsg);
                        }
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "处理错误", "数据填写有误");
                        isSaving = false;
                        submitButton.setDisable(false);
                        statusLabel.setText("处理错误");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "网络错误", "无法连接到服务器: " + e.getMessage());
                    isSaving = false;
                    submitButton.setDisable(false);
                    statusLabel.setText("网络错误");
                });
            }
        });
        }else {
            params.put("id",ClassID+"");
            NetworkUtils.post("/section/updateSection",params, "", new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    Platform.runLater(() -> {
                        try {
                            JsonObject response = gson.fromJson(result, JsonObject.class);

                            if (response.has("code") && response.get("code").getAsInt() == 200) {
                                showAlert(Alert.AlertType.INFORMATION, "修改成功", "班级修改成功");
                                closeDialog();
                            } else {
                                String errorMsg = response.has("msg") ? response.get("msg").getAsString() : "未知错误";
                                showAlert(Alert.AlertType.ERROR, "修改失败", errorMsg);
                                isSaving = false;
                                submitButton.setDisable(false);
                                statusLabel.setText("保存失败: " + errorMsg);
                            }
                        } catch (Exception e) {
                            showAlert(Alert.AlertType.ERROR, "处理错误", "数据填写有误");
                            isSaving = false;
                            submitButton.setDisable(false);
                            statusLabel.setText("处理错误");
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Platform.runLater(() -> {
                        JsonObject res = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")), JsonObject.class);
                        showAlert(Alert.AlertType.ERROR, "网络错误", res.get("msg").getAsString());
                        isSaving = false;
                        submitButton.setDisable(false);
                        statusLabel.setText("处理错误");
                    });
                }
            });
        }
    }
    
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        if (majorComboBox.getValue() == null || majorComboBox.getValue().isEmpty()) {
            errors.append("请选择专业\n");
        }
        
        if (gradeComboBox.getValue() == null || gradeComboBox.getValue().isEmpty()) {
            errors.append("请选择年级\n");
        }
        if(ClassID == -1){
            if (advisorIdField.getText() == null || advisorIdField.getText().isEmpty()) {
                errors.append("请输入导员ID\n");
            }
        }

        if (numberField.getText() == null || numberField.getText().isEmpty()) {
            errors.append("请输入班级编号\n");
        }
        
        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "表单错误", errors.toString());
            return false;
        }
        
        return true;
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void closeDialog() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }
    public void setStage(Stage stage){
        this.stage = stage;
    }
}
