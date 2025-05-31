package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ShowMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class AddNewStudentController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(AddNewStudentController.class.getName());
    private static final Gson gson = new Gson();

    @FXML private TextField sduIdField;
    @FXML private TextField usernameField;
    @FXML private ComboBox<String> sexComboBox;
    @FXML private ComboBox<String> collegeComboBox;
    @FXML private ComboBox<String> majorComboBox;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> ethnicComboBox;
    @FXML private ComboBox<String> nationComboBox;
    @FXML private ComboBox<String> politicsStatusComboBox;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;
    @FXML private Label permissionLabel;
    private Stage stage;
    private Map<TextField, Label> errorLabels = new HashMap<>();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeCollegeOptions();
        initializeEthnicOptions();
        initializeNationOptions();
        initializeSexOptions();
        initializePoliticsStatusOptions();
        setupValidation();
    }

    /**
     * 初始化学院和专业选项
     */
    private void initializeCollegeOptions() {
        ObservableList<String> colleges = FXCollections.observableArrayList(
                "软件学院","计算机学院", "数学学院", "物理学院", "外语学院", "经济管理学院",
                "文学院", "历史学院", "法学院", "医学院", "生命科学学院"
        );
        collegeComboBox.setItems(colleges);
        ObservableList<String> majors = FXCollections.observableArrayList(
                "软件工程","大数据","数字媒体技术","人工智能国际班"
        );
        majorComboBox.setItems(majors);
    }
      /**
     * 初始化学院选项
     */
    private void initializeSexOptions() {
        ObservableList<String> sex = FXCollections.observableArrayList(
                "男","女"
        );
        sexComboBox.setItems(sex);
    }

    /**
     * 初始化民族选项
     */
    private void initializeEthnicOptions() {
        ObservableList<String> ethnics = FXCollections.observableArrayList(
                "汉族", "蒙古族", "回族", "藏族", "维吾尔族", "苗族", "彝族", "壮族", "布依族", "朝鲜族", "满族", "侗族", "瑶族", "白族", "土家族", "哈尼族", "哈萨克族", "傣族", "黎族", "傈僳族", "佤族", "畲族", "高山族", "拉祜族", "水族", "东乡族", "纳西族", "景颇族", "柯尔克孜族", "土族", "达斡尔族", "仫佬族", "羌族", "布朗族", "撒拉族", "毛南族", "仡佬族", "锡伯族", "阿昌族", "普米族", "塔吉克族", "怒族", "乌孜别克族", "俄罗斯族", "鄂温克族", "德昂族", "保安族", "裕固族", "京族", "塔塔尔族", "独龙族", "鄂伦春族", "赫哲族", "门巴族", "珞巴族", "基诺族", "其他"
        );
        ethnicComboBox.setItems(ethnics);
    }
    
    /**
     * 初始化国籍选项
     */
    private void initializeNationOptions() {
        ObservableList<String> nations = FXCollections.observableArrayList(
                "中国", "美国", "英国", "法国", "德国", "日本", "韩国", "俄罗斯", 
                "加拿大", "澳大利亚", "新西兰", "印度", "巴西", "南非", "其他"
        );
        nationComboBox.setItems(nations);
        nationComboBox.setValue("中国");
    }
    
    /**
     * 初始化政治面貌选项
     */
    private void initializePoliticsStatusOptions() {
        ObservableList<String> politicsStatuses = FXCollections.observableArrayList(
                "中共党员", "中共预备党员", "共青团员","群众"
        );
        politicsStatusComboBox.setItems(politicsStatuses);
    }
    
    /**
     * 设置表单验证
     */
    private void setupValidation() {
        // 为每个输入字段添加失去焦点时的验证
        setupFieldValidation(sduIdField, "工号不能为空", this::validateSduId);
        setupFieldValidation(usernameField, "姓名不能为空", this::validateUsername);
        setupFieldValidation(emailField, "邮箱格式不正确", this::validateEmail);
        setupFieldValidation(phoneField, "电话号码格式不正确", this::validatePhone);
    }
    
    /**
     * 为输入字段设置验证
     */
    private void setupFieldValidation(TextField field, String defaultErrorMessage, ValidationFunction validator) {
        // 创建错误提示标签
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        
        // 找到字段的父容器，并添加错误标签
        if (field.getParent() instanceof HBox) {
            HBox parent = (HBox) field.getParent();
            if (parent.getParent() instanceof VBox) {
                VBox container = (VBox) parent.getParent();
                int index = container.getChildren().indexOf(parent);
                if (index >= 0 && index < container.getChildren().size()) {
                    container.getChildren().add(index + 1, errorLabel);
                }
            }
        }
        
        // 存储错误标签引用
        errorLabels.put(field, errorLabel);
        
        // 设置失去焦点时的验证
        field.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // 当失去焦点时
                String result = validator.validate(field.getText());
                if (result != null) {
                    showFieldError(field, result);
                } else {
                    hideFieldError(field);
                }
            }
        });
    }
    
    /**
     * 显示字段错误
     */
    private void showFieldError(TextField field, String errorMessage) {
        Label errorLabel = errorLabels.get(field);
        if (errorLabel != null) {
            errorLabel.setText(errorMessage);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            field.getStyleClass().add("field-error");
        }
    }
    
    /**
     * 隐藏字段错误
     */
    private void hideFieldError(TextField field) {
        Label errorLabel = errorLabels.get(field);
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            field.getStyleClass().remove("field-error");
        }
    }
    
    /**
     * 验证工号
     */
    private String validateSduId(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "工号不能为空";
        }
        if (!Pattern.matches("^[0-9]{5,12}$", value)) {
            return "工号格式不正确，应为5-12位数字";
        }
        return null;
    }
    /**
     * 验证工号
     */
    private String validateSex(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "性别不能为空";
        }
        return null;
    }
    
    /**
     * 验证姓名
     */
    private String validateUsername(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "姓名不能为空";
        }
        if (value.length() < 2 || value.length() > 20) {
            return "姓名长度应在2-20个字符之间";
        }
        return null;
    }
    
    /**
     * 验证邮箱
     */
    private String validateEmail(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "邮箱不能为空";
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!Pattern.matches(emailRegex, value)) {
            return "邮箱格式不正确";
        }
        return null;
    }
    
    /**
     * 验证电话
     */
    private String validatePhone(String value) {
        if (!Pattern.matches("^1[3-9]\\d{9}$", value)) {
            return "手机号格式不正确";
        }
        return null;
    }
    
    /**
     * 验证表单
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // 验证工号
        String sduIdError = validateSduId(sduIdField.getText());
        if (sduIdError != null) {
            showFieldError(sduIdField, sduIdError);
            isValid = false;
        } else {
            hideFieldError(sduIdField);
        }
        //验证性别
        String sexError = validateSex(sexComboBox.getValue());
        if(sexError != null){
            ShowMessage.showErrorMessage("性别不能为空",sexError);
        }
        // 验证姓名
        String usernameError = validateUsername(usernameField.getText());
        if (usernameError != null) {
            showFieldError(usernameField, usernameError);
            isValid = false;
        } else {
            hideFieldError(usernameField);
        }
        
        // 验证邮箱
        String emailError = validateEmail(emailField.getText());
        if (emailError != null) {
            showFieldError(emailField, emailError);
            isValid = false;
        } else {
            hideFieldError(emailField);
        }
        
        // 验证电话
        String phoneError = validatePhone(phoneField.getText());
        if (phoneError != null) {
            showFieldError(phoneField, phoneError);
            isValid = false;
        } else {
            hideFieldError(phoneField);
        }
        
        // 验证学院选择
        if (collegeComboBox.getValue() == null) {
            ShowMessage.showErrorMessage("验证失败", "请选择所属学院");
            isValid = false;
        }
          // 验证专业选择
        if (majorComboBox.getValue() == null) {
            ShowMessage.showErrorMessage("验证失败", "请选择所属专业");
            isValid = false;
        }

        // 验证民族选择
        if (ethnicComboBox.getValue() == null) {
            ShowMessage.showErrorMessage("验证失败", "请选择民族");
            isValid = false;
        }
        
        // 验证国籍选择
        if (nationComboBox.getValue() == null) {
            ShowMessage.showErrorMessage("验证失败", "请选择国籍");
            isValid = false;
        }
        
        // 验证政治面貌选择
        if (politicsStatusComboBox.getValue() == null) {
            ShowMessage.showErrorMessage("验证失败", "请选择政治面貌");
            isValid = false;
        }
        
        return isValid;
    }
    /***
     * 处理专业转换
     */
    private String transMajor(String major){
        switch (major){
            case "软件工程":
                return "MAJOR_0";
            case "数字媒体技术":
                return "MAJOR_1";
            case "大数据":
                return "MAJOR_2";
            case "人工智能国际班" :
                return "MAJOR_3";
            default:
                return "MAJOR_-1";
        }
    }
    
    /**
     * 处理提交按钮点击事件
     */
    @FXML
    private void handleSubmit() {
        if (!validateForm()) {
            return;
        }
        
        // 显示加载状态或禁用按钮
        submitButton.setDisable(true);
        
        // 获取表单数据
        Map<String, String> params = new HashMap<>();
        params.put("username", usernameField.getText().trim());
        params.put("college", collegeComboBox.getValue());
        params.put("major", transMajor(majorComboBox.getValue()));
        params.put("email", emailField.getText().trim());
        params.put("ethnic", ethnicComboBox.getValue());
        params.put("password", "123456");
        params.put("nation", nationComboBox.getValue());
        params.put("phone", phoneField.getText().trim());
        params.put("PoliticsStatus", politicsStatusComboBox.getValue());
        params.put("SDUId", sduIdField.getText().trim());
        params.put("sex",sexComboBox.getValue());
        params.put("permission","2");

        

        // 调用API
        NetworkUtils.postAsync("/admin/addUser", params, null )
            .thenAcceptAsync(response -> {
                Platform.runLater(() -> {
                    try {
                        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
                        
                        if (jsonResponse.has("code") && jsonResponse.get("code").getAsInt() == 200) {
                            ShowMessage.showInfoMessage("成功", "成功添加学生：" + usernameField.getText());
                            stage.close(); // 关闭窗口
                        } else {
                            String errorMsg = "添加学生失败";
                            if (jsonResponse.has("msg")) {
                                errorMsg += ": " + jsonResponse.get("msg").getAsString();
                            }
                            ShowMessage.showErrorMessage("错误", errorMsg);
                            submitButton.setDisable(false);
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "处理添加学生响应失败", e);
                        ShowMessage.showErrorMessage("处理错误", "无法处理服务器响应：" + e.getMessage());
                        submitButton.setDisable(false);
                    }
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "添加学生请求失败", ex);
                    ShowMessage.showErrorMessage("网络错误", "添加学生请求失败：" + ex.getMessage());
                    submitButton.setDisable(false);
                });
                return null;
            });
    }

    /**
     * 设置舞台引用
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void handleCancel(ActionEvent actionEvent) {
        stage.close();
    }

    /**
     * 验证函数接口
     */
    @FunctionalInterface
    private interface ValidationFunction {
        String validate(String value);
    }
}
