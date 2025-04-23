package com.work.javafx.controller.teacher;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ApplyNewCourseController implements Initializable {
    private Stage stage;

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
                "2024-2025 秋季", "2024-2025 春季", "2025-2026 秋季", "2025-2026 春季"));
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
        departmentField.setText("信息科学与技术学院");
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
            // 在实际应用中，这里会调用服务层保存课程申请
            // 目前只显示成功消息
            showAlert(Alert.AlertType.INFORMATION, "提交成功", "课程申请已提交，等待审核！");
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
        if (isEmpty(courseCodeField)) errorMessages.append("- 课序号不能为空\n");
        if (isEmpty(classroomField)) errorMessages.append("- 上课教室不能为空\n");
        if (isEmpty(capacityField)) errorMessages.append("- 课容量不能为空\n");
        if (isEmpty(startWeekField)) errorMessages.append("- 开始周不能为空\n");
        if (isEmpty(endWeekField)) errorMessages.append("- 结束周不能为空\n");
        if (isEmpty(classHoursField)) errorMessages.append("- 课时不能为空\n");
        if (isEmpty(departmentField)) errorMessages.append("- 开设学院不能为空\n");
        if (isEmpty(courseDescriptionField)) errorMessages.append("- 课程简介不能为空\n");
        
        // 课序号格式验证（通常是字母+数字格式）
        if (!isEmpty(courseCodeField) && !courseCodeField.getText().matches("[A-Za-z]+\\d+")) {
            errorMessages.append("- 课序号格式不正确，应为字母+数字格式（如CS101）\n");
        }
        
        // 学分验证
        if (!isEmpty(creditsField)) {
            double credits = Double.parseDouble(creditsField.getText());
            if (credits <= 0 || credits > 10) {
                errorMessages.append("- 学分应在0-10之间\n");
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
            if (hours <= 0 || hours > 20) {
                errorMessages.append("- 课时应在1-20之间\n");
            }
        }
        
        // 课程简介长度验证
        if (!isEmpty(courseDescriptionField) && courseDescriptionField.getText().length() < 10) {
            errorMessages.append("- 课程简介太短，至少需要10个字符\n");
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
