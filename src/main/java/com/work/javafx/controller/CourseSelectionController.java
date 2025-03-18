package com.work.javafx.controller;

import com.work.javafx.MainApplication;
import com.work.javafx.util.ShowMessage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CourseSelectionController implements Initializable {
    // 菜单按钮
    @FXML private Button homeBtn;
    @FXML private Button personalCenterBtn;
    @FXML private Button courseScheduleBtn;
    @FXML private Button courseSelectionBtn;
    @FXML private Button gradeQueryBtn;
    @FXML private Button teachingEvaluationBtn;
    
    // 选课导航按钮
    @FXML private Button thisTermBtn;
    @FXML private Button generalCourseBtn;
    @FXML private Button selectedCoursesBtn;
    @FXML private Button courseResultBtn;
    
    // 查询条件控件
    @FXML private ComboBox<String> collegeComboBox;
    @FXML private ComboBox<String> courseTypeComboBox;
    @FXML private TextField courseNameField;
    @FXML private Button searchButton;
    
    // 课程表格
    @FXML private TableView<Course> courseTableView;
    @FXML private TableColumn<Course, Integer> numberColumn;
    @FXML private TableColumn<Course, String> courseCodeColumn;
    @FXML private TableColumn<Course, Double> creditColumn;
    @FXML private TableColumn<Course, String> courseTypeColumn;
    @FXML private TableColumn<Course, String> teacherColumn;
    @FXML private TableColumn<Course, String> timeLocationColumn;
    @FXML private TableColumn<Course, String> capacityColumn;
    @FXML private TableColumn<Course, String> actionColumn;
    
    // 分页控件
    @FXML private Label courseCountLabel;
    @FXML private Label currentPageLabel;
    
    // 当前活动的菜单按钮
    private Button currentActiveMenuButton;
    // 当前活动的导航按钮
    private Button currentActiveNavButton;
    // 当前页码
    private int currentPage = 1;
    // 每页显示数量
    private final int PAGE_SIZE = 5;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化当前活动按钮
        currentActiveMenuButton = courseSelectionBtn;
        currentActiveNavButton = thisTermBtn;
        
        // 将选课系统按钮设置为活动状态
        courseSelectionBtn.getStyleClass().add("active-menu-item");
        
        // 初始化下拉框
        initComboBoxes();
        
        // 初始化表格
        initTableView();
        
        // 加载示例数据
        loadSampleCourses();
        
        System.out.println("选课系统界面初始化成功");
    }
    
    /**
     * 初始化下拉框选项
     */
    private void initComboBoxes() {
        // 学院下拉框
        ObservableList<String> colleges = FXCollections.observableArrayList(
            "所有学院", "计算机学院", "数学学院", "物理学院", "外语学院", "信息学院"
        );
        collegeComboBox.setItems(colleges);
        collegeComboBox.setValue("所有学院");
        
        // 课程性质下拉框
        ObservableList<String> courseTypes = FXCollections.observableArrayList(
            "所有类型", "必修课", "选修课", "通识课", "体育课"
        );
        courseTypeComboBox.setItems(courseTypes);
        courseTypeComboBox.setValue("所有类型");
    }
    
    /**
     * 初始化表格结构
     */
    private void initTableView() {
        // 设置列的值工厂
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        courseCodeColumn.setCellValueFactory(data -> {
            Course course = data.getValue();
            return new SimpleStringProperty(course.getCourseCode() + "\n" + course.getCourseName());
        });
        creditColumn.setCellValueFactory(new PropertyValueFactory<>("credit"));
        courseTypeColumn.setCellValueFactory(new PropertyValueFactory<>("courseType"));
        teacherColumn.setCellValueFactory(new PropertyValueFactory<>("teacher"));
        timeLocationColumn.setCellValueFactory(data -> {
            Course course = data.getValue();
            return new SimpleStringProperty(course.getTime() + "\n" + course.getLocation());
        });
        capacityColumn.setCellValueFactory(data -> {
            Course course = data.getValue();
            return new SimpleStringProperty(course.getSelected() + "/" + course.getCapacity());
        });
        
        // 自定义操作列
        actionColumn.setCellFactory(createActionCellFactory());
        
        // 自定义行样式
        courseTableView.setRowFactory(tv -> {
            TableRow<Course> row = new TableRow<Course>() {
                @Override
                protected void updateItem(Course course, boolean empty) {
                    super.updateItem(course, empty);
                    if (course == null || empty) {
                        getStyleClass().removeAll("available-row", "selected-row", "full-row");
                    } else {
                        getStyleClass().removeAll("available-row", "selected-row", "full-row");
                        if (course.isSelected()) {
                            getStyleClass().add("selected-row");
                        } else if (course.getSelected() >= course.getCapacity()) {
                            getStyleClass().add("full-row");
                        } else {
                            getStyleClass().add("available-row");
                        }
                    }
                }
            };
            return row;
        });
    }
    
    /**
     * 创建自定义操作列工厂
     */
    private Callback<TableColumn<Course, String>, TableCell<Course, String>> createActionCellFactory() {
        return column -> new TableCell<Course, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    Course course = getTableView().getItems().get(getIndex());
                    
                    Button actionButton = new Button();
                    if (course.isSelected()) {
                        actionButton.setText("退选");
                        actionButton.getStyleClass().add("withdraw-button");
                        actionButton.setOnAction(event -> {
                            course.setSelected(false);
                            course.setSelectedCount(course.getSelected() - 1);
                            getTableView().refresh();
                            ShowMessage.showInfoMessage("操作成功", "已成功退选课程：" + course.getCourseName());
                        });
                    } else if (course.getSelected() >= course.getCapacity()) {
                        actionButton.setText("已满");
                        actionButton.getStyleClass().add("disabled-button");
                        actionButton.setDisable(true);
                    } else {
                        actionButton.setText("选课");
                        actionButton.getStyleClass().add("select-button");
                        actionButton.setOnAction(event -> {
                            course.setSelected(true);
                            course.setSelectedCount(course.getSelected() + 1);
                            getTableView().refresh();
                            ShowMessage.showInfoMessage("操作成功", "已成功选择课程：" + course.getCourseName());
                        });
                    }
                    
                    HBox box = new HBox(actionButton);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        };
    }
    
    /**
     * 加载示例课程数据
     */
    private void loadSampleCourses() {
        ObservableList<Course> courses = FXCollections.observableArrayList(
            new Course(1, "MATH2005", "高等数学(II)", 4.0, "必修课", "李明", "周一 08:00-09:40", "理科楼 A203", 45, 23, false),
            new Course(2, "PHYS1003", "大学物理实验", 2.0, "必修课", "王华", "周六 10:10-11:50", "物理实验楼 B101", 30, 30, true),
            new Course(3, "COMP2013", "数据结构", 3.0, "必修课", "张伟", "周三 14:00-15:40", "信息楼 C305", 60, 45, false),
            new Course(4, "ENGL1002", "大学英语(II)", 3.0, "必修课", "Sarah Johnson", "周三 10:10-11:50", "外语楼 D201", 35, 25, true),
            new Course(5, "COMP2022", "Java程序设计", 2.5, "选修课", "刘强", "周四 16:00-17:40", "信息楼 C202", 40, 38, false)
        );
        
        courseTableView.setItems(courses);
        courseCountLabel.setText("共找到" + courses.size() + "门课程");
    }
    
    /**
     * 查询课程
     */
    @FXML
    private void searchCourses() {
        String collegeName = collegeComboBox.getValue();
        String courseType = courseTypeComboBox.getValue();
        String courseName = courseNameField.getText();
        
        System.out.println("查询课程: 学院=" + collegeName + ", 课程性质=" + courseType + ", 课程名称=" + courseName);
        
        // 这里应该根据查询条件请求后端API获取课程数据
        // 简单示例：重新加载示例数据
        loadSampleCourses();
    }
    
    /**
     * 显示本学期课程
     */
    @FXML
    private void showThisTermCourses() {
        switchActiveNavButton(thisTermBtn);
        System.out.println("显示本学期课程");
        loadSampleCourses();
    }
    
    /**
     * 显示通识选修课
     */
    @FXML
    private void showGeneralCourses() {
        switchActiveNavButton(generalCourseBtn);
        System.out.println("显示通识选修课");
        
        // 加载通识课程示例数据
        ObservableList<Course> courses = FXCollections.observableArrayList(
            new Course(1, "HIST1001", "中国近代史", 2.0, "通识课", "李华", "周二 08:00-09:40", "人文楼 A101", 100, 78, false),
            new Course(2, "PHIL1002", "哲学导论", 2.0, "通识课", "王明", "周四 14:00-15:40", "人文楼 A105", 80, 65, false),
            new Course(3, "ARTS1003", "音乐鉴赏", 1.5, "通识课", "张艺", "周五 19:00-20:40", "艺术楼 B201", 50, 50, true)
        );
        
        courseTableView.setItems(courses);
        courseCountLabel.setText("共找到" + courses.size() + "门课程");
    }
    
    /**
     * 显示已选课程
     */
    @FXML
    private void showSelectedCourses() {
        switchActiveNavButton(selectedCoursesBtn);
        System.out.println("显示已选课程");
        
        // 加载已选课程示例数据
        ObservableList<Course> selectedCourses = FXCollections.observableArrayList(
            new Course(1, "PHYS1003", "大学物理实验", 2.0, "必修课", "王华", "周六 10:10-11:50", "物理实验楼 B101", 30, 30, true),
            new Course(2, "ENGL1002", "大学英语(II)", 3.0, "必修课", "Sarah Johnson", "周三 10:10-11:50", "外语楼 D201", 35, 25, true)
        );
        
        courseTableView.setItems(selectedCourses);
        courseCountLabel.setText("共选择" + selectedCourses.size() + "门课程");
    }
    
    /**
     * 显示选课结果
     */
    @FXML
    private void showCourseResults() {
        switchActiveNavButton(courseResultBtn);
        System.out.println("显示选课结果");
        
        // 加载选课结果示例数据
        ObservableList<Course> courseResults = FXCollections.observableArrayList(
            new Course(1, "MATH2005", "高等数学(II)", 4.0, "必修课", "李明", "周一 08:00-09:40", "理科楼 A203", 45, 23, false),
            new Course(2, "PHYS1003", "大学物理实验", 2.0, "必修课", "王华", "周六 10:10-11:50", "物理实验楼 B101", 30, 30, true),
            new Course(3, "ENGL1002", "大学英语(II)", 3.0, "必修课", "Sarah Johnson", "周三 10:10-11:50", "外语楼 D201", 35, 25, true)
        );
        
        courseTableView.setItems(courseResults);
        courseCountLabel.setText("共选择" + courseResults.size() + "门课程");
    }
    
    /**
     * 上一页
     */
    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            currentPageLabel.setText(String.valueOf(currentPage));
            // 这里应该请求对应页码的数据
            System.out.println("显示第" + currentPage + "页");
        }
    }
    
    /**
     * 下一页
     */
    @FXML
    private void nextPage() {
        currentPage++;
        currentPageLabel.setText(String.valueOf(currentPage));
        // 这里应该请求对应页码的数据
        System.out.println("显示第" + currentPage + "页");
    }
    
    /**
     * 切换菜单按钮高亮状态
     */
    private void switchActiveButton(Button newActiveButton) {
        // 移除当前活动按钮的高亮样式
        if (currentActiveMenuButton != null) {
            currentActiveMenuButton.getStyleClass().remove("active-menu-item");
        }

        // 为新的活动按钮添加高亮样式
        if (newActiveButton != null && !newActiveButton.getStyleClass().contains("active-menu-item")) {
            newActiveButton.getStyleClass().add("active-menu-item");
        }

        // 更新当前活动按钮
        currentActiveMenuButton = newActiveButton;
    }
    
    /**
     * 切换导航按钮高亮状态
     */
    private void switchActiveNavButton(Button newActiveButton) {
        // 移除当前活动按钮的高亮样式
        if (currentActiveNavButton != null) {
            currentActiveNavButton.getStyleClass().remove("active-nav-button");
        }

        // 为新的活动按钮添加高亮样式
        if (newActiveButton != null && !newActiveButton.getStyleClass().contains("active-nav-button")) {
            newActiveButton.getStyleClass().add("active-nav-button");
        }

        // 更新当前活动按钮
        currentActiveNavButton = newActiveButton;
    }
    
    /**
     * 切换到首页
     */
    @FXML
    private void switchToHome() {
        try {
            MainApplication.changeView("MainView.fxml", "css/MainView.css");
        } catch (IOException e) {
            e.printStackTrace();
            ShowMessage.showErrorMessage("切换到首页失败", null);
        }
    }

    /**
     * 切换到个人中心
     */
    @FXML
    private void switchToPersonalCenter() {
        switchActiveButton(personalCenterBtn);
        ShowMessage.showInfoMessage("功能提示", "个人中心功能正在开发中");
    }

    /**
     * 切换到课表查询
     */
    @FXML
    private void switchToCourseSchedule() {
        try {
            MainApplication.changeView("CourseSchedule.fxml", "css/CourseSchedule.css");
        } catch (IOException e) {
            e.printStackTrace();
            ShowMessage.showErrorMessage("切换到课表查询失败", null);
        }
    }

    /**
     * 切换到选课系统
     */
    @FXML
    private void switchToCourseSelection() {
        // 当前已经是选课系统页面，无需任何操作
        System.out.println("当前已经是选课系统页面");
    }

    /**
     * 切换到成绩查询
     */
    @FXML
    private void switchToGradeQuery() {
        switchActiveButton(gradeQueryBtn);
        ShowMessage.showInfoMessage("功能提示", "成绩查询功能正在开发中");
    }

    /**
     * 切换到教学评价
     */
    @FXML
    private void switchToTeachingEvaluation() {
        switchActiveButton(teachingEvaluationBtn);
        ShowMessage.showInfoMessage("功能提示", "教学评价功能正在开发中");
    }
    
    /**
     * 退出登录
     */
    @FXML
    private void logout() {
        try {
            MainApplication.changeView("Login.fxml", "css/Login.css");
        } catch (IOException e) {
            e.printStackTrace();
            ShowMessage.showErrorMessage("退出登录失败", null);
        }
    }
    
    /**
     * 课程数据模型类
     */
    public static class Course {
        private final int number;
        private final String courseCode;
        private final String courseName;
        private final double credit;
        private final String courseType;
        private final String teacher;
        private final String time;
        private final String location;
        private final int capacity;
        private int selectedCount;
        private boolean selected;
        
        public Course(int number, String courseCode, String courseName, double credit, 
                    String courseType, String teacher, String time, String location, 
                    int capacity, int selectedCount, boolean selected) {
            this.number = number;
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.credit = credit;
            this.courseType = courseType;
            this.teacher = teacher;
            this.time = time;
            this.location = location;
            this.capacity = capacity;
            this.selectedCount = selectedCount;
            this.selected = selected;
        }
        
        // Getters
        public int getNumber() { return number; }
        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public double getCredit() { return credit; }
        public String getCourseType() { return courseType; }
        public String getTeacher() { return teacher; }
        public String getTime() { return time; }
        public String getLocation() { return location; }
        public int getCapacity() { return capacity; }
        public int getSelected() { return selectedCount; }
        public boolean isSelected() { return selected; }
        
        // Setters
        public void setSelectedCount(int selectedCount) { this.selectedCount = selectedCount; }
        public void setSelected(boolean selected) { this.selected = selected; }
    }
} 