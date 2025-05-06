package com.work.javafx.controller.teacher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ShowMessage;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ApplyNewCourseController implements Initializable {
    private Stage stage;
    static Gson gson = new Gson();
    @FXML private TextField courseNameField;
    @FXML private TextField courseSubtypeField;
    @FXML private TextField creditsField;
    @FXML private TextField courseCodeField;
    @FXML private TextField classroomField;
    @FXML private TextField capacityField;
    @FXML private TextField startWeekField;
    @FXML private TextField endWeekField;
    @FXML private TextField classHoursField;
    @FXML private TextField departmentField;
    @FXML private ComboBox<String> semesterComboBox;
    @FXML private ComboBox<String> courseTypeComboBox;
    @FXML private ComboBox<String> assessmentTypeComboBox;
    @FXML private TextField regularPercentageField;
    @FXML private TextField finalPercentageField;
    @FXML private TextArea courseDescriptionField;
    @FXML private VBox gradeDistributionContainer;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化下拉菜单
        semesterComboBox.setItems(FXCollections.observableArrayList(
                "2024-2025-1", "2024-2025-2", "2025-2026-1", "2025-2026-2"));
        semesterComboBox.getSelectionModel().selectFirst();
        
        courseTypeComboBox.setItems(FXCollections.observableArrayList(
                "必修", "限选", "任选"));
        courseTypeComboBox.getSelectionModel().selectFirst();
        
        assessmentTypeComboBox.setItems(FXCollections.observableArrayList(
                "考试", "考查"));
        assessmentTypeComboBox.getSelectionModel().selectFirst();
        
        // 设置成绩比例初始值
        regularPercentageField.setText("40");
        finalPercentageField.setText("60");
        
        // 添加成绩比例监听器
        regularPercentageField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                regularPercentageField.setText(newValue.replaceAll("[^\\d]", ""));
            } else if (!newValue.isEmpty()) {
                int regularValue = Integer.parseInt(newValue);
                if (regularValue > 100) {
                    regularPercentageField.setText("100");
                    finalPercentageField.setText("0");
                } else {
                    finalPercentageField.setText(String.valueOf(100 - regularValue));
                }
            }
        });
        
        finalPercentageField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                finalPercentageField.setText(newValue.replaceAll("[^\\d]", ""));
            } else if (!newValue.isEmpty()) {
                int finalValue = Integer.parseInt(newValue);
                if (finalValue > 100) {
                    finalPercentageField.setText("100");
                    regularPercentageField.setText("0");
                } else {
                    regularPercentageField.setText(String.valueOf(100 - finalValue));
                }
            }
        });
        
        // 考核方式监听器
        assessmentTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean isExam = "考试".equals(newValue);
            gradeDistributionContainer.setVisible(isExam);
            gradeDistributionContainer.setManaged(isExam);
        });
        
        // 设置数字输入限制
        addNumericValidation(creditsField, true); // 允许小数
        addNumericValidation(capacityField, false); // 只允许整数
        addNumericValidation(startWeekField, false); // 只允许整数
        addNumericValidation(endWeekField, false); // 只允许整数
        addNumericValidation(classHoursField, false); // 只允许整数
        
        // 设置默认值
        startWeekField.setText("1");
        endWeekField.setText("16");
        departmentField.setText("软件学院");
    }
    
    /**
     * 添加数字输入验证
     * @param textField 文本框
     * @param allowDecimal 是否允许小数
     */
    private void addNumericValidation(TextField textField, boolean allowDecimal) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (allowDecimal) {
                // 允许小数点的数字格式
                if (!newValue.matches("\\d*(\\.\\d*)?")) {
                    textField.setText(oldValue);
                }
            } else {
                // 只允许整数
                if (!newValue.matches("\\d*")) {
                    textField.setText(oldValue);
                }
            }
        });
    }

    /**
     * 处理取消按钮
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        // 询问用户是否确定取消
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认取消");
        alert.setHeaderText(null);
        alert.setContentText("确定要取消申请吗？所有已填写的内容将丢失。");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                closeWindow();
            }
        });
    }
    
    /**
     * 处理提交按钮
     */
    @FXML
    private void handleSubmit(ActionEvent event) {
        if (validateForm()) {
            Map<String,String> requestBody = new HashMap<>();
            requestBody.put("name",courseNameField.getText());//课程名称
            if(!isEmpty(courseSubtypeField)){
                requestBody.put("category",courseSubtypeField.getText());//课程小类
            }if(!isEmpty(courseDescriptionField)){
                requestBody.put("intro",courseDescriptionField.getText());//课程简介
            }
            if(assessmentTypeComboBox.getValue().equals("考试")){
                requestBody.put("examination","1");//考试
                requestBody.put("regularRatio",(Integer.parseInt(regularPercentageField.getText())/100.0)+"");//平时分
                requestBody.put("finalRatio",(Integer.parseInt(finalPercentageField.getText())/100.0)+"");//期末分
            }else{
                requestBody.put("examination","0");//考查
            }
            requestBody.put("point",creditsField.getText());//课程学分
            requestBody.put("classNum",courseCodeField.getText());//课序号
            requestBody.put("classroom",classroomField.getText());//上课教室
            requestBody.put("weekStart",startWeekField.getText());//开始周
            requestBody.put("weekEnd",endWeekField.getText());//结束周
            requestBody.put("period",classHoursField.getText());//学时
            requestBody.put("college",departmentField.getText());//学院
            requestBody.put("term",semesterComboBox.getValue());//开设学期
            requestBody.put("type",courseTypeComboBox.getValue());//类型
            requestBody.put("capacity",capacityField.getText());//客容量
            String requestJson = gson.toJson(requestBody);
            System.out.println(requestJson);
            NetworkUtils.post("/class/create", requestJson, new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    JsonObject res = gson.fromJson(result,JsonObject.class);
                    try{
                    if(res.has("code") && res.get("code").getAsInt() == 200){
                        ShowMessage.showInfoMessage("创建成功",res.get("msg").getAsString());
                    }else if(res.has("code") && res.get("code").getAsInt() == 403){
                        ShowMessage.showErrorMessage("创建失败",res.get("msg").getAsString());
                    }
                    else{
                        ShowMessage.showInfoMessage("创建结果",res.get("msg").getAsString());
                    }
                    }catch (Exception e){
                        ShowMessage.showInfoMessage("创建结果",res.get("msg").getAsString());
                        System.out.println(e);
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    JsonObject res = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")), JsonObject.class);
                    ShowMessage.showErrorMessage("操作失败",res.get("msg").getAsString());
                }
            });
            closeWindow();
        }
    }
    
    /**
     * 表单验证
     */
    private boolean validateForm() {
        StringBuilder errorMessages = new StringBuilder();
        
        // 必填字段验证
        if (isEmpty(courseNameField)) errorMessages.append("- 课程名称不能为空\n");
        if (isEmpty(creditsField)) errorMessages.append("- 学分不能为空\n");
        if (isEmpty(classroomField)) errorMessages.append("- 上课教室不能为空\n");
        if (isEmpty(capacityField)) errorMessages.append("- 课容量不能为空\n");
        if (isEmpty(startWeekField)) errorMessages.append("- 开始周不能为空\n");
        if (isEmpty(endWeekField)) errorMessages.append("- 结束周不能为空\n");
        if (isEmpty(classHoursField)) errorMessages.append("- 课时不能为空\n");
        if (isEmpty(departmentField)) errorMessages.append("- 开设学院不能为空\n");


        
        // 学分验证
        if (!isEmpty(creditsField)) {
            double credits = Double.parseDouble(creditsField.getText());
            if (credits <= 0 || credits > 20) {
                errorMessages.append("- 学分应在0-20之间\n");
            }
        }
        
        // 课程容量验证
        if (!isEmpty(capacityField)) {
            int capacity = Integer.parseInt(capacityField.getText());
            if (capacity <= 0) {
                errorMessages.append("- 课容量必须大于0\n");
            }
        }
        
        // 周数验证
        if (!isEmpty(startWeekField) && !isEmpty(endWeekField)) {
            int start = Integer.parseInt(startWeekField.getText());
            int end = Integer.parseInt(endWeekField.getText());
            
            if (start <= 0 || end <= 0) {
                errorMessages.append("- 周数必须大于0\n");
            } else if (start > end) {
                errorMessages.append("- 开始周不能大于结束周\n");
            } else if (end > 20) {
                errorMessages.append("- 结束周不能超过20\n");
            }
        }
        
        // 课时验证
        if (!isEmpty(classHoursField)) {
            int hours = Integer.parseInt(classHoursField.getText());
            if (hours <= 0 || hours > 200) {
                errorMessages.append("- 课时应在1-200之间\n");
            }
        }
        
        // 课程简介长度验证
        if (courseDescriptionField.getText().length() > 200) {
            errorMessages.append("- 请控制在200字以内\n");
        }
        
        // 显示错误信息
        if (errorMessages.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "表单验证失败", errorMessages.toString());
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查文本字段是否为空
     */
    private boolean isEmpty(TextInputControl field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }
    
    /**
     * 显示弹窗
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 设置窗口引用
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    /**
     * 关闭窗口
     */
    private void closeWindow() {
        if (stage != null) {
            stage.close();
        }
    }
}
