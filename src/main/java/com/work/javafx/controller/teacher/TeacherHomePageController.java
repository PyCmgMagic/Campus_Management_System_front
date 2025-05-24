package com.work.javafx.controller.teacher;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.work.javafx.controller.admin.AddNewAnnouncementController;
import com.work.javafx.controller.admin.editAnnouncementController;
import com.work.javafx.entity.Data;
import com.work.javafx.util.NetworkUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos; // Added for alignment
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
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class TeacherHomePageController implements Initializable {
    //卡片功能按钮
    @FXML private Label CompleteCourseSchedule;
    @FXML private Label ClassCount;
    @FXML private Label hoursCount;
    @FXML private VBox noticeListContainer; // 公告条目容器
    @FXML private Label dateText; // 日期文本
    @FXML private VBox todayCoursesContainer; // 今日课程容器

    // 基础控制器引用
    private TeacherBaseViewController baseController;
    private final Gson gson = new Gson();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //卡片功能按钮绑定
        CompleteCourseSchedule.setOnMouseClicked(this::changetocourseScheduleManagement);

        // 更新当前日期显示
        updateCurrentDate();
        // 加载今日教学任务
        loadTodayCourses();
        // 加载公告
        loadNotices();
        //获取教师统计数据
        fetchStatistics();
    }
    /***
     * 获取教师统计数据
     */
    private void fetchStatistics(){
        NetworkUtils.get("/Teacher/getMessage", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) throws IOException {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if(res.get("code").getAsInt() == 200){
                    JsonObject data = res.getAsJsonObject("data");
                    Platform.runLater(() -> { // Ensure UI updates are on JavaFX Application Thread
                        ClassCount.setText(data.get("classAmo").getAsString());
                        hoursCount.setText(data.get("totalClassHour").getAsString());
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                String errorMessage = e.getMessage();
                if (errorMessage != null && errorMessage.contains("{")) {
                    try {
                        JsonObject res = gson.fromJson(errorMessage.substring(errorMessage.indexOf("{")), JsonObject.class);
                        System.err.println("获取教师统计数据失败: " + res.get("msg").getAsString());
                    } catch (JsonParseException | IllegalStateException jsonEx) {
                        System.err.println("获取教师统计数据失败，且错误信息解析失败: " + errorMessage);
                    }
                } else {
                    System.err.println("获取教师统计数据失败: " + errorMessage);
                }
            }
        });
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
     * 加载今日教学任务
     */
    private void loadTodayCourses() {
        // 清空今日课程容器
        Platform.runLater(() -> {
            if (todayCoursesContainer != null) {
                todayCoursesContainer.getChildren().clear();
            }
        });

        // 获取当前日期
        LocalDate today = LocalDate.now();
        // 获取今天是星期几
        int dayOfWeek = today.getDayOfWeek().getValue();

        // 获取当前学期
        String currentTerm = Data.getInstance().getCurrentTerm();
        if (currentTerm == null || currentTerm.isEmpty()) {
            // 当前学期为空，需要从服务器获取
            fetchCurrentTerm(() -> {
                // 获取完当前学期后再加载今日课程
                Platform.runLater(this::loadTodayCourses);
            });
            return; // 终止当前方法执行，避免使用空的学期值
        }


        fetchCurrentWeek(currentTerm, (currentWeek) -> {
            if (currentWeek == null || currentWeek.isEmpty()) {
                displayTodayCoursesError("无法获取当前教学周，无法加载今日课程。");
                return;
            }
            // 构建请求URL和参数
            String url = "/class/getClassSchedule/";
            url += currentWeek;
            System.out.println("Requesting today's courses with URL: " + url + " and term: " + currentTerm);
            Map<String, String> params = new HashMap<>();
            params.put("term", currentTerm);

            // 发送网络请求获取课表数据
            NetworkUtils.get(url, params, new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    Platform.runLater(() -> {
                        try {
                            JsonObject res = gson.fromJson(result, JsonObject.class);
                            if (res.has("code") && res.get("code").getAsInt() == 200) {
                                JsonArray data = res.getAsJsonArray("data");
                                // 筛选今日课程
                                filterAndDisplayTodayCourses(data, dayOfWeek);
                            } else {
                                String msg = res.has("msg") ? res.get("msg").getAsString() : "未知错误";
                                displayTodayCoursesError("获取课程数据失败: " + msg);
                            }
                        } catch (Exception e) {
                            displayTodayCoursesError("解析课程数据时发生错误: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    displayTodayCoursesError("网络错误，无法加载今日课程: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        });
    }

    /**
     * 筛选并显示今日课程
     * @param data 课程数据
     * @param dayOfWeek 今天是星期几(1-7)
     */
    private void filterAndDisplayTodayCourses(JsonArray data, int dayOfWeek) {
        Platform.runLater(() -> {
            if (todayCoursesContainer != null) {
                todayCoursesContainer.getChildren().clear();

                boolean hasCourses = false;

                // 课程时间段定义
                String[] timeSlots = {
                        "08:00 - 09:50",
                        "10:10 - 12:00",
                        "14:00 - 15:50",
                        "16:10 - 18:00",
                        "19:00 - 20:50"
                };

                for (JsonElement element : data) {
                    JsonObject course = element.getAsJsonObject();
                    // 确保 'time' 字段存在且为整数
                    if (!course.has("time") || !course.get("time").isJsonPrimitive() || !course.get("time").getAsJsonPrimitive().isNumber()) {
                        System.err.println("课程数据缺少'time'字段或格式不正确: " + course.toString());
                        continue;
                    }
                    int index = course.get("time").getAsInt();


                    int courseDay = (index / 5) + 1;

                    // 如果是今天的课程
                    if (courseDay == dayOfWeek) {
                        hasCourses = true;
                        String courseName = course.has("name") ? course.get("name").getAsString() : "未知课程";
                        String classroom = course.has("classroom") ? course.get("classroom").getAsString() : "未知地点";
                        String classInfo = course.has("className") ? course.get("className").getAsString() : "";
                        String studentsCount = course.has("studentsCount") ? course.get("studentsCount").getAsString() : "";

                        // 计算是第几节课 (0-4, 对应 timeSlots 数组的索引)
                        int timeSlotIndex = index % 5;

                        // 添加课程项
                        addCourseItem(timeSlots[timeSlotIndex], courseName, classroom, classInfo, studentsCount);
                    }
                }

                // 如果今天没有课程，显示提示信息
                if (!hasCourses) {
                    Label noCourseLabel = new Label("今日没有安排课程");
                    noCourseLabel.getStyleClass().add("no-course-message");
                    noCourseLabel.setStyle("-fx-padding: 15px; -fx-text-fill: #888; -fx-alignment: center;");
                    todayCoursesContainer.getChildren().add(noCourseLabel);
                }
            }
        });
    }

    /**
     * 添加课程项到UI
     * @param time 课程时间
     * @param courseName 课程名称
     * @param location 上课地点
     * @param classInfo 班级信息
     * @param studentsCount 学生人数
     */
    private void addCourseItem(String time, String courseName, String location, String classInfo, String studentsCount) {
        VBox courseItem = new VBox();
        courseItem.getStyleClass().add("course-item");

        // 课程时间
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("course-time");

        // 课程名称
        Label courseNameLabel = new Label(courseName);
        courseNameLabel.getStyleClass().add("course-name");

        // 课程信息容器
        HBox courseInfo = new HBox();
        courseInfo.getStyleClass().add("course-info");
        courseInfo.setSpacing(10); // Add some spacing

        // 地点信息
        HBox locationBox = new HBox(5); // Spacing for icon and label
        locationBox.getStyleClass().add("course-location");
        locationBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        FontAwesomeIconView locationIcon = new FontAwesomeIconView();
        locationIcon.setGlyphName("MAP_MARKER");
        locationIcon.setSize("12");

        Label locationLabel = new Label(location);

        locationBox.getChildren().addAll(locationIcon, locationLabel);

        // 学生信息
        HBox studentsBox = new HBox(5); // Spacing for icon and label
        studentsBox.getStyleClass().add("course-students");
        studentsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        FontAwesomeIconView studentsIcon = new FontAwesomeIconView();
        studentsIcon.setGlyphName("USERS");
        studentsIcon.setSize("12");

        // 组合班级信息和人数
        String studentInfo = classInfo;
        if (studentsCount != null && !studentsCount.isEmpty() && !"0".equals(studentsCount)) { // Check for "0" as well
            studentInfo += (studentInfo.isEmpty() ? "" : " / ") + studentsCount + "人";
        }
        if (studentInfo.isEmpty()) {
            studentInfo = "暂无学生信息";
        }

        Label studentsLabel = new Label(studentInfo);

        studentsBox.getChildren().addAll(studentsIcon, studentsLabel);

        // 添加空白占位
        javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // 组装课程信息
        courseInfo.getChildren().addAll(locationBox, spacer, studentsBox);

        // 组装整个课程项
        courseItem.getChildren().addAll(timeLabel, courseNameLabel, courseInfo);

        // 添加到容器
        todayCoursesContainer.getChildren().add(courseItem);
    }

    /**
     * 显示今日课程加载错误信息
     * @param message 错误信息
     */
    private void displayTodayCoursesError(String message) {
        Platform.runLater(() -> {
            if (todayCoursesContainer != null) {
                todayCoursesContainer.getChildren().clear();
                Label errorLabel = new Label(message);
                errorLabel.getStyleClass().add("error-message");
                errorLabel.setStyle("-fx-text-fill: red; -fx-padding: 10px; -fx-alignment: center;");
                todayCoursesContainer.getChildren().add(errorLabel);
            }
        });
    }

    public void setBaseController(TeacherBaseViewController controller) {
        this.baseController = controller;
    }
    //查看完整课表
    private void changetocourseScheduleManagement(MouseEvent mouseEvent) {
        if (baseController != null) baseController.switchTocourseScheduleManagement();
    }



    private void loadNotices() {
        Platform.runLater(() -> noticeListContainer.getChildren().clear());
        Map<String, String> params = new HashMap<>();
        params.put("Status", "0"); // "0" for active/visible notices

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

                                List<JsonElement> noticeList = new ArrayList<>();
                                for (JsonElement el : noticesArray) {
                                    noticeList.add(el);
                                }

                                DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                                Collections.sort(noticeList, new Comparator<JsonElement>() {
                                    @Override
                                    public int compare(JsonElement o1, JsonElement o2) {
                                        JsonObject notice1 = o1.getAsJsonObject();
                                        JsonObject notice2 = o2.getAsJsonObject();

                                        int isTop1 = notice1.has("isTop") ? notice1.get("isTop").getAsInt() : 0;
                                        int isTop2 = notice2.has("isTop") ? notice2.get("isTop").getAsInt() : 0;

                                        if (isTop1 != isTop2) {
                                            return Integer.compare(isTop2, isTop1);
                                        }

                                        String timeStr1 = notice1.has("publishTime") ? notice1.get("publishTime").getAsString() : "";
                                        String timeStr2 = notice2.has("publishTime") ? notice2.get("publishTime").getAsString() : "";

                                        if (timeStr1.isEmpty() && timeStr2.isEmpty()) return 0;
                                        if (timeStr1.isEmpty()) return 1;
                                        if (timeStr2.isEmpty()) return -1;

                                        try {
                                            LocalDateTime dt1 = LocalDateTime.parse(timeStr1, inputFormatter);
                                            LocalDateTime dt2 = LocalDateTime.parse(timeStr2, inputFormatter);
                                            return dt2.compareTo(dt1);
                                        } catch (DateTimeParseException e) {
                                            return timeStr2.compareTo(timeStr1);
                                        }
                                    }
                                });


                                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                                for (JsonElement noticeElement : noticeList) {
                                    JsonObject noticeObject = noticeElement.getAsJsonObject();
                                    int id = noticeObject.get("id").getAsInt();
                                    String title = noticeObject.has("title") ? noticeObject.get("title").getAsString() : "无标题";
                                    String content = noticeObject.has("content") ? noticeObject.get("content").getAsString() : "";
                                    int isTop = noticeObject.has("isTop") ? noticeObject.get("isTop").getAsInt() : 0;
                                    int visibleScope = noticeObject.has("visibleScope") ? noticeObject.get("visibleScope").getAsInt() : 0;
                                    String publishTimeStr = noticeObject.has("publishTime") ? noticeObject.get("publishTime").getAsString() : "";
                                    String creatorName = noticeObject.has("creatorName") ? noticeObject.get("creatorName").getAsString() : "未知发布者";

                                    String formattedPublishTime = "时间未知";
                                    if (!publishTimeStr.isEmpty()) {
                                        try {
                                            LocalDateTime dateTime = LocalDateTime.parse(publishTimeStr, inputFormatter);
                                            formattedPublishTime = dateTime.format(outputFormatter);
                                        } catch (DateTimeParseException e) {
                                            System.err.println("公告日期解析错误: " + publishTimeStr + " - " + e.getMessage());
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
                        if (errorMessage != null && errorMessage.contains("{") && errorMessage.contains("}")) {
                            try {
                                JsonObject errorJson = gson.fromJson(errorMessage.substring(errorMessage.indexOf("{")), JsonObject.class);
                                if (errorJson.has("msg")) {
                                    errorMessage = errorJson.get("msg").getAsString();
                                }
                            } catch (JsonParseException jsonEx) {
                            }
                        }
                        displayErrorMessage("获取公告列表时发生错误: " + errorMessage);
                        System.err.println("loadNotices中发生错误: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                String errorMessage = e.getMessage();
                if (errorMessage != null && errorMessage.contains("{") && errorMessage.contains("}")) {
                    try {
                        JsonObject errorJson = gson.fromJson(errorMessage.substring(errorMessage.indexOf("{")), JsonObject.class);
                        if (errorJson.has("msg")) {
                            errorMessage = errorJson.get("msg").getAsString();
                        }
                    } catch (JsonParseException jsonEx) {
                    }
                }
                displayErrorMessage("网络错误，无法加载公告: " + errorMessage);
                System.err.println("loadNotices中网络故障: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void addNoticeItem(int id, String title, String content, int isTop, int visibleScope, String timeInfo) {
        HBox noticeItem = new HBox();
        noticeItem.getStyleClass().add("notice-item");
        noticeItem.setSpacing(10);
        noticeItem.setOnMouseClicked(e -> showNoticeDetails(title, content));

        StackPane iconPane = new StackPane();
        iconPane.getStyleClass().add("notice-icon");



        VBox noticeDetailsVBox = new VBox();
        noticeDetailsVBox.getStyleClass().add("notice-content");
        noticeDetailsVBox.setSpacing(5);

        HBox titleLine = new HBox(5);
        titleLine.setAlignment(Pos.CENTER_LEFT);

        if (isTop == 1) {
            Label topTag = new Label("[置顶]");
            topTag.getStyleClass().add("notice-tag-top");
            titleLine.getChildren().add(topTag);
        }

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("notice-title");

        titleLine.getChildren().add(titleLabel);


        Label timeLabel = new Label(timeInfo);
        timeLabel.getStyleClass().add("notice-time");

        noticeDetailsVBox.getChildren().addAll(titleLine, timeLabel);
        HBox actions = new HBox();
        actions.getStyleClass().add("notice-actions");
        actions.setSpacing(5);
        actions.setAlignment(Pos.CENTER_RIGHT);


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
        HBox.setHgrow(actions, javafx.scene.layout.Priority.NEVER);

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
                System.err.println("DialogStyles.css 未找到。将使用默认对话框样式。");
            }
        } catch (Exception e) {
            System.err.println("加载对话框CSS时出错: " + e.getMessage());
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
                System.err.println("AddNewAnnouncementController 为空。请检查FXML路径和控制器绑定。");
                displayErrorMessage("无法打开发布公告窗口：控制器加载失败。");
                return;
            }

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.DECORATED);
            popupStage.setTitle("发布新公告");
            Scene scene = new Scene(root, 800, 600);
            popupStage.setScene(scene);
            popupStage.setMinWidth(700);
            popupStage.setMinHeight(550);

            controller.initVisble(false);
            controller.setStage(popupStage);
            controller.setOnPublishCompleteCallback(this::loadNotices);

            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            displayErrorMessage("打开公告发布窗口失败: " + e.getMessage());
        }  catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("打开公告发布窗口时发生意外错误: " + e.getMessage());
        }
    }

    private void editNotice(int noticeId, String title, String content, int isTop, int visibleScope) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/editAnnouncement.fxml"));
            Parent root = loader.load();

            editAnnouncementController controller = loader.getController();
            if (controller == null) {
                System.err.println("editAnnouncementController 为空。请检查FXML路径和控制器绑定。");
                displayErrorMessage("无法打开编辑公告窗口：控制器加载失败。");
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
            displayErrorMessage("打开公告编辑窗口时发生意外错误: " + e.getMessage());
        }
    }

    private void deleteNotice(int noticeId) {
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("确认删除");
        confirmationDialog.setHeaderText("您确定要删除此公告吗？");
        confirmationDialog.setContentText("公告ID: " + noticeId + "\n此操作会将公告状态设置为关闭，但不会从数据库中物理删除。");

        try {
            URL cssResource = getClass().getResource("/com/work/javafx/css/admin/DialogStyles.css");
            if (cssResource != null) {
                DialogPane dialogPane = confirmationDialog.getDialogPane();
                dialogPane.getStylesheets().add(cssResource.toExternalForm());
                dialogPane.getStyleClass().add("confirmation-dialog");
            }
        } catch (Exception e) {
            System.err.println("加载确认对话框CSS时出错: " + e.getMessage());
        }


        confirmationDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(noticeId));

                NetworkUtils.post("/notice/close", params, "" , new NetworkUtils.Callback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        Platform.runLater(() -> {
                            try {
                                JsonObject res = gson.fromJson(result, JsonObject.class);
                                if (res.has("code") && res.get("code").getAsInt() == 200) {
                                    displayInfoMessage("公告已成功关闭（标记为删除）。");
                                    loadNotices();
                                } else {
                                    String msg = res.has("msg") ? res.get("msg").getAsString() : "未知错误";
                                    displayErrorMessage("关闭公告失败: " + msg);
                                }
                            } catch (JsonParseException e) {
                                displayErrorMessage("关闭公告失败: 服务器响应格式错误。");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Platform.runLater(() -> displayErrorMessage("关闭公告操作失败: " + e.getMessage()));
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void displayErrorMessage(String message) {
        Platform.runLater(() -> {
            if (noticeListContainer == null) return;
            noticeListContainer.getChildren().clear();
            Label errorLabel = new Label(message);
            errorLabel.getStyleClass().add("error-message");
            errorLabel.setStyle("-fx-text-fill: red; -fx-padding: 10px; -fx-alignment: center; -fx-font-weight: bold;");
            noticeListContainer.getChildren().add(errorLabel);
        });
    }

    private void displayInfoMessage(String message) {
        Platform.runLater(() -> {
            if (noticeListContainer == null) return;
            noticeListContainer.getChildren().clear();
            Label infoLabel = new Label(message);
            infoLabel.getStyleClass().add("info-message");
            infoLabel.setStyle("-fx-padding: 10px; -fx-alignment: center;");
            noticeListContainer.getChildren().add(infoLabel);
        });
    }

    /**
     * 获取当前学期
     * @param callback 获取完成后的回调
     */
    private void fetchCurrentTerm(Runnable callback) {
        NetworkUtils.get("/term/getCurrentTerm", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200 && res.has("data")) {
                        String currentTerm = res.get("data").getAsString();
                        System.out.println("成功获取当前学期: " + currentTerm);
                        Data.getInstance().setCurrentTerm(currentTerm);

                        if (callback != null) {
                            callback.run();
                        }
                    } else {
                        String msg = res.has("msg") ? res.get("msg").getAsString() : "响应格式不正确或获取失败";
                        System.err.println("获取当前学期失败: " + msg);
                        displayTodayCoursesError("获取当前学期失败 ("+msg+")，无法加载今日课程");
                    }
                } catch (Exception e) {
                    System.err.println("解析当前学期数据时发生错误: " + e.getMessage());
                    displayTodayCoursesError("获取当前学期失败（解析错误），无法加载今日课程");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("获取当前学期网络请求失败: " + e.getMessage());
                displayTodayCoursesError("网络错误，无法获取当前学期");
                if (e.getMessage() != null && e.getMessage().contains("{")) {
                    try {
                        JsonObject res = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")), JsonObject.class);
                        if (res.has("msg")) {
                            System.err.println("服务器错误信息: " + res.get("msg").getAsString());
                        }
                    } catch (Exception ex) {
                    }
                }
                e.printStackTrace();
            }
        });
    }

    private void fetchCurrentWeek(String term, java.util.function.Consumer<String> callback) {
        if (term == null || term.isEmpty()) {
            System.err.println("无法获取当前周，因为学期为空。");
            if (callback != null) callback.accept(null);
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("term", term);
        callback.accept("1");
//        NetworkUtils.get("/week/getTermCurrentWeek", params, new NetworkUtils.Callback<String>() {
//            @Override
//            public void onSuccess(String result) throws IOException {
//                JsonObject res = gson.fromJson(result, JsonObject.class);
//                if (res.has("code") && res.get("code").getAsInt() == 200 && res.has("data")) {
//                    String currentWeek = res.get("data").getAsString();
//                    System.out.println("成功获取当前教学周: " + currentWeek + " for term: " + term);
//                    if (callback != null) callback.accept(currentWeek);
//                } else {
//                    String msg = res.has("msg") ? res.get("msg").getAsString() : "响应格式不正确或获取失败";
//                    System.err.println("获取当前教学周失败: " + msg);
//                    if (callback != null) callback.accept(null);
//                }
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                System.err.println("获取当前教学周网络请求失败: " + e.getMessage());
//                if (callback != null) callback.accept(null);
//                e.printStackTrace();
//            }
//        });
    }
}