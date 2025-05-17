package com.work.javafx.controller.teacher;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.work.javafx.controller.admin.AddNewAnnouncementController;
import com.work.javafx.controller.admin.editAnnouncementController;
import com.work.javafx.util.NetworkUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.DialogPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

// 移除对 TeacherBaseViewController 的继承
public class TeacherHomePageController implements Initializable {
    //卡片功能按钮
    @FXML private Label CompleteCourseSchedule;
    @FXML private Label AttendanceManagement;
    @FXML private VBox noticeListContainer; // 公告条目容器
    // @FXML private Button publishNewNoticeButton; // 发布新公告按钮 - FXML中已通过onAction处理

    // 基础控制器引用
    private TeacherBaseViewController baseController;
    private final Gson gson = new Gson();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //卡片功能按钮绑定
        CompleteCourseSchedule.setOnMouseClicked(this::changetocourseScheduleManagement);
        AttendanceManagement.setOnMouseClicked(this::changetoAttendanceManagement);
        
        loadNotices();
    }

    public void setBaseController(TeacherBaseViewController controller) {
        this.baseController = controller;
    }
    //查看完整课表
    private void changetocourseScheduleManagement(MouseEvent mouseEvent) {
        if (baseController != null) baseController.switchTocourseScheduleManagement();
    }
    //切换到考勤管理
    private void changetoAttendanceManagement(MouseEvent mouseEvent) {
        if (baseController != null) baseController.switchToAttendanceManagement();
    }


    private void loadNotices() {
        Platform.runLater(() -> noticeListContainer.getChildren().clear());
        Map<String, String> params = new HashMap<>();
        params.put("Status", "0");

        NetworkUtils.get("/notice/getTeacherNoticeList", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                Platform.runLater(() -> {
                    try {
                        JsonObject res = gson.fromJson(result, JsonObject.class);
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            if (res.has("data") && res.get("data").isJsonArray()) {
                                JsonArray noticesArray = res.get("data").getAsJsonArray();
                                if (noticesArray.isEmpty()) {
                                    displayInfoMessage("当前没有公告。");
                                    return;
                                }
                                noticeListContainer.getChildren().clear();

                                DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                                for (JsonElement noticeElement : noticesArray) {
                                    JsonObject noticeObject = noticeElement.getAsJsonObject();
                                    int id = noticeObject.get("id").getAsInt();
                                    String title = noticeObject.has("title") ? noticeObject.get("title").getAsString() : "无标题";
                                    String content = noticeObject.get("content").getAsString();
                                    int isTop = noticeObject.has("isTop") ? noticeObject.get("isTop").getAsInt() : 0;
                                    int visibleScope = noticeObject.has("visibleScope") ? noticeObject.get("visibleScope").getAsInt() : 0; // 或某些默认范围
                                    String publishTimeStr = noticeObject.has("publishTime") ? noticeObject.get("publishTime").getAsString() : "";
                                    String creatorName = noticeObject.has("creatorName") ? noticeObject.get("creatorName").getAsString() : "系统";
                                    
                                    String formattedPublishTime = "";
                                    if (!publishTimeStr.isEmpty()) {
                                        try {
                                            LocalDateTime dateTime = LocalDateTime.parse(publishTimeStr, inputFormatter);
                                            formattedPublishTime = dateTime.format(outputFormatter);
                                        } catch (DateTimeParseException e) {
                                            System.err.println("日期解析错误: " + publishTimeStr + " - " + e.getMessage());
                                            formattedPublishTime = publishTimeStr; // 回退到原始字符串
                                        }
                                    }
                                    
                                    String timeAndCreatorInfo = "发布时间：" + formattedPublishTime + " | 发布人：" + creatorName;
                                    addNoticeItem(id, title, content, isTop, visibleScope, timeAndCreatorInfo);
                                }
                            } else {
                                displayErrorMessage("公告列表为空或格式错误。");
                            }
                        } else {
                            String msg = res.has("msg") ? res.get("msg").getAsString() : "未知错误";
                            displayErrorMessage("获取公告列表失败: " + msg);
                        }
                    } catch (JsonParseException e) {
                        displayErrorMessage("获取公告列表失败: 服务器响应格式错误。");
                        System.err.println("loadNotices中的JSON解析错误: " + e.getMessage());
                    } catch (Exception e) {
                        String errorMessage = e.getMessage();
                        try {
                            if (errorMessage != null && errorMessage.contains("{") && errorMessage.contains("}")) {
                                JsonObject errorJson = gson.fromJson(errorMessage.substring(errorMessage.indexOf("{")), JsonObject.class);
                                if (errorJson.has("msg")) {
                                    errorMessage = errorJson.get("msg").getAsString();
                                }
                            }
                        } catch (JsonParseException jsonEx) {

                        }
                        displayErrorMessage("获取公告列表失败: " + errorMessage);
                        System.err.println("loadNotices中发生错误: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {

                String errorMessage = e.getMessage();
                try {
                     if (errorMessage != null && errorMessage.contains("{") && errorMessage.contains("}")) {
                        JsonObject errorJson = gson.fromJson(errorMessage.substring(errorMessage.indexOf("{")), JsonObject.class);
                         if (errorJson.has("msg")) {
                            errorMessage = errorJson.get("msg").getAsString();
                        }
                    }
                } catch (JsonParseException jsonEx) {

                }
                displayErrorMessage("网络错误，无法加载公告: " + errorMessage);
                System.err.println("loadNotices中网络故障: " + e.getMessage());
            }
        });
    }

    private void addNoticeItem(int id, String title, String content, int isTop, int visibleScope, String timeInfo) {
        HBox noticeItem = new HBox();
        noticeItem.getStyleClass().add("notice-item");
        noticeItem.setOnMouseClicked(e -> showNoticeDetails(title, content));

        StackPane iconPane = new StackPane();
        iconPane.getStyleClass().add("notice-icon"); 
    

        VBox noticeDetailsVBox = new VBox();
        noticeDetailsVBox.getStyleClass().add("notice-content");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("notice-title");
        if (isTop == 1) { 
            titleLabel.getStyleClass().add("notice-title-top");
        }

        Label timeLabel = new Label(timeInfo);
        timeLabel.getStyleClass().add("notice-time");

        noticeDetailsVBox.getChildren().addAll(titleLabel, timeLabel);

        HBox actions = new HBox();
        actions.getStyleClass().add("notice-actions");


        Button editBtn = new Button("编辑");
        editBtn.getStyleClass().addAll("notice-btn", "edit-btn");
        editBtn.setOnAction(e -> {
            e.consume(); // 消费事件，防止触发noticeItem的点击事件
            editNotice(id, title, content, isTop, visibleScope);
        });

        Button deleteBtn = new Button("删除");
        deleteBtn.getStyleClass().addAll("notice-btn", "delete-btn");
        deleteBtn.setOnAction(e -> {
            e.consume(); // 消费事件
            deleteNotice(id);
        });

        actions.getChildren().addAll(editBtn, deleteBtn);

        HBox.setHgrow(noticeDetailsVBox, javafx.scene.layout.Priority.ALWAYS); // 使内容占据可用空间
        noticeItem.getChildren().addAll(iconPane, noticeDetailsVBox, actions);
        noticeListContainer.getChildren().add(noticeItem);
    }

    private void showNoticeDetails(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("公告详情");
        alert.setHeaderText(title);

        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(300);
        textArea.setPrefWidth(500);

        alert.setGraphic(null); // 移除默认图标

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setContent(textArea);
        dialogPane.setPrefWidth(550);
        dialogPane.setPrefHeight(400);

        try {
            // 尝试加载通用对话框样式或教师特定样式
            URL cssResource = getClass().getResource("/com/work/javafx/css/admin/DialogStyles.css"); // 复用管理员对话框样式
            if (cssResource != null) {
                dialogPane.getStylesheets().add(cssResource.toExternalForm());
                dialogPane.getStyleClass().add("notice-dialog"); // 公告对话框的通用样式类
            } else {
                System.err.println("DialogStyles.css 未找到。");
            }
        } catch (Exception e) {
            System.err.println("Error loading CSS for dialog: " + e.getMessage());
        }

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setResizable(true);

        alert.showAndWait();
    }

    @FXML
    private void publishNewNotice() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/addNewAnnouncement.fxml"));
            Parent root = loader.load();

            AddNewAnnouncementController controller = loader.getController();
            if (controller == null) {
                System.err.println("AddNewAnnouncementController 为空。");
                displayErrorMessage("无法打开发布公告窗口。");
                return;
            }
            
            Stage popupStage = new Stage();
            popupStage.initStyle(StageStyle.DECORATED);
            popupStage.setTitle("发布新公告");
            popupStage.setScene(new Scene(root, 800, 600));
            popupStage.setMinWidth(700);
            popupStage.setMinHeight(550);

            controller.setStage(popupStage);
            controller.setOnPublishCompleteCallback(this::loadNotices);

            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            displayErrorMessage("打开公告发布窗口失败: " + e.getMessage());
        }  catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("发生意外错误: " + e.getMessage());
        }
    }
    
    private void editNotice(int noticeId, String title, String content, int isTop, int visibleScope) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/editAnnouncement.fxml"));
            Parent root = loader.load();

            editAnnouncementController controller = loader.getController();
            if (controller == null) {
                System.err.println("editAnnouncementController 为空。请检查FXML和控制器。");
                 displayErrorMessage("无法打开编辑公告窗口。");
                return;
            }
            
            controller.initData(noticeId, title, content, isTop, visibleScope);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("编辑公告");
            stage.setScene(new Scene(root, 800, 600));
            controller.setStage(stage);
            controller.setOnEditCompleteCallback(this::loadNotices);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            displayErrorMessage("打开公告编辑窗口失败: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("发生意外错误: " + e.getMessage());
        }
    }

    private void deleteNotice(int noticeId) {

        System.out.println("尝试删除公告 ID: " + noticeId);

        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("确认删除");
        confirmationDialog.setHeaderText("您确定要删除此公告吗？");
        confirmationDialog.setContentText("ID: " + noticeId);

        confirmationDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Map<String, Object> params = new HashMap<>();
                params.put("id", noticeId);
                String jsonRequestBody = gson.toJson(params);

                NetworkUtils.post("/notice/delete", jsonRequestBody, new NetworkUtils.Callback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        Platform.runLater(() -> {
                            try {
                                JsonObject res = gson.fromJson(result, JsonObject.class);
                                if (res.has("code") && res.get("code").getAsInt() == 200) {
                                    displayInfoMessage("公告删除成功。");
                                    loadNotices(); // 刷新列表
                                } else {
                                    String msg = res.has("msg") ? res.get("msg").getAsString() : "未知错误";
                                    displayErrorMessage("删除失败: " + msg);
                                }
                            } catch (JsonParseException e) {
                                displayErrorMessage("删除失败: 响应格式错误。");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Platform.runLater(() -> displayErrorMessage("删除失败: " + e.getMessage()));
                    }
                });
            }
        });
    }

    private void displayErrorMessage(String message) {
        Platform.runLater(() -> {
            noticeListContainer.getChildren().clear();
            Label errorLabel = new Label(message);
            errorLabel.getStyleClass().add("error-message");
            errorLabel.setStyle("-fx-text-fill: red; -fx-padding: 10px; -fx-alignment: center;");
            noticeListContainer.getChildren().add(errorLabel);
        });
    }

    private void displayInfoMessage(String message) {
        Platform.runLater(() -> {
            noticeListContainer.getChildren().clear();
            Label infoLabel = new Label(message);
            infoLabel.getStyleClass().add("info-message");
            infoLabel.setStyle("-fx-padding: 10px; -fx-alignment: center;");
            noticeListContainer.getChildren().add(infoLabel);
        });
    }
}