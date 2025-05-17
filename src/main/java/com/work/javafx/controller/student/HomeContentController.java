package com.work.javafx.controller.student;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.work.javafx.util.NetworkUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 首页内容控制器
 * 负责处理首页内容区域的交互逻辑
 */
public class HomeContentController implements Initializable {
    
    @FXML
    private Label dateText;
    @FXML
    private VBox noticeListContainer; // 公告列表容器

    private final Gson gson = new Gson();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("首页内容初始化成功");
        
        // 更新当前日期显示
        updateCurrentDate();
        // 加载公告
        loadNotices();
    }
    
    /**
     * 更新当前日期显示
     */
    private void updateCurrentDate() {
        // 如果界面上有日期标签，则更新为当前日期
        if (dateText != null) {
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE");
            String formattedDate = now.format(formatter);
            dateText.setText(formattedDate);
        }
    }
    
    /**
     * 切换到课表查询
     * 通过获取基础视图控制器实例来切换视图
     */
    @FXML
    private void switchToCourseSchedule() {

        try {
            // 获取当前场景
            Scene scene = dateText.getScene();
            if (scene != null) {
                // 获取基础视图控制器实例
                Object userData = scene.getUserData();
                if (userData instanceof StudentBaseViewController) {
                    StudentBaseViewController baseController = (StudentBaseViewController) userData;
                    // 调用基础视图控制器的方法切换到课表查询
                    baseController.switchToCourseSchedule();
                } else {
                    System.out.println("无法获取基础视图控制器：userData不是BaseViewController类型");
                }
            } else {
                System.out.println("无法获取场景");
            }
        } catch (Exception e) {
            System.out.println("切换到课表查询时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadNotices() {
        Platform.runLater(() -> noticeListContainer.getChildren().clear());
        Map<String, String> params = new HashMap<>();
        // 学生端通常不需要区分状态，获取所有可见的公告
        // 如果有特定参数需求，如只显示已发布的，可以在这里添加，例如 params.put("status", "published");

        NetworkUtils.get("/notice/getStudentNoticeList", params, new NetworkUtils.Callback<String>() {
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
                                    String title = noticeObject.has("title") ? noticeObject.get("title").getAsString() : "无标题";
                                    String content = noticeObject.get("content").getAsString();
                                    String publishTimeStr = noticeObject.has("publishTime") ? noticeObject.get("publishTime").getAsString() : "";
                                    String creatorName = noticeObject.has("creatorName") ? noticeObject.get("creatorName").getAsString() : "无";

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
                                    addNoticeItem(title, content, timeAndCreatorInfo);
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
                        String errorMessage = e.getMessage() != null ? e.getMessage() : "发生未知错误";
                         try {
                            if (errorMessage.contains("{") && errorMessage.contains("}")) {
                                JsonObject errorJson = gson.fromJson(errorMessage.substring(errorMessage.indexOf("{")), JsonObject.class);
                                if (errorJson.has("msg")) {
                                    errorMessage = errorJson.get("msg").getAsString();
                                }
                            }
                        } catch (JsonParseException jsonEx) {
                            // 如果错误消息不是JSON格式，保持原样
                        }
                        displayErrorMessage("获取公告列表失败: " + errorMessage);
                        System.err.println("loadNotices中发生错误: " + e.toString());
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : "网络错误";
                try {
                     if (errorMessage.contains("{") && errorMessage.contains("}")) {
                        JsonObject errorJson = gson.fromJson(errorMessage.substring(errorMessage.indexOf("{")), JsonObject.class);
                         if (errorJson.has("msg")) {
                            errorMessage = errorJson.get("msg").getAsString();
                        }
                    }
                } catch (JsonParseException jsonEx) {
                     // 如果错误消息不是JSON格式，保持原样
                }
                displayErrorMessage("网络错误，无法加载公告: " + errorMessage);
                System.err.println("loadNotices中网络故障: " + e.toString());
                e.printStackTrace();
            }
        });
    }

    private void addNoticeItem(String title, String content, String timeInfo) {
        HBox noticeItem = new HBox();
        noticeItem.getStyleClass().add("notice-item");
        noticeItem.setOnMouseClicked(e -> showNoticeDetails(title, content));
        StackPane icon = new StackPane(); // 图标容器
        icon.getStyleClass().add("notice-icon");
        VBox noticeDetailsVBox = new VBox();
        noticeDetailsVBox.getStyleClass().add("notice-content");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("notice-title");

        Label timeLabel = new Label(timeInfo);
        timeLabel.getStyleClass().add("notice-time");

        noticeDetailsVBox.getChildren().addAll(titleLabel, timeLabel);
        HBox.setHgrow(noticeDetailsVBox, javafx.scene.layout.Priority.ALWAYS);
        noticeItem.getChildren().addAll(icon,noticeDetailsVBox);
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

        alert.setGraphic(null); 

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setContent(textArea);
        dialogPane.setPrefWidth(550);
        dialogPane.setPrefHeight(400);

        try {
            // Attempt to load admin's dialog style directly
            URL cssResource = getClass().getResource("/com/work/javafx/css/admin/DialogStyles.css"); 
            if (cssResource != null) {
                dialogPane.getStylesheets().add(cssResource.toExternalForm());
                dialogPane.getStyleClass().add("notice-dialog");
            } else {
                System.err.println("DialogStyles.css not found.");
            }
        } catch (Exception e) {
            System.err.println("Error loading CSS for dialog: " + e.getMessage());
        }

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setResizable(true);

        alert.showAndWait();
    }

    private void displayErrorMessage(String message) {
        Platform.runLater(() -> {
            if (noticeListContainer != null) {
                noticeListContainer.getChildren().clear();
                Label errorLabel = new Label(message);
                errorLabel.getStyleClass().add("error-message");
                errorLabel.setStyle("-fx-text-fill: red; -fx-padding: 10px; -fx-alignment: center;");
                noticeListContainer.getChildren().add(errorLabel);
            }
        });
    }

    private void displayInfoMessage(String message) {
        Platform.runLater(() -> {
            if (noticeListContainer != null) {
                noticeListContainer.getChildren().clear();
                Label infoLabel = new Label(message);
                infoLabel.getStyleClass().add("info-message");
                infoLabel.setStyle("-fx-padding: 10px; -fx-alignment: center;");
                noticeListContainer.getChildren().add(infoLabel);
            }
        });
    }
} 