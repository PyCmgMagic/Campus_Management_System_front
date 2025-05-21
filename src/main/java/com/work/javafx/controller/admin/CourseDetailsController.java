package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.work.javafx.model.ClassSimpleInfo;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ShowMessage;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class CourseDetailsController implements Initializable {
    private Stage stage;
    private int courseId;
    private JsonObject courseData;
    private Gson gson = new Gson();
    private boolean isApplicable = false; // 是否为待审批状态

    // UI元素
    @FXML private Label courseIdLabel;
    @FXML private Label courseNameLabel;
    @FXML private Label collegeLabel;
    @FXML private Label courseTypeLabel;
    @FXML private Label creditLabel;
    @FXML private Label teacherIdLabel;
    @FXML private Label classroomLabel;
    @FXML private Label termLabel;
    @FXML private Label weekRangeLabel;
    @FXML private Label capacityLabel;
    @FXML private Label periodLabel;
    @FXML private Label timeLabel;
    @FXML private Label examTypeLabel;
    @FXML private Label publishStatusLabel;
    @FXML private Label classNumLabel;
    @FXML private Label regularRatioLabel;
    @FXML private Label finalRatioLabel;
    @FXML private Label introLabel;
    @FXML private Label courseStatusLabel;
    
    // 按钮
    @FXML private Button closeButton;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button backButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始状态，隐藏审批按钮
        approveButton.setVisible(false);
        rejectButton.setVisible(false);
    }

    /**
     * 加载课程详情
     * @param courseId 课程ID
     */
    public void loadCourseDetails(int courseId) {
        this.courseId = courseId;
        
        // 调用API获取课程详情
        NetworkUtils.get("/class/detail/" + courseId, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject response = gson.fromJson(result, JsonObject.class);
                
                if (response.has("code") && response.get("code").getAsInt() == 200) {
                    courseData = response.getAsJsonObject("data");
                    updateUI();
                } else {
                    // 显示错误信息
                    String errorMsg = response.has("msg") ? response.get("msg").getAsString() : "获取课程详情失败";
                    showError(errorMsg);
                }
            }

            @Override
            public void onFailure(Exception e) {
                showError("网络错误: " + e.getMessage());
            }
        });
    }
    
    /**
     * 设置是否为审批页面
     * @param isApplicable 是否是待审批课程
     */
    public void setApplicable(boolean isApplicable) {
        this.isApplicable = isApplicable;
        
        // 如果是待审批课程，显示审批按钮
        if (isApplicable) {
            approveButton.setVisible(true);
            rejectButton.setVisible(true);
            closeButton.setVisible(false);
        } else {
            approveButton.setVisible(false);
            rejectButton.setVisible(false);
            closeButton.setVisible(true);
        }
    }

    /**
     * 更新UI显示
     */
    private void updateUI() {
        if (courseData == null) return;
        
        // 基本信息
        courseIdLabel.setText(nullSafeGetString(courseData, "id"));
        courseNameLabel.setText(nullSafeGetString(courseData, "name"));
        collegeLabel.setText(nullSafeGetString(courseData, "college"));
        courseTypeLabel.setText(nullSafeGetString(courseData, "type"));
        creditLabel.setText(nullSafeGetString(courseData, "point"));
        teacherIdLabel.setText(nullSafeGetString(courseData, "teacherId"));
        classNumLabel.setText(nullSafeGetString(courseData, "classNum"));

        // 课程信息
        classroomLabel.setText(nullSafeGetString(courseData, "classroom"));
        termLabel.setText(nullSafeGetString(courseData, "term"));
        
        // 构建周次范围
        String weekStart = nullSafeGetString(courseData, "weekStart");
        String weekEnd = nullSafeGetString(courseData, "weekEnd");
        weekRangeLabel.setText(weekStart + " - " + weekEnd + " 周");
        
        capacityLabel.setText(nullSafeGetString(courseData, "capacity"));
        periodLabel.setText(nullSafeGetString(courseData, "period"));
        
        // 处理上课时间，可能为null
        String timeValue = nullSafeGetString(courseData, "time");
        timeLabel.setText(timeValue.isEmpty() ? "待安排" : timeValue);
        
        // 考核信息
        int examType = courseData.has("examination") ? courseData.get("examination").getAsInt() : 0;
        examTypeLabel.setText(examType == 1 ? "考试" : "考查");
        
        boolean published = courseData.has("published") && courseData.get("published").getAsBoolean();
        publishStatusLabel.setText(published ? "已发布" : "未发布");
        
        // 成绩比例
        double regularRatio = courseData.has("regularRatio") ? courseData.get("regularRatio").getAsDouble() : 0;
        double finalRatio = courseData.has("finalRatio") ? courseData.get("finalRatio").getAsDouble() : 0;
        regularRatioLabel.setText(formatPercentage(regularRatio));
        finalRatioLabel.setText(formatPercentage(finalRatio));
        
        // 课程简介
        String intro = nullSafeGetString(courseData, "intro");
        introLabel.setText(intro.isEmpty() ? "暂无课程简介" : intro);
        
        // 课程状态
        String status = nullSafeGetString(courseData, "status");
        courseStatusLabel.setText(status);
        
        // 设置状态标签样式
        setStatusStyle(status);
    }
    
    /**
     * 设置状态标签样式
     */
    private void setStatusStyle(String status) {
        // 默认样式
        courseStatusLabel.getStyleClass().remove("status-pending");
        courseStatusLabel.getStyleClass().remove("status-approved");
        courseStatusLabel.getStyleClass().remove("status-rejected");
        
        // 根据状态设置样式
        switch (status) {
            case "待审核":
                courseStatusLabel.getStyleClass().add("status-pending");
                break;
            case "已通过":
                courseStatusLabel.getStyleClass().add("status-approved");
                break;
            case "已拒绝":
                courseStatusLabel.getStyleClass().add("status-rejected");
                break;
        }
    }
    
    /**
     * 处理返回按钮事件
     */
    @FXML
    private void handleBackAction() {
        closeWindow();
    }
    
    /**
     * 处理关闭按钮事件
     */
    @FXML
    private void handleCloseAction() {
        closeWindow();
    }

    private void fetchAvailableClasses(java.util.function.Consumer<List<ClassSimpleInfo>> callback) {
        String classesUrl = "/section/getSectionListAll?page=1&size=500";
        NetworkUtils.get(classesUrl, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200 && res.has("data")) {
                        java.lang.reflect.Type listType = new TypeToken<ArrayList<ClassSimpleInfo>>(){}.getType();
                        JsonObject data = res.getAsJsonObject("data");
                        List<ClassSimpleInfo> fetchedClasses = gson.fromJson(data.get("section"), listType);
                        callback.accept(fetchedClasses);
                    } else {
                        ShowMessage.showErrorMessage("获取班级失败", "无法解析班级列表: " + (res.has("msg") ? res.get("msg").getAsString() : "格式错误"));
                        callback.accept(new ArrayList<>());
                    }
                } catch (Exception ex) {
                    JsonObject res = gson.fromJson(ex.getMessage().substring(ex.getMessage().indexOf("{")), JsonObject.class);
                    ShowMessage.showErrorMessage("获取班级失败", "网络请求获取班级列表失败: " + res.get("msg").getAsString());
                    callback.accept(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Exception e) {
                JsonObject res = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")), JsonObject.class);
                ShowMessage.showErrorMessage("获取班级失败", "网络请求获取班级列表失败: " + res.get("msg").getAsString());
                callback.accept(new ArrayList<>());
            }
        });
    }
   
    /**
     * 处理通过按钮事件
     */
    @FXML
    private void handleApproveAction() {
        if (showConfirmDialog("确认操作", "确定要通过课程 " + courseData.get("name").getAsString() + " 的申请吗？")) {
            if(courseData.get("type").getAsString().equals("必修")){
                fetchAvailableClasses(availableClasses -> {
                    if (availableClasses == null || availableClasses.isEmpty()) {
                        ShowMessage.showErrorMessage("操作失败", "无法获取可用班级列表或列表为空。");
                        return;
                    }

                    // 创建并显示自定义对话框
                    Dialog<ClassSimpleInfo> dialog = new Dialog<>();
                    dialog.setTitle("选择班级");
                    dialog.setHeaderText("请为课程 '" +courseData.get("name").getAsString()  + "' 选择一个班级进行绑定。");

                    // 设置按钮
                    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                    // 创建 ComboBox
                    ComboBox<ClassSimpleInfo> classComboBox = new ComboBox<>();
                    classComboBox.setItems(FXCollections.observableArrayList(availableClasses));
                    classComboBox.setPromptText("请选择班级");
                    if (!availableClasses.isEmpty()) {
                        classComboBox.setValue(availableClasses.get(0)); // 默认选中第一个
                    }

                    // 设置对话框内容
                    VBox vbox = new VBox(10); // 10是间距
                    vbox.getChildren().addAll(new Label("选择要绑定的班级:"), classComboBox);
                    dialog.getDialogPane().setContent(vbox);

                    // 启用/禁用 OK 按钮，直到选择了班级
                    javafx.scene.Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
                    okButton.setDisable(classComboBox.getValue() == null); // 如果初始没选中，则禁用
                    classComboBox.valueProperty().addListener((obs, oldVal, newVal) -> okButton.setDisable(newVal == null));
                    
                    dialog.setResultConverter(dialogButton -> {
                        if (dialogButton == ButtonType.OK) {
                            return classComboBox.getValue();
                        }
                        return null;
                    });

                    Optional<ClassSimpleInfo> result = dialog.showAndWait();

                    result.ifPresent(selectedClass -> {
                        // 步骤5: 用户选择了班级并点击了OK，发送批准请求
                        String selectedClassId = selectedClass.getId()+""; // 获取选中的班级ID
                        Map<String,String> params = new HashMap<>();
                        params.put("status","1");
                        params.put("ccourseId",selectedClassId);
                        String url = "/class/approve/" + courseData.get("id").getAsString();
                        // 发送审批请求
                        NetworkUtils.post(url,params,"", new NetworkUtils.Callback<String>() {
                            @Override
                            public void onSuccess(String result) {
                                JsonObject response = gson.fromJson(result, JsonObject.class);
                                if (response.has("code") && response.get("code").getAsInt() == 200) {
                                    // 审批成功，关闭窗口
                                    showSuccessDialog("课程审批成功");
                                    closeWindow();
                                } else {
                                    // 显示错误信息
                                    String errorMsg = response.has("msg") ? response.get("msg").getAsString() : "课程审批失败";
                                    showError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                showError("网络错误: " + e.getMessage());
                            }
                        });
                    });
                });
            }else{
                Map<String,String> params = new HashMap<>();
                params.put("status","1");
                String url = "/class/approve/" + courseData.get("id").getAsString();
                NetworkUtils.post(url,params,"", new NetworkUtils.Callback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        JsonObject response = gson.fromJson(result, JsonObject.class);
                        if (response.has("code") && response.get("code").getAsInt() == 200) {
                            // 审批成功，关闭窗口
                            showSuccessDialog("课程审批成功");
                            closeWindow();
                        } else {
                            // 显示错误信息
                            String errorMsg = response.has("msg") ? response.get("msg").getAsString() : "课程审批失败";
                            showError(errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        showError("网络错误: " + e.getMessage());
                    }
                });
            }

    }
    }
    
    /**
     * 处理拒绝按钮事件
     */
    @FXML
    private void handleRejectAction() {
        if (courseData == null) return;
        // 创建文本输入对话框
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("拒绝课程申请");
        dialog.setHeaderText("请输入拒绝原因");
        dialog.setContentText("拒绝原因:");
        
        // 获取用户输入的拒绝原因
        Optional<String> result = dialog.showAndWait();
        // 只有当用户提供了拒绝原因时才继续
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String rejectReason = result.get().trim();
            int id = courseData.get("id").getAsInt();
            int classNum = courseData.get("classNum").getAsInt();
            String url = "/class/approve/"+id+"?status=2&classNum="+classNum+"&reason="+rejectReason;

            NetworkUtils.post(url, "", new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    JsonObject response = gson.fromJson(result, JsonObject.class);
                    if (response.has("code") && response.get("code").getAsInt() == 200) {
                        // 拒绝成功，关闭窗口
                        showSuccessDialog("已拒绝课程申请");
                        closeWindow();
                    } else {
                        // 显示错误信息
                        String errorMsg = response.has("msg") ? response.get("msg").getAsString() : "拒绝课程申请失败";
                        showError(errorMsg);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    showError("网络错误: " + e.getMessage());
                }
            });
        } else {
            // 如果用户没有提供拒绝原因，显示提示
            showError("必须提供拒绝原因");
        }
    }
    
    /**
     * 显示成功对话框
     */
    private void showSuccessDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("操作成功");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 显示错误信息
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("操作失败");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

        private boolean showConfirmDialog(String title, String content) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);

            return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
        }
    /**
     * 安全获取JsonObject中的字符串值
     */
    private String nullSafeGetString(JsonObject json, String key) {
        if (json == null || !json.has(key) || json.get(key).isJsonNull()) {
            return "";
        }
        return json.get(key).getAsString();
    }
    
    /**
     * 格式化百分比
     */
    private String formatPercentage(double value) {
        return String.format("%.0f%%", value * 100);
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
