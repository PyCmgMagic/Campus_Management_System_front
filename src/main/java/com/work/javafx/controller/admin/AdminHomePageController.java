package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.work.javafx.util.NetworkUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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



/**
 * 管理员首页控制器
 * 处理管理员控制台页面的交互逻辑
 */
public class AdminHomePageController implements Initializable {
    // AdminBaseViewController的实例，用于页面切换等基础操作
    AdminBaseViewController adminBaseController = new AdminBaseViewController();

    @FXML
    private Label studentCountLabel; // 显示学生总数的标签

    @FXML
    private Label teacherCountLabel; // 显示教师总数的标签

    @FXML
    private VBox noticeListContainer; // 显示公告列表的VBox容器
    Gson gson = new Gson();

    /**
     * 初始化控制器，在FXML加载完成后调用。
     * @param location FXML文件的位置
     * @param resources FXML文件使用的资源包
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        
        // 加载初始数据
        loadStatistics();
        loadNotices();    // 加载公告列表
    }


    /**
     * 从服务器加载统计数据（学生和教师的数量）并更新UI。
     */
    private void loadStatistics() {
        Map<String,String> params = new HashMap<>();
        
        // 获取教师人数
        params.put("permission","1"); //
        NetworkUtils.get("/admin/getNum", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                // 确保UI更新在JavaFX应用线程执行
                Platform.runLater(() -> {
                    try {
                        JsonObject res  = gson.fromJson(result, JsonObject.class);
                        if(res.has("code") && res.get("code").getAsInt()==200){
                            int data = res.get("data").getAsInt();
                            teacherCountLabel.setText(String.valueOf(data));
                        } else {
                            System.out.print("获取教师人数失败：");
                            if (res.has("msg")) {
                                System.out.println(res.get("msg").getAsString());
                            } else {
                                System.out.println("未知错误");
                            }
                        }
                    } catch (JsonParseException e) {
                        System.err.println("获取教师人数失败：JSON解析错误 - " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("获取教师人数失败：处理响应时发生未知错误 - " + e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("获取教师人数网络请求失败: " + e.getMessage());

            }
        });
        
        // 获取学生人数
        params.put("permission","2");
        NetworkUtils.get("/admin/getNum", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                Platform.runLater(() -> {
                    try {
                        JsonObject res  = gson.fromJson(result, JsonObject.class);
                        if(res.has("code") && res.get("code").getAsInt()==200){
                            int data = res.get("data").getAsInt();
                            studentCountLabel.setText(String.valueOf(data));
                        } else {
                            System.out.print("获取学生人数失败：");
                            if (res.has("msg")) {
                                System.out.println(res.get("msg").getAsString());
                            } else {
                                System.out.println("未知错误");
                            }
                        }
                    } catch (JsonParseException e) {
                        System.err.println("获取学生人数失败：JSON解析错误 - " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("获取学生人数失败：处理响应时发生未知错误 - " + e.getMessage());
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                 System.err.println("获取学生人数网络请求失败: " + e.getMessage());

            }
        });
    }

    /**
     * 从服务器加载公告列表并更新UI。
     */
    private void loadNotices() {
        Platform.runLater(() -> noticeListContainer.getChildren().clear());
        Map<String,String> params =  new HashMap<>();
        params.put("Status","1");
        // 发起网络请求获取公告列表
        NetworkUtils.get("/notice/getAdminNoticeList", params, new NetworkUtils.Callback<String>() {
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

                                // 定义日期时间格式化器
                                DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); // 输出格式

                                // 遍历公告数组，为每条公告创建UI元素
                                for (JsonElement noticeElement : noticesArray) {
                                    JsonObject noticeObject = noticeElement.getAsJsonObject();
                                    int id = noticeObject.get("id").getAsInt();
                                    String content = noticeObject.get("content").getAsString();
                                    int  isTop  = noticeObject.get("isTop").getAsInt();
                                    int visibleScope = noticeObject.get("visibleScope").getAsInt();
                                    String title = noticeObject.has("title") ? noticeObject.get("title").getAsString() : "无标题";
                                    String publishTimeStr = noticeObject.has("publishTime") ? noticeObject.get("publishTime").getAsString() : "";
                                     String creatorName = noticeObject.has("creatorName") ? noticeObject.get("creatorName").getAsString() : "null";
                                    String creatorInfo = "发布人: "  + creatorName;

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
                                    // 组合时间和创建者信息
                                    String timeAndCreatorInfo = "发布时间：" + formattedPublishTime + " | " + creatorInfo;
                                    addNoticeItem(id, title, content, isTop,visibleScope,timeAndCreatorInfo);
                                }
                            } else {
                                System.out.println("获取公告列表失败：响应中没有data字段或data不是有效的数组");
                                displayErrorMessage("公告列表为空");
                            }
                        } else {
                            String msg = res.has("msg") ? res.get("msg").getAsString() : "未知错误";
                            System.out.println("获取公告列表失败：" + msg);
                            displayErrorMessage("获取公告列表失败: " + msg);
                        }
                    } catch (JsonParseException e) {
                        System.err.println("获取公告列表失败：JSON解析错误 - " + e.getMessage());
                        displayErrorMessage("获取公告列表失败: 服务器响应格式错误。");
                    } catch (Exception e) {
                        JsonObject res = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")), JsonObject.class);
                        String msg = res.get("msg").getAsString();
                        System.err.println("获取公告列表失败： - " + msg);
                        displayErrorMessage("获取公告列表失败: " +msg);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                JsonObject res = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")), JsonObject.class);
                String msg = res.get("msg").getAsString();
                System.err.println("获取公告列表失败： - " + msg);
                displayErrorMessage("获取公告列表失败: " +msg);
            }
        });
    }

    /**
     * 在公告列表容器中显示错误信息。
     * @param message 要显示的错误消息文本
     */
    private void displayErrorMessage(String message) {
        Platform.runLater(() -> {
            noticeListContainer.getChildren().clear(); // 清空容器
            Label errorLabel = new Label(message); // 创建错误标签
            errorLabel.getStyleClass().add("error-message"); // 添加CSS类名，用于样式定义
            errorLabel.setStyle("-fx-text-fill: red; -fx-padding: 10px; -fx-alignment: center;"); // 直接设置样式
            noticeListContainer.getChildren().add(errorLabel); // 添加到容器
        });
    }

    /**
     * 在公告列表容器中显示普通提示信息。
     * @param message 要显示的提示消息文本
     */
    private void displayInfoMessage(String message) {
        Platform.runLater(() -> {
            noticeListContainer.getChildren().clear();
            Label infoLabel = new Label(message);
            infoLabel.getStyleClass().add("info-message"); // 添加CSS类名
            infoLabel.setStyle("-fx-padding: 10px; -fx-alignment: center;"); // 直接设置样式
            noticeListContainer.getChildren().add(infoLabel); // 添加到容器
        });
    }
    
    /**
     * 根据提供的标题和时间信息，创建一个公告条目并添加到UI列表中。
     * @param id 公告ID
     * @param title 公告标题
     * @param content 公告内容
     * @param timeInfo 公告的时间和发布者信息字符串
     * @param isTop 是否置顶
     * @param visibleScope 可见范围
     */
    private void addNoticeItem(int id, String title, String content, int isTop,int visibleScope,String timeInfo) {
        HBox noticeItem = new HBox(); // 公告条目的根容器
        noticeItem.getStyleClass().add("notice-item");
        noticeItem.setOnMouseClicked(e -> showNoticeDetails(title, content));
        
        StackPane icon = new StackPane(); // 图标容器
        icon.getStyleClass().add("notice-icon");
        
        VBox noticeDetailsVBox = new VBox(); // 内容容器
        noticeDetailsVBox.getStyleClass().add("notice-content");
        
        HBox titleBox = new HBox(); // 标题行容器
        Label titleLabel = new Label(title); // 标题标签
        titleLabel.getStyleClass().add("notice-title");
        titleBox.getChildren().add(titleLabel);

        Label timeLabel = new Label(timeInfo); // 时间信息标签
        timeLabel.getStyleClass().add("notice-time");
        
        noticeDetailsVBox.getChildren().addAll(titleBox, timeLabel);
        
        HBox actions = new HBox();
        actions.getStyleClass().add("notice-actions");
        
        Button editBtn = new Button("编辑");
        editBtn.getStyleClass().addAll("notice-btn", "edit-btn");
        editBtn.setOnAction(e -> {
            editNotice(id,title,content,isTop,visibleScope);
        }); // 设置编辑按钮的点击事件
        
        Button deleteBtn = new Button("删除");
        deleteBtn.getStyleClass().addAll("notice-btn", "delete-btn");
        deleteBtn.setOnAction(e -> deleteNotice(id)); // 设置删除按钮的点击事件
        
        actions.getChildren().addAll(editBtn, deleteBtn); // 将按钮添加到操作HBox
        
        noticeItem.getChildren().addAll(icon, noticeDetailsVBox, actions); // 组合图标、内容和操作到公告条目HBox

        noticeListContainer.getChildren().add(noticeItem);
    }
    /**
     * 显示公告详情。
     * @param title 公告标题
     * @param content 公告内容
     */
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
            URL cssResource = getClass().getResource("/com/work/javafx/css/admin/DialogStyles.css");
            if (cssResource != null) {
                dialogPane.getStylesheets().add(cssResource.toExternalForm());
                dialogPane.getStyleClass().add("notice-dialog");
            } else {
                System.err.println("not found");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }


        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setResizable(true);

        alert.showAndWait();
    }

