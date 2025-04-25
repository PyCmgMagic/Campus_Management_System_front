package com.work.javafx.controller.teacher;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.work.javafx.controller.student.UserInfo1;
import com.work.javafx.util.NetworkUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
                    JsonArray dataArray = res.getAsJsonArray("data");
                    Type couserListType = new TypeToken<List<UltimateCourse>>(){}.getType();
                    List<UltimateCourse> loadCourseList = gson.fromJson(dataArray,couserListType);
                    courseList.clear();
                    courseList.addAll(loadCourseList);
                    setupTable();
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

    private HBox createActionButtons(String status) {
        HBox buttons = new HBox(5);
        
        switch (status) {
            case "active":
                buttons.getChildren().addAll(
                    createIconButton("👥", "查看学生名单"),
                    createIconButton("📢", "发布班级通知"),
                    createTextButton("管理资料", "secondary"),
                    createTextButton("成绩录入", "primary")
                );
                break;
            case "proposed":
                buttons.getChildren().addAll(
                    createTextButton("查看申请详情", "secondary"),
                    createTextButton("撤销申请", "secondary")
                );
                break;
            case "past":
                buttons.getChildren().addAll(
                    createTextButton("查看详情", "secondary"),
                    createTextButton("查看历史成绩", "secondary")
                );
                break;
            case "rejected":
                buttons.getChildren().addAll(
                    createTextButton("查看驳回原因", "secondary"),
                    createTextButton("重新编辑申请", "secondary")
                );
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
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Course model class
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
