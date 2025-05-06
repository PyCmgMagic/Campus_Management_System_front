package com.work.javafx.controller.teacher;
import com.work.javafx.controller.admin.CourseDetailsController;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ShowMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import com.work.javafx.model.UltimateCourse;
import javafx.scene.control.Alert;

import javax.swing.*;

public class CourseManagementContent implements Initializable {
static Gson gson = new Gson();
    @FXML
    private ComboBox<String> semesterComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private Button ApplyForNewCourse;

    @FXML
    private TableView<UltimateCourse> courseTable;

    private ObservableList<UltimateCourse> courseList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupTable();
        loadData();
    }

    private void setupComboBoxes() {
        // Setup semester combo box
        ObservableList<String> semesters = FXCollections.observableArrayList(
            "2024-2025-1",
            "2024-2025-2",
            "全部历史学期"
        );
        semesterComboBox.setItems(semesters);
        semesterComboBox.getSelectionModel().selectFirst();

        // Setup status combo box
        ObservableList<String> statuses = FXCollections.observableArrayList(
            "全部",
            "当前授课",
            "历史授课",
            "我的申请"
        );
        statusComboBox.setItems(statuses);
        statusComboBox.getSelectionModel().selectFirst();
    }

    private void setupTable() {
        courseList = FXCollections.observableArrayList();
        courseTable.setItems(courseList);
        
        // 设置表格列宽策略为自适应填充可用空间
        courseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // 确保表格能够填充父容器的可用空间
        VBox.setVgrow(courseTable, Priority.ALWAYS);
    }

    private void loadData() {
        String term = semesterComboBox.getValue();
        Map<String,String> Param = new HashMap<>();
        Param.put("term",term);
        NetworkUtils.get("/class/list", Param, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if(res.has("code") && res.get("code").getAsInt()==200){
                    // 获取分页数据
                    JsonObject dataObject = res.getAsJsonObject("data");
                    JsonArray dataArray = dataObject.getAsJsonArray("list");
                    int totalCourses = dataObject.get("total").getAsInt();
                    int pageSize = dataObject.get("pageSize").getAsInt();
                    int pageNum = dataObject.get("pageNum").getAsInt();
                    
                    Type couserListType = new TypeToken<List<UltimateCourse>>(){}.getType();
                    List<UltimateCourse> loadCourseList = gson.fromJson(dataArray,couserListType);
                    
                    // 处理每个课程对象，添加操作按钮
                    for (UltimateCourse course : loadCourseList) {
                        // 转换属性名称以匹配前端要求
                        setCourseProperties(course);
                    }
                    
                    courseList.clear();
                    courseList.addAll(loadCourseList);
                }else{
                    System.out.println("失败！"+ res.get("msg").getAsString());
                }
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        });
    }
    
    // 为课程设置前端所需属性
    private void setCourseProperties(UltimateCourse course) {
        // 设置操作按钮，根据课程状态判断显示哪些按钮
        String courseStatus = course.getStatus();
        HBox actionButtons;
        
        if ("已通过".equals(courseStatus)) {
            actionButtons = createActionButtons("active", course);
        } else if ("待审批".equals(courseStatus)) {
            actionButtons = createActionButtons("proposed", course);
        } else if ("已拒绝".equals(courseStatus)) {
            actionButtons = createActionButtons("rejected", course);
        } else {
            actionButtons = createActionButtons("active", course);
        }
        
        // 直接调用setter方法设置actions
        course.setActions(actionButtons);
        
        // 设置其他属性用于前端显示
        try {
            // 课程编号使用classNum
            java.lang.reflect.Field codeField = UltimateCourse.class.getDeclaredField("courseCode");
            codeField.setAccessible(true);
            codeField.set(course, course.getClassNum());
            
            // 课程名称使用name
            java.lang.reflect.Field nameField = UltimateCourse.class.getDeclaredField("courseName");
            nameField.setAccessible(true);
            nameField.set(course, course.getName());
            
            // 学期使用term
            java.lang.reflect.Field semesterField = UltimateCourse.class.getDeclaredField("semester");
            semesterField.setAccessible(true);
            semesterField.set(course, course.getTerm());
            
            // 学分使用point
            java.lang.reflect.Field creditsField = UltimateCourse.class.getDeclaredField("credits");
            creditsField.setAccessible(true);
            creditsField.set(course, String.valueOf(course.getPoint()));
            
            // 选课人数默认设置为0或容量
            java.lang.reflect.Field countField = UltimateCourse.class.getDeclaredField("studentCount");
            countField.setAccessible(true);
            countField.set(course, String.valueOf(course.getCapacity()));
            
            // 教学大纲状态默认设为"已提交"
            java.lang.reflect.Field syllabusField = UltimateCourse.class.getDeclaredField("syllabusStatus");
            syllabusField.setAccessible(true);
            syllabusField.set(course, "已提交");
            
        } catch (Exception e) {
            System.out.println("设置课程属性失败: " + e.getMessage());
        }
    }

    private HBox createActionButtons(String status, UltimateCourse course) {
        HBox buttons = new HBox(5);
        
        switch (status) {
            case "active":
                Button viewStudentsButton = createIconButton("👥", "查看学生名单");
                Button enterGradesButton = createTextButton("成绩录入", "primary");

                viewStudentsButton.setOnAction(event -> handleViewStudents(course));

                buttons.getChildren().addAll(
                    viewStudentsButton,
                    enterGradesButton
                );
                break;
            case "proposed":
                Button viewApplicationButton = createTextButton("查看申请详情", "secondary");
                Button cancelApplicationButton = createTextButton("撤销申请", "secondary");
                viewApplicationButton.setOnAction(event -> {
                    try {
                        handleViewApplicationButton(course);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                cancelApplicationButton.setOnAction(event -> {
                    try {
                        handlecancelApplicationButton(course);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
                buttons.getChildren().addAll(
                    viewApplicationButton,
                    cancelApplicationButton
                );
                break;
            case "past":
                Button viewDetailsButton = createTextButton("查看详情", "secondary");
                Button viewHistoryGradesButton = createTextButton("查看历史成绩", "secondary");

                buttons.getChildren().addAll(
                   viewDetailsButton,
                   viewHistoryGradesButton
                );
                break;
            case "rejected":
                Button viewRejectionReasonButton = createTextButton("查看驳回原因", "secondary");
                Button reEditApplicationButton = createTextButton("重新编辑申请", "secondary");
                viewRejectionReasonButton.setOnAction(event -> {
                    try {
                        handleviewRejectionReasonButton(course);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
                buttons.getChildren().addAll(
                    viewRejectionReasonButton,
                    reEditApplicationButton
                );
                break;
            default:
                System.out.println("未知的课程状态: " + status);
                break;
        }
        
        return buttons;
    }

    private Button createIconButton(String icon, String tooltip) {
        Button button = new Button(icon);
        button.getStyleClass().addAll("secondary", "action-icon-btn");
        button.setTooltip(new Tooltip(tooltip));
        return button;
    }

    private Button createTextButton(String text, String style) {
        Button button = new Button(text);
        button.getStyleClass().addAll(style, "action-btn");
        return button;
    }

    @FXML
    public  void ApplyForNewCourse(ActionEvent event) {
        try {
            // 加载新课程申请窗口
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/teacher/ApplyNewCourse.fxml"));
            Parent root = loader.load();
            
            // 获取控制器
            ApplyNewCourseController controller = loader.getController();
            
            // 创建新窗口
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL); // 设置为模态窗口
            popupStage.initStyle(StageStyle.DECORATED);
            popupStage.setTitle("申请新课程");
            popupStage.setScene(new Scene(root, 800, 600));
            
            // 设置最小窗口大小
            popupStage.setMinWidth(700);
            popupStage.setMinHeight(550);
            
            // 将窗口引用传递给控制器
            controller.setStage(popupStage);
            
            // 显示窗口
            popupStage.showAndWait();
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//处理查看驳回原因
    private void handleviewRejectionReasonButton(UltimateCourse course){
        int courseid = course.getId();
        String url = "/class/getReason/" + courseid;
        NetworkUtils.get(url, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject res = gson.fromJson(result,JsonObject.class);
                if(res.has("code") && res.get("code").getAsInt() == 200){
                    System.out.println("dsad");
                    String rejectionReason =  res.get("data").getAsString()+"";
                    if (rejectionReason == null || rejectionReason.isEmpty()) {
                        rejectionReason = "未提供退回原因";
                    }
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("驳回原因");
                    alert.setHeaderText("课程：" + course.getCourseName());
                    alert.setContentText("驳回原因：" + rejectionReason);
                    alert.showAndWait();
                }

            }

            @Override
            public void onFailure(Exception e) {
                JsonObject res = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")),JsonObject.class);
                ShowMessage.showErrorMessage("失败",res.get("msg").getAsString());
            }
        });

        
    }
    // 处理查看学生名单按钮点击事件
    private void handleViewStudents(UltimateCourse course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/teacher/StudentListView.fxml"));
            Parent root = loader.load();

            // 获取新窗口的控制器
            StudentListViewController controller = loader.getController();

            // 检查控制器是否成功获取
            if (controller == null) {
                 System.err.println("无法获取 StudentListViewController 控制器实例。");
                 // 可以显示一个错误提示给用户
                 Alert alert = new Alert(Alert.AlertType.ERROR);
                 alert.setTitle("内部错误");
                 alert.setHeaderText(null);
                 alert.setContentText("无法加载学生列表界面控制器。");
                 alert.showAndWait();
                 return; // 提前退出
            }
            
            // 将课程信息传递给新窗口的控制器并加载数据
            // 假设 UltimateCourse 的 classNum 对应 API 需要的 courseId
            controller.initializeData(course.getId()+"");

            // 创建新窗口 (Stage)
            Stage studentListStage = new Stage();
            studentListStage.initModality(Modality.APPLICATION_MODAL);
            studentListStage.initStyle(StageStyle.DECORATED);
            studentListStage.setTitle("学生名单 - " + course.getName());
            studentListStage.setScene(new Scene(root));

             studentListStage.setMinWidth(600);
             studentListStage.setMinHeight(400);

            // 显示窗口并等待用户关闭它
            studentListStage.showAndWait();

        } catch (IOException e) {
            System.err.println("无法加载学生名单窗口 FXML: " + e.getMessage());
            e.printStackTrace();
            // 向用户显示错误提示
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("加载错误");
            alert.setHeaderText(null);
            // 更具体的错误消息
            alert.setContentText("加载学生名单界面文件时出错: " + e.getMessage()); 
            alert.showAndWait();
        } catch (Exception e) {
             System.err.println("显示学生名单窗口时发生错误: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("运行时错误");
            alert.setHeaderText(null);
            alert.setContentText("显示学生名单时遇到未知错误: " + e.getMessage());
            alert.showAndWait();
        }
    }
    //查看课程详情
    private void handleViewApplicationButton(UltimateCourse course) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/CourseDetails.fxml"));
        Parent root = loader.load();
        //获取控制器
        CourseDetailsController controller = loader.getController();
         //创建新窗口
        Stage stage = new Stage();
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("课程详情——"+course.getName());
        stage.setScene(new Scene(root));
        // 设置最小窗口大小
        stage.setMinWidth(700);
        stage.setMinHeight(550);
        // 设置课程ID并加载数据
        controller.loadCourseDetails(course.getId());
        // 设置为非审批页面
        controller.setApplicable(false);

        controller.setStage(stage);
        stage.showAndWait();
    }
//撤销申请
    private void handlecancelApplicationButton(UltimateCourse course){
        if(ShowMessage.showConfirmMessage("确认撤销？","确认撤销 " + course.getName()+ " 的申请？")){
            ShowMessage.showInfoMessage("撤销中","撤销中...请稍后...");
            int courseid = course.getId();
            String url = "/class/delete/" + courseid;
            NetworkUtils.post(url, null, new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    JsonObject res= gson.fromJson(result, JsonObject.class);
                    if(res.has("code") && res.get("code").getAsInt()==200){
                        ShowMessage.showInfoMessage("成功","撤销 " + course.getName()+" 课程成功");
                    }else{
                        ShowMessage.showErrorMessage("失败",res.get("msg").getAsString());
                    }
                    loadData();
                }
                @Override
                public void onFailure(Exception e) {
                    int index  = e.getMessage().indexOf("{");
                    JsonObject res = gson.fromJson(e.getMessage().substring(index), JsonObject.class);
                    if(res.has("msg")){
                        ShowMessage.showErrorMessage("失败",res.get("msg").getAsString());
                    }
                }
            });
        }
    }
    // 课程类
    public static class Course {
        private final String courseCode;
        private final String courseName;
        private final String otherTeachers;
        private final String semester;
        private final String credits;
        private final String studentCount;
        private final String syllabusStatus;
        private final String status;
        private final HBox actions;

        public Course(String courseCode, String courseName, String otherTeachers, 
                     String semester, String credits, String studentCount, 
                     String syllabusStatus, String status, HBox actions) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.otherTeachers = otherTeachers;
            this.semester = semester;
            this.credits = credits;
            this.studentCount = studentCount;
            this.syllabusStatus = syllabusStatus;
            this.status = status;
            this.actions = actions;
        }

        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public String getOtherTeachers() { return otherTeachers; }
        public String getSemester() { return semester; }
        public String getCredits() { return credits; }
        public String getStudentCount() { return studentCount; }
        public String getSyllabusStatus() { return syllabusStatus; }
        public String getStatus() { return status; }
        public HBox getActions() { return actions; }
    }
}