    /**
     * "发布新公告"按钮的事件处理程序。
     * 打开一个新的模态窗口用于发布公告。
     */
    @FXML
    private void publishNewNotice() {
        try {
            // 加载发布新公告的FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/addNewAnnouncement.fxml"));
            Parent root = loader.load();

            AddNewAnnouncementController controller = loader.getController();

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.DECORATED);
            popupStage.setTitle("发布公告");
            popupStage.setScene(new Scene(root, 800, 600));

            popupStage.setMinWidth(700); // 设置最小宽度
            popupStage.setMinHeight(550); // 设置最小高度

            controller.setStage(popupStage);
            controller.setOnPublishCompleteCallback(this::loadNotices);
            popupStage.showAndWait(); // 显示窗口并等待其关闭
        } catch (IOException e) {
            e.printStackTrace(); // 打印加载FXML时的IO异常
        }
    }
    
    /**
     * 编辑公告的方法。
     * @param noticeId 被编辑公告的ID
     */
    private void editNotice(int noticeId,String title,String content,int isTop,int visibleScope) {
       try {
           FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/editAnnouncement.fxml"));
           Parent root = loader.load();

           editAnnouncementController controller = loader.getController();

           if (controller != null) {
               controller.initData(noticeId,title,content,isTop,visibleScope);
           } else {
               System.err.println("editAnnouncementController 为空");
               return;
           }

           Stage stage = new Stage();
           stage.initModality(Modality.APPLICATION_MODAL);
           stage.initStyle(StageStyle.DECORATED);
           stage.setTitle("编辑公告");
           stage.setScene(new Scene(root, 800, 600));
              controller.setStage(stage);

           stage.showAndWait(); // 显示窗口并等待其关闭

       }catch (Exception e) {
           e.printStackTrace();
       }
    }


    /**
     * 删除公告的方法。
     * @param noticeId 被删除公告的ID
     */
    private void deleteNotice(int noticeId) {
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("确认删除");
        confirmationDialog.setHeaderText("您确定要删除此公告吗？");
        confirmationDialog.setContentText("ID: " + noticeId);

        confirmationDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Map<String, String> params = new HashMap<>();
                params.put("id", noticeId+"");


                NetworkUtils.post("/notice/close", params, "" , new NetworkUtils.Callback<String>() {
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


} 