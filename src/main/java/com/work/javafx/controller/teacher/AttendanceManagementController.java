package com.work.javafx.controller.teacher;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AttendanceManagementController implements Initializable {

    // Filter Controls
    @FXML private ComboBox<String> courseComboBox;
    @FXML private ComboBox<String> classComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private DatePicker datePicker;

    // Attendance Table
    @FXML private TableView<AttendanceRecord> attendanceTableView;
    @FXML private TableColumn<AttendanceRecord, String> studentIdCol;
    @FXML private TableColumn<AttendanceRecord, String> studentNameCol;
    @FXML private TableColumn<AttendanceRecord, String> classNameCol;
    @FXML private TableColumn<AttendanceRecord, String> courseNameCol;
    @FXML private TableColumn<AttendanceRecord, String> statusCol;
    @FXML private TableColumn<AttendanceRecord, String> signInTimeCol;
    @FXML private TableColumn<AttendanceRecord, String> remarksCol;
    @FXML private TableColumn<AttendanceRecord, Void> actionCol;

    // Class Statistics Table
    @FXML private TableView<ClassAttendanceStat> classStatsTableView;
    @FXML private TableColumn<ClassAttendanceStat, String> statsClassNameCol;
    @FXML private TableColumn<ClassAttendanceStat, Integer> statsTotalStudentsCol;
    @FXML private TableColumn<ClassAttendanceStat, Integer> statsPresentCol;
    @FXML private TableColumn<ClassAttendanceStat, Integer> statsLateCol;
    @FXML private TableColumn<ClassAttendanceStat, Integer> statsAbsentCol;
    @FXML private TableColumn<ClassAttendanceStat, Integer> statsLeaveCol;
    @FXML private TableColumn<ClassAttendanceStat, String> statsRateCol;

    // 用于存储表格数据的列表
    private ObservableList<AttendanceRecord> attendanceData = FXCollections.observableArrayList();
    private ObservableList<ClassAttendanceStat> classStatsData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // --- Initialization Code ---

        // Set default date for DatePicker (optional)
        datePicker.setValue(LocalDate.now());

        // Configure TableView columns (Example for attendanceTableView)
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        studentNameCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        classNameCol.setCellValueFactory(new PropertyValueFactory<>("className"));
        courseNameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status")); 
        signInTimeCol.setCellValueFactory(new PropertyValueFactory<>("signInTime"));
        remarksCol.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        // --- 配置 statusCol 以显示带样式的 Label ---
        statusCol.setCellFactory(column -> new TableCell<AttendanceRecord, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null); // 清除图形
                    setStyle(""); // 清除样式
                } else {
                    Label statusLabel = new Label(item);
                    // 清除旧样式，防止复用单元格时样式叠加
                    statusLabel.getStyleClass().removeAll("status-badge", "status-normal", "status-late", "status-absent", "status-leave");
                    statusLabel.getStyleClass().add("status-badge"); // 添加基础样式
                    // 根据状态添加特定样式类
                    switch (item) {
                        case "正常":
                            statusLabel.getStyleClass().add("status-normal");
                            break;
                        case "迟到":
                            statusLabel.getStyleClass().add("status-late");
                            break;
                        case "缺勤":
                            statusLabel.getStyleClass().add("status-absent");
                            break;
                        case "请假":
                            statusLabel.getStyleClass().add("status-leave");
                            break;
                    }
                    setGraphic(statusLabel); // 将 Label 设置为单元格的图形
                    setText(null); // 清除文本
                    setAlignment(Pos.CENTER); // 居中显示
                }
            }
        });

        // --- 配置 actionCol 以显示 "修改" 按钮 ---
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("修改");

            {
                editButton.getStyleClass().add("action-btn"); // 应用按钮样式
                editButton.setOnAction(event -> {
                    AttendanceRecord record = getTableView().getItems().get(getIndex());
                    // TODO: 实现点击修改按钮的逻辑, 例如打开编辑对话框
                    System.out.println("修改记录: " + record.getStudentName());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // 将按钮放在 HBox 中并居中
                    HBox pane = new HBox(editButton);
                    pane.setAlignment(Pos.CENTER);
                    setGraphic(pane);
                }
            }
        });

        // 配置 classStatsTableView 列
        statsClassNameCol.setCellValueFactory(new PropertyValueFactory<>("className"));
        statsTotalStudentsCol.setCellValueFactory(new PropertyValueFactory<>("totalStudents"));
        statsPresentCol.setCellValueFactory(new PropertyValueFactory<>("presentCount"));
        statsLateCol.setCellValueFactory(new PropertyValueFactory<>("lateCount"));
        statsAbsentCol.setCellValueFactory(new PropertyValueFactory<>("absentCount"));
        statsLeaveCol.setCellValueFactory(new PropertyValueFactory<>("leaveCount"));
        statsRateCol.setCellValueFactory(new PropertyValueFactory<>("attendanceRate"));

        // Populate ComboBoxes (courses, classes, statuses) with data
        courseComboBox.getItems().addAll("全部课程", "高等数学 (II)", "数据结构与算法", "面向对象程序设计");
        classComboBox.getItems().addAll("全部班级", "计算机系", "软件工程", "信息安全");
        statusComboBox.getItems().addAll("全部状态", "正常", "迟到", "缺勤", "请假");
        courseComboBox.getSelectionModel().selectFirst(); // 默认选中第一个
        classComboBox.getSelectionModel().selectFirst();
        statusComboBox.getSelectionModel().selectFirst();

        // --- 加载虚拟数据 ---
        loadDummyData();
        attendanceTableView.setItems(attendanceData);
        classStatsTableView.setItems(classStatsData);

        System.out.println("考勤管理界面初始化完成");
    }

    // --- 加载虚拟数据的方法 ---
    private void loadDummyData() {
        // 清空旧数据，防止重复添加
        attendanceData.clear();
        classStatsData.clear();

        // 虚拟考勤记录
        attendanceData.addAll(
                new AttendanceRecord("2025001", "张三", "计算机系", "高等数学 (II)", "正常", "08:10:23", "-"),
                new AttendanceRecord("2025015", "李四", "计算机系", "高等数学 (II)", "迟到", "08:22:45", "交通拥堵"),
                new AttendanceRecord("2025042", "王五", "软件工程", "数据结构与算法", "缺勤", "-", "未通知"),
                new AttendanceRecord("2025078", "赵六", "软件工程", "数据结构与算法", "请假", "-", "病假"),
                new AttendanceRecord("2025036", "钱七", "计算机系", "高等数学 (II)", "正常", "08:05:36", "-"),
                new AttendanceRecord("2025102", "孙八", "信息安全", "面向对象程序设计", "正常", "10:08:11", "-"),
                new AttendanceRecord("2025119", "周九", "信息安全", "面向对象程序设计", "迟到", "10:15:01", "睡过头"),
                 new AttendanceRecord("2025002", "陈十", "计算机系", "高等数学 (II)", "正常", "08:09:55", "-"),
                 new AttendanceRecord("2025050", "吴十一", "软件工程", "数据结构与算法", "正常", "10:12:30", "-"),
                 new AttendanceRecord("2025123", "郑十二", "信息安全", "面向对象程序设计", "请假", "-", "事假")
        );

        // 虚拟班级统计数据
        classStatsData.addAll(
                new ClassAttendanceStat("计算机系", 68, 62, 3, 1, 2, "91.2%"),
                new ClassAttendanceStat("软件工程", 42, 36, 2, 1, 3, "85.7%"),
                new ClassAttendanceStat("信息安全", 35, 33, 1, 0, 1, "94.3%")
        );
    }

    // --- Event Handlers ---
    @FXML
    private void handleQueryAction() {
        // Get filter values
        String selectedCourse = courseComboBox.getValue();
        String selectedClass = classComboBox.getValue();
        String selectedStatus = statusComboBox.getValue();
        LocalDate selectedDate = datePicker.getValue(); // 获取选定的日期

        // --- 示例: 根据筛选条件过滤数据 ---
        // 注意: 这是一个简单的客户端过滤示例，实际应用中可能需要在后端或数据库层面进行查询
        ObservableList<AttendanceRecord> filteredData = FXCollections.observableArrayList();
        // 注意：这里我们从原始数据源 attendanceData 进行过滤，而不是当前表格中的数据
        for (AttendanceRecord record : attendanceData) {
             boolean courseMatch = "全部课程".equals(selectedCourse) || selectedCourse == null || record.getCourseName().equals(selectedCourse);
             boolean classMatch = "全部班级".equals(selectedClass) || selectedClass == null || record.getClassName().equals(selectedClass);
             boolean statusMatch = "全部状态".equals(selectedStatus) || selectedStatus == null || record.getStatus().equals(selectedStatus);
            // TODO: 如果需要，添加日期匹配逻辑，例如:
            // boolean dateMatch = selectedDate == null || record.getDate().equals(selectedDate); // 假设 AttendanceRecord 有 getDate() 方法

            if (courseMatch && classMatch && statusMatch /* && dateMatch */) {
                filteredData.add(record);
            }
        }
        attendanceTableView.setItems(filteredData); // 更新表格显示过滤后的数据

        System.out.println("查询考勤: Course=" + selectedCourse + ", Class=" + selectedClass + ", Status=" + selectedStatus + ", Date=" + selectedDate);
        // 注意: 班级统计表通常不会根据单条记录的筛选条件实时更新，可能需要另外的逻辑
    }

     @FXML
    private void handleExportAction() {
        // TODO: Implement data export logic
        System.out.println("导出数据");
    }

    @FXML
    private void handleMarkAttendanceAction() {
        // TODO: Implement logic to open a new window/dialog for marking attendance
        System.out.println("标记考勤");
    }

    @FXML
    private void handleGenerateReportAction() {
        // TODO: Implement report generation logic
        System.out.println("生成报告");
    }

    // --- Placeholder Data Models (Replace with your actual models) ---
    public static class AttendanceRecord {
        private final String studentId;
        private final String studentName;
        private final String className;
        private final String courseName;
        private final String status; // e.g., "正常", "迟到", "缺勤", "请假"
        private final String signInTime;
        private final String remarks;
        // private final LocalDate date; // 如果需要按日期筛选，可以添加日期属性

        public AttendanceRecord(String studentId, String studentName, String className, String courseName, String status, String signInTime, String remarks /*, LocalDate date*/) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.className = className;
            this.courseName = courseName;
            this.status = status;
            this.signInTime = signInTime;
            this.remarks = remarks;
            // this.date = date;
        }

        // Add Getters for all properties used in PropertyValueFactory
        public String getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public String getClassName() { return className; }
        public String getCourseName() { return courseName; }
        public String getStatus() { return status; }
        public String getSignInTime() { return signInTime; }
        public String getRemarks() { return remarks; }
        // public LocalDate getDate() { return date; }
    }

    public static class ClassAttendanceStat {
        private final String className;
        private final int totalStudents;
        private final int presentCount;
        private final int lateCount;
        private final int absentCount;
        private final int leaveCount;
        private final String attendanceRate; // 使用 String 以便直接显示百分比

        public ClassAttendanceStat(String className, int totalStudents, int presentCount, int lateCount, int absentCount, int leaveCount, String attendanceRate) {
            this.className = className;
            this.totalStudents = totalStudents;
            this.presentCount = presentCount;
            this.lateCount = lateCount;
            this.absentCount = absentCount;
            this.leaveCount = leaveCount;
            this.attendanceRate = attendanceRate;
        }

        // --- Getters for all properties ---
        public String getClassName() { return className; }
        public int getTotalStudents() { return totalStudents; }
        public int getPresentCount() { return presentCount; }
        public int getLateCount() { return lateCount; }
        public int getAbsentCount() { return absentCount; }
        public int getLeaveCount() { return leaveCount; }
        public String getAttendanceRate() { return attendanceRate; }
    }
}