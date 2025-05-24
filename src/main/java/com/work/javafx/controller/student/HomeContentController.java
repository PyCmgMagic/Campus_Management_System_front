package com.work.javafx.controller.student;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.work.javafx.entity.Data;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ResUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos; // 导入 Pos 用于对齐
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView; // 导入 FontAwesome 图标

import java.io.IOException; // 导入 IOException
import java.net.URL;
// import java.time.DayOfWeek; // DayOfWeek 暂时未使用，但保留以备将来之需
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList; // 导入 ArrayList 用于排序
import java.util.Collections; // 导入 Collections 用于排序
import java.util.Comparator;  // 导入 Comparator 用于排序
import java.util.HashMap;
import java.util.List;      // 导入 List 用于排序
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
    @FXML
    private VBox todayCoursesContainer; // 今日课程容器

    private final Gson gson = new Gson();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("学生端首页内容初始化成功");

        // 更新当前日期显示
        updateCurrentDate();
        // 加载公告
        loadNotices();
        // 加载今日课程
        loadTodayCourses();
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
     * 加载今日课程
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
        // 获取今天是星期几 (1 = 星期一, ..., 7 = 星期日)
        int dayOfWeekValue = today.getDayOfWeek().getValue();

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

        // 获取当前教学周
        fetchCurrentWeek(currentTerm, (currentWeek) -> {
            if (currentWeek == null || currentWeek.isEmpty()) {
                displayTodayCoursesError("无法获取当前教学周，今日课程加载失败。");
                return;
            }

            // 构建请求URL和参数
            String url = "/class/getClassSchedule/";
            url += currentWeek;
            System.out.println("请求学生今日课程，URL: " + url + ", 学期: " + currentTerm);

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
                                if (res.has("data") && res.get("data").isJsonArray()) {
                                    JsonArray data = res.getAsJsonArray("data");
                                    // 筛选并显示今日课程
                                    filterAndDisplayTodayCourses(data, dayOfWeekValue);
                                } else {
                                    displayTodayCoursesError("课程数据格式错误或为空。");
                                }
                            } else {
                                String msg = res.has("msg") ? res.get("msg").getAsString() : "未知错误";
                                displayTodayCoursesError("获取课程数据失败: " + msg);
                            }
                        } catch (JsonParseException e) {
                            displayTodayCoursesError("解析课程数据时发生错误: " + e.getMessage());
                            e.printStackTrace();
                        } catch (Exception e) { // 捕获更广泛的异常
                            displayTodayCoursesError("处理课程数据时发生未知错误: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    String errorMessage = ResUtil.getMsgFromException(e);
                    displayTodayCoursesError("网络错误，无法加载今日课程: " + errorMessage);
                    e.printStackTrace();
                }
            });
        });
    }

    /**
     * 筛选并显示今日课程
     * @param data 课程数据JsonArray
     * @param dayOfWeekValue 今天是星期几(1-7, 1=周一)
     */
    private void filterAndDisplayTodayCourses(JsonArray data, int dayOfWeekValue) {
        Platform.runLater(() -> {
            if (todayCoursesContainer != null) {
                todayCoursesContainer.getChildren().clear();

                boolean hasCoursesToday = false;

                // 课程时间段定义 (与教师端保持一致或根据学生端需求调整)
                String[] timeSlots = {
                        "08:00 - 09:50", // 第1-2节
                        "10:10 - 12:00", // 第3-4节
                        "14:00 - 15:50", // 第5-6节
                        "16:10 - 18:00", // 第7-8节
                        "19:00 - 20:50"  // 第9-10节
                };

                for (JsonElement element : data) {
                    JsonObject course = element.getAsJsonObject();

                    // 确保 'time' 字段存在且为整数
                    if (!course.has("time") || !course.get("time").isJsonPrimitive() || !course.get("time").getAsJsonPrimitive().isNumber()) {
                        System.err.println("学生端课程数据缺少'time'字段或格式不正确: " + course.toString());
                        continue;
                    }
                    int timeIndex = course.get("time").getAsInt(); // timeIndex 从0开始，代表周一第一节课

                    // 计算课程在哪一天 (1=周一, ..., 7=周日)
                    int courseDay = (timeIndex / 5) + 1;

                    // 如果是今天的课程
                    if (courseDay == dayOfWeekValue) {
                        hasCoursesToday = true;

                        String courseName = course.has("name") ? course.get("name").getAsString() : "未知课程";
                        String classroom = course.has("classroom") ? course.get("classroom").getAsString() : "地点待定";
                        // 学生端可能还需要显示教师名称
                        String teacherName = course.has("teacherName") ? course.get("teacherName").getAsString() : "";


                        // 计算是当天的第几大节课 (0-4, 对应 timeSlots 数组的索引)
                        int slotInDay = timeIndex % 5;

                        // 添加课程项到UI
                        addCourseItem(timeSlots[slotInDay], courseName, classroom, teacherName);
                    }
                }

                // 如果今天没有课程，显示提示信息
                if (!hasCoursesToday) {
                    Label noCourseLabel = new Label("今日无课，尽情享受学习时光吧！");
                    noCourseLabel.getStyleClass().add("no-course-message");
                    noCourseLabel.setStyle("-fx-padding: 15px; -fx-text-fill: #888; -fx-alignment: center; -fx-font-style: italic;");
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
     * @param teacherName 教师名称 (新增)
     */
    private void addCourseItem(String time, String courseName, String location, String teacherName) {
        VBox courseItem = new VBox();
        courseItem.getStyleClass().add("course-item");
        courseItem.setSpacing(5);

        // 时间标签
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("course-time");

        // 课程名称标签
        Label courseNameLabel = new Label(courseName);
        courseNameLabel.getStyleClass().add("course-name");

        // 地点和教师信息的HBox
        HBox detailsBox = new HBox();
        detailsBox.setSpacing(10); // HBox内元素间距
        detailsBox.setAlignment(Pos.CENTER_LEFT); // 左对齐

        HBox locationBox = new HBox(5); // 图标和文字间距
        locationBox.setAlignment(Pos.CENTER_LEFT);
        FontAwesomeIconView locationIcon = new FontAwesomeIconView();
        locationIcon.setGlyphName("MAP_MARKER");
        locationIcon.setSize("1em"); // 图标大小
        locationIcon.getStyleClass().add("course-detail-icon");
        Label locationLabel = new Label(location);
        locationLabel.getStyleClass().add("course-detail-text");
        locationBox.getChildren().addAll(locationIcon, locationLabel);

        detailsBox.getChildren().add(locationBox);

        if (teacherName != null && !teacherName.isEmpty()) {
            HBox teacherBox = new HBox(5);
            teacherBox.setAlignment(Pos.CENTER_LEFT);
            FontAwesomeIconView teacherIcon = new FontAwesomeIconView();
            teacherIcon.setGlyphName("USER_CIRCLE_ALT");
            teacherIcon.setSize("1em");
            teacherIcon.getStyleClass().add("course-detail-icon");
            Label teacherLabel = new Label(teacherName);
            teacherLabel.getStyleClass().add("course-detail-text");
            teacherBox.getChildren().addAll(teacherIcon, teacherLabel);

            // 添加一个分隔符或间距
            javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.SOMETIMES); // 可伸缩的间距
            detailsBox.getChildren().addAll(spacer, teacherBox);
        }


        courseItem.getChildren().addAll(timeLabel, courseNameLabel, detailsBox);
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
                errorLabel.setStyle("-fx-text-fill: red; -fx-padding: 10px; -fx-alignment: center; -fx-font-weight: bold;");
                todayCoursesContainer.getChildren().add(errorLabel);
            }
        });
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
                Object userData = scene.getUserData();
                if (userData instanceof StudentBaseViewController) {
                    StudentBaseViewController baseController = (StudentBaseViewController) userData;
                    baseController.switchToCourseSchedule();
                } else {
                    System.err.println("无法获取基础视图控制器：scene.getUserData() 不是 StudentBaseViewController 类型或为 null。");
                    Alert alert = new Alert(Alert.AlertType.ERROR, "无法切换页面，请联系管理员。");
                    alert.showAndWait();
                }
            } else {
                System.err.println("无法获取当前场景，切换课表失败。");
            }
        } catch (Exception e) {
            System.err.println("切换到课表查询时发生错误: " + e.getMessage());
            e.printStackTrace();
            // 用户提示
            Alert alert = new Alert(Alert.AlertType.ERROR, "切换页面时发生内部错误。");
            alert.showAndWait();
        }
    }

    private void loadNotices() {
        Platform.runLater(() -> {
            if (noticeListContainer != null) {
                noticeListContainer.getChildren().clear();
            }
        });
        Map<String, String> params = new HashMap<>();

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
                                if (noticeListContainer != null) noticeListContainer.getChildren().clear();


                                List<JsonElement> noticeList = new ArrayList<>();
                                for (JsonElement el : noticesArray) {
                                    noticeList.add(el);
                                }

                                DateTimeFormatter inputIsoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                                Collections.sort(noticeList, new Comparator<JsonElement>() {
                                    @Override
                                    public int compare(JsonElement o1, JsonElement o2) {
                                        JsonObject notice1 = o1.getAsJsonObject();
                                        JsonObject notice2 = o2.getAsJsonObject();

                                        // 检查 'isTop' 字段是否存在，默认为0 (不置顶)
                                        int isTop1 = notice1.has("isTop") && notice1.get("isTop").isJsonPrimitive() ? notice1.get("isTop").getAsInt() : 0;
                                        int isTop2 = notice2.has("isTop") && notice2.get("isTop").isJsonPrimitive() ? notice2.get("isTop").getAsInt() : 0;

                                        // 置顶的公告优先 (isTop=1 排在 isTop=0 前面)
                                        if (isTop1 != isTop2) {
                                            return Integer.compare(isTop2, isTop1); // isTop值大的在前
                                        }

                                        // 如果置顶状态相同，则按发布时间倒序排列 (新的在前)
                                        String timeStr1 = notice1.has("publishTime") ? notice1.get("publishTime").getAsString() : "";
                                        String timeStr2 = notice2.has("publishTime") ? notice2.get("publishTime").getAsString() : "";

                                        if (timeStr1.isEmpty() && timeStr2.isEmpty()) return 0;
                                        if (timeStr1.isEmpty()) return 1; // timeStr1为空，排在后面
                                        if (timeStr2.isEmpty()) return -1; // timeStr2为空，timeStr1排在前面

                                        try {
                                            LocalDateTime dt1 = LocalDateTime.parse(timeStr1, inputIsoFormatter);
                                            LocalDateTime dt2 = LocalDateTime.parse(timeStr2, inputIsoFormatter);
                                            return dt2.compareTo(dt1);
                                        } catch (DateTimeParseException e) {
                                            // 如果日期解析失败，可以尝试按原始字符串比较，或将错误项排在后面
                                            System.err.println("公告日期解析失败，将影响排序: " + e.getMessage());
                                            return timeStr2.compareTo(timeStr1); // 备用比较
                                        }
                                    }
                                });


                                DateTimeFormatter outputHumanReadableFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                                for (JsonElement noticeElement : noticeList) { // 遍历排序后的列表
                                    JsonObject noticeObject = noticeElement.getAsJsonObject();
                                    String title = noticeObject.has("title") ? noticeObject.get("title").getAsString() : "无标题";
                                    String content = noticeObject.has("content") ? noticeObject.get("content").getAsString() : "无内容"; // 提供默认值
                                    // 获取 isTop 字段，用于UI显示
                                    int isTop = noticeObject.has("isTop") && noticeObject.get("isTop").isJsonPrimitive() ? noticeObject.get("isTop").getAsInt() : 0;
                                    String publishTimeStr = noticeObject.has("publishTime") ? noticeObject.get("publishTime").getAsString() : "";
                                    String creatorName = noticeObject.has("creatorName") ? noticeObject.get("creatorName").getAsString() : "无"; // 默认发布者

                                    String formattedPublishTime = "时间未知"; // 默认时间
                                    if (!publishTimeStr.isEmpty()) {
                                        try {
                                            LocalDateTime dateTime = LocalDateTime.parse(publishTimeStr, inputIsoFormatter);
                                            formattedPublishTime = dateTime.format(outputHumanReadableFormatter);
                                        } catch (DateTimeParseException e) {
                                            System.err.println("公告日期解析错误: " + publishTimeStr + " - " + e.getMessage());
                                        }
                                    }

                                    String timeAndCreatorInfo = "发布于：" + formattedPublishTime + "  |  发布人：" + creatorName;
                                    addNoticeItem(title, content, timeAndCreatorInfo, isTop); // 传递 isTop
                                }
                            } else {
                                displayErrorMessage("公告列表数据格式错误或为空。");
                            }
                        } else {
                            String msg = res.has("msg") ? res.get("msg").getAsString() : "未知错误";
                            displayErrorMessage("获取公告列表失败: " + msg);
                        }
                    } catch (JsonParseException e) {
                        displayErrorMessage("无法加载公告: 服务器响应数据格式错误。");
                        e.printStackTrace();
                    } catch (Exception e) {
                        String errorMessage = ResUtil.getMsgFromException(e);
                        displayErrorMessage("加载公告时发生未知错误: " + errorMessage);
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                String errorMessage = ResUtil.getMsgFromException(e);
                displayErrorMessage("网络错误，无法加载公告: " + errorMessage);
                e.printStackTrace();
            }
        });
    }

    /**
     * 添加单个公告项到UI
     * @param title 公告标题
     * @param content 公告内容 (用于详情展示)
     * @param timeInfo 发布时间和发布人信息
     * @param isTop 是否置顶 (新增参数)
     */
    private void addNoticeItem(String title, String content, String timeInfo, int isTop) {
        HBox noticeItem = new HBox();
        noticeItem.getStyleClass().add("notice-item");
        noticeItem.setSpacing(10); // HBox内元素间距
        noticeItem.setOnMouseClicked(e -> showNoticeDetails(title, content)); // 点击显示详情

        StackPane iconPane = new StackPane();
        iconPane.getStyleClass().add("notice-icon");


        VBox noticeDetailsVBox = new VBox();
        noticeDetailsVBox.getStyleClass().add("notice-content");
        noticeDetailsVBox.setSpacing(3); // 标题和时间信息的间距

        HBox titleLine = new HBox(5);
        titleLine.setAlignment(Pos.CENTER_LEFT);

        if (isTop == 1) {
            Label topTag = new Label("[置顶]");
            topTag.getStyleClass().add("notice-tag-top"); // 为置顶标签应用CSS样式
            titleLine.getChildren().add(topTag);
        }

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("notice-title");
        titleLine.getChildren().add(titleLabel);


        Label timeLabel = new Label(timeInfo);
        timeLabel.getStyleClass().add("notice-time");

        noticeDetailsVBox.getChildren().addAll(titleLine, timeLabel);

        HBox.setHgrow(noticeDetailsVBox, javafx.scene.layout.Priority.ALWAYS);

        noticeItem.getChildren().addAll(iconPane, noticeDetailsVBox);
        if (noticeListContainer != null) {
            noticeListContainer.getChildren().add(noticeItem);
        }
    }


    private void showNoticeDetails(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("公告详情");
        alert.setHeaderText(title); // 将标题置于头部

        TextArea textArea = new TextArea(content);
        textArea.setEditable(false); // 内容不可编辑
        textArea.setWrapText(true); // 自动换行
        textArea.setPrefHeight(300); // 偏好高度
        textArea.setPrefWidth(500);  // 偏好宽度

        alert.setGraphic(null); // 移除默认的警告/信息图标

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setContent(textArea); // 将TextArea设置为对话框内容
        dialogPane.setPrefWidth(550);
        dialogPane.setPrefHeight(400);


        try {
            // 尝试加载与管理员端一致的对话框样式，或学生端特有的样式
            URL cssResource = getClass().getResource("/com/work/javafx/css/admin/DialogStyles.css");
            if (cssResource != null) {
                dialogPane.getStylesheets().add(cssResource.toExternalForm());
                dialogPane.getStyleClass().add("notice-dialog");
            } else {
                System.err.println("公告详情对话框的CSS样式文件未找到。");
            }
        } catch (Exception e) {
            System.err.println("为公告详情对话框加载CSS失败: " + e.getMessage());
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
                noticeListContainer.getChildren().add(errorLabel);
            } else {
                System.err.println( message);
            }
        });
    }

    private void displayInfoMessage(String message) {
        Platform.runLater(() -> {
            if (noticeListContainer != null) { // 再次检查
                noticeListContainer.getChildren().clear();
                Label infoLabel = new Label(message);
                infoLabel.getStyleClass().add("info-message");
                noticeListContainer.getChildren().add(infoLabel);
            } else {
                System.err.println(message);
            }
        });
    }

    /**
     * 获取当前学期
     * @param callback 获取完成后的回调，在学期数据成功获取并设置后执行
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

    /**
     * 获取指定学期的当前教学周
     * @param term 学期名称
     * @param callback 回调函数，接收当前教学周字符串 (失败时为null)
     */
    private void fetchCurrentWeek(String term, java.util.function.Consumer<String> callback) {
        if (term == null || term.isEmpty()) {
            System.err.println("无法获取当前周，因为学期名称为空。");
            if (callback != null) Platform.runLater(() -> callback.accept(null));
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("term", term); // API可能需要的参数名
        callback.accept("1");
        //TODO:
//        NetworkUtils.get("/week/getTermCurrentWeek", params, new NetworkUtils.Callback<String>() {
//            @Override
//            public void onSuccess(String result) throws IOException {
//                try {
//                    JsonObject res = gson.fromJson(result, JsonObject.class);
//                    if (res.has("code") && res.get("code").getAsInt() == 200 && res.has("data")) {
//                        String currentWeek = res.get("data").getAsString();
//                        System.out.println("学生端成功获取当前教学周: " + currentWeek + " (学期: " + term + ")");
//                        if (callback != null) Platform.runLater(() -> callback.accept(currentWeek));
//                    } else {
//                        String errorMsg = res.has("msg") ? res.get("msg").getAsString() : "未知错误或数据格式不正确";
//                        System.err.println("学生端获取当前教学周失败: " + errorMsg);
//                        if (callback != null) Platform.runLater(() -> callback.accept(null));
//                    }
//                } catch (JsonParseException e) {
//                    System.err.println("学生端解析当前教学周数据时发生JSON错误: " + e.getMessage());
//                    if (callback != null) Platform.runLater(() -> callback.accept(null));
//                    e.printStackTrace();
//                } catch (Exception e) { // 更广泛的异常
//                    System.err.println("学生端处理当前教学周数据时发生错误: " + e.getMessage());
//                    if (callback != null) Platform.runLater(() -> callback.accept(null));
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                String msg = ResUtil.getMsgFromException(e);
//                System.err.println("学生端获取当前教学周网络请求失败: " + msg);
//                if (callback != null) Platform.runLater(() -> callback.accept(null));
//                e.printStackTrace();
//            }
//        });
    }
}