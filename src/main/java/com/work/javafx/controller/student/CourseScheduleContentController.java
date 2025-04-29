package com.work.javafx.controller.student;

import com.work.javafx.util.ShowMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 课表查询控制器
 * 负责处理课表查询界面的交互逻辑
 */
public class CourseScheduleContentController implements Initializable {

    // 查询条件控件
    @FXML private ComboBox<String> academicYearComboBox;
    @FXML private ComboBox<String> semesterComboBox;
    @FXML private Label semesterLabel;
    @FXML private Button queryButton;

    // 课表表格相关
    @FXML private TableView<CourseRow> scheduleTableView;
    @FXML private TableColumn<CourseRow, String> timeColumn;
    @FXML private TableColumn<CourseRow, String> mondayColumn;
    @FXML private TableColumn<CourseRow, String> tuesdayColumn;
    @FXML private TableColumn<CourseRow, String> wednesdayColumn;
    @FXML private TableColumn<CourseRow, String> thursdayColumn;
    @FXML private TableColumn<CourseRow, String> fridayColumn;
    @FXML private TableColumn<CourseRow, String> saturdayColumn;
    @FXML private TableColumn<CourseRow, String> sundayColumn;

    // 课表工具按钮
    @FXML private Button printButton;
    @FXML private Button exportButton;

    // 当前活动的按钮
    private Button currentActiveButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // 初始化下拉框选项
        initComboBoxes();

        // 初始化表格
        initTableView();

        // 加载示例课表数据
        loadSampleData();

        System.out.println("课表查询界面初始化成功");
    }

    /**
     * 初始化下拉框选项
     */
    private void initComboBoxes() {
        // 学年下拉框
        ObservableList<String> academicYears = FXCollections.observableArrayList(
                "2023-2024", "2024-2025", "2025-2026"
        );
        academicYearComboBox.setItems(academicYears);
        academicYearComboBox.setValue("2024-2025");

        // 学期下拉框
        ObservableList<String> semesters = FXCollections.observableArrayList(
                "第一学期", "第二学期"
        );
        semesterComboBox.setItems(semesters);
        semesterComboBox.setValue("第二学期");


    }

    /**
     * 初始化表格结构
     */
    private void initTableView() {
        // 设置列的单元格值工厂
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

        // 设置时间列的单元格工厂
        timeColumn.setCellFactory(column -> {
            return new TableCell<CourseRow, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        setText(null);

                        // 创建时间信息显示容器
                        VBox vbox = new VBox();
                        vbox.setAlignment(Pos.CENTER);
                        vbox.setSpacing(5);

                        // 分割时间信息
                        String[] parts = item.split("\n");
                        if (parts.length >= 2) {
                            // 节次信息
                            Label sessionLabel = new Label(parts[0]);
                            sessionLabel.getStyleClass().add("time-session");
                            sessionLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");

                            // 时间信息
                            Label timeLabel = new Label(parts[1]);
                            timeLabel.getStyleClass().add("time-detail");
                            timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #777;");

                            vbox.getChildren().addAll(sessionLabel, timeLabel);
                        } else {
                            Label label = new Label(item);
                            vbox.getChildren().add(label);
                        }

                        // 设置样式
                        setGraphic(vbox);
                        setStyle("-fx-background-color: #f8f8f8;");
                    }
                }
            };
        });

        // 创建一个通用的单元格工厂方法来处理课程内容
        setCourseColumnCellFactory(mondayColumn);
        setCourseColumnCellFactory(tuesdayColumn);
        setCourseColumnCellFactory(wednesdayColumn);
        setCourseColumnCellFactory(thursdayColumn);
        setCourseColumnCellFactory(fridayColumn);
        setCourseColumnCellFactory(saturdayColumn);
        setCourseColumnCellFactory(sundayColumn);

        // 设置列的值工厂
        mondayColumn.setCellValueFactory(new PropertyValueFactory<>("monday"));
        tuesdayColumn.setCellValueFactory(new PropertyValueFactory<>("tuesday"));
        wednesdayColumn.setCellValueFactory(new PropertyValueFactory<>("wednesday"));
        thursdayColumn.setCellValueFactory(new PropertyValueFactory<>("thursday"));
        fridayColumn.setCellValueFactory(new PropertyValueFactory<>("friday"));
        saturdayColumn.setCellValueFactory(new PropertyValueFactory<>("saturday"));
        sundayColumn.setCellValueFactory(new PropertyValueFactory<>("sunday"));

        // 设置表格属性
        scheduleTableView.setRowFactory(tv -> {
            TableRow<CourseRow> row = new TableRow<>();
            row.setStyle("-fx-padding: 5px 0;");
            return row;
        });
    }

    /**
     * 设置课程列的单元格工厂
     */
    private void setCourseColumnCellFactory(TableColumn<CourseRow, String> column) {
        column.setCellFactory(col -> {
            return new TableCell<CourseRow, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty || item.trim().isEmpty()) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        setText(null);

                        VBox courseContainer = new VBox();
                        courseContainer.getStyleClass().add("course-content");
                        courseContainer.setSpacing(5);
                        courseContainer.setAlignment(Pos.CENTER);

                        // 分割课程信息
                        String[] parts = item.split("\n");
                        if (parts.length >= 2) {
                            // 课程名称
                            Label courseName = new Label(parts[0]);
                            courseName.setWrapText(true);
                            courseName.setStyle("-fx-font-weight: bold; -fx-text-fill: #d32f2f;");

                            // 地点信息
                            Label location = new Label(parts[1]);
                            location.setWrapText(true);
                            location.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

                            courseContainer.getChildren().addAll(courseName, location);
                        } else {
                            Label courseName = new Label(item);
                            courseName.setWrapText(true);
                            courseName.setStyle("-fx-font-weight: bold; -fx-text-fill: #d32f2f;");
                            courseContainer.getChildren().add(courseName);
                        }

                        VBox.setVgrow(courseContainer, Priority.ALWAYS);
                        setGraphic(courseContainer);

                        // 有课程的单元格设置不同的样式
                        setStyle("-fx-alignment: center;");
                    }
                }
            };
        });
    }

    /**
     * 加载示例课表数据
     */
    private void loadSampleData() {
        // 设置课程列的单元格工厂
        setCourseColumnCellFactory(mondayColumn);
        setCourseColumnCellFactory(tuesdayColumn);
        setCourseColumnCellFactory(wednesdayColumn);
        setCourseColumnCellFactory(thursdayColumn);
        setCourseColumnCellFactory(fridayColumn);
        setCourseColumnCellFactory(saturdayColumn);
        setCourseColumnCellFactory(sundayColumn);

        // 创建示例课表数据
        ObservableList<CourseRow> scheduleData = FXCollections.observableArrayList(
                new CourseRow("第1-2节\n08:00-09:40", "高等数学(II)\n理科楼A203\n李明", "", "数据结构\n信息楼C305\n张伟", "", "Java程序设计\n信息楼C202\n刘强", "", ""),
                new CourseRow("第3-4节\n10:10-11:50", "", "英语(II)\n外语楼D201\nSarah", "", "Java程序设计\n信息楼C202\n刘强", "", "大学物理实验\n物理实验楼B101\n王华", ""),
                new CourseRow("第5-6节\n14:00-15:40", "", "", "数据结构\n信息楼C305\n张伟", "高等数学(II)\n理科楼A203\n李明", "", "", ""),
                new CourseRow("第7-8节\n16:00-17:40", "英语(II)\n外语楼D201\nSarah", "", "", "", "大学物理\n理科楼B101\n王强", "", ""),
                new CourseRow("第9-10节\n19:00-20:40", "", "大学物理\n理科楼B101\n王强", "", "", "", "", "")
        );

        // 将数据设置到表格
        scheduleTableView.setItems(scheduleData);
    }

    /**
     * 查询课表
     */
    @FXML
    private void queryCourseSchedule() {
        String academicYear = academicYearComboBox.getValue();
        String semester = semesterComboBox.getValue();


        // 这里可以根据查询条件请求后端API获取课表数据
        // 简单示例：模拟查询操作，重新加载数据
        loadSampleData();

        ShowMessage.showInfoMessage("查询成功", "已加载" + academicYear + semester + "的课表");
    }

    /**
     * 打印课表
     */
    @FXML
    private void printSchedule() {
        System.out.println("打印课表");
        try {
            // 导入我们新创建的工具类
            com.work.javafx.util.ExportUtils.printNode(
                scheduleTableView, 
                academicYearComboBox.getValue() + " " + 
                semesterComboBox.getValue() + " " +
                " 课表"
            );
        } catch (Exception e) {
            e.printStackTrace();
            ShowMessage.showErrorMessage("打印错误", "打印课表时发生错误：" + e.getMessage());
        }
    }

    /**
     * 导出为Excel
     */
    @FXML
    private void exportToExcel() {
        System.out.println("导出Excel");
        try {
            // 获取当前窗口
            Stage stage = (Stage) exportButton.getScene().getWindow();
            
            // 调用导出工具
            com.work.javafx.util.ExportUtils.exportToExcel(
                scheduleTableView,
                academicYearComboBox.getValue(),
                semesterComboBox.getValue(),
                "个人课表",
                stage
            );
        } catch (Exception e) {
            e.printStackTrace();
            ShowMessage.showErrorMessage("导出错误", "导出Excel时发生错误：" + e.getMessage());
        }
    }

    /**
     * 切换按钮高亮状态
     * @param newActiveButton 需要高亮的按钮
     */
    private void switchActiveButton(Button newActiveButton) {
        // 移除当前活动按钮的高亮样式
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active-menu-item");
        }

        // 为新的活动按钮添加高亮样式
        if (newActiveButton != null && !newActiveButton.getStyleClass().contains("active-menu-item")) {
            newActiveButton.getStyleClass().add("active-menu-item");
        }

        // 更新当前活动按钮
        currentActiveButton = newActiveButton;
    }

    public void handleTermChange(ActionEvent inputMethodEvent) {
        semesterLabel.setText(academicYearComboBox.getValue()+"学年 "+semesterComboBox.getValue());

    }

    /**
     * 课表行数据模型
     */
    public static class CourseRow {
        private final String time;
        private final String monday;
        private final String tuesday;
        private final String wednesday;
        private final String thursday;
        private final String friday;
        private final String saturday;
        private final String sunday;

        public CourseRow(String time, String monday, String tuesday, String wednesday,
                         String thursday, String friday, String saturday, String sunday) {
            this.time = time;
            this.monday = monday;
            this.tuesday = tuesday;
            this.wednesday = wednesday;
            this.thursday = thursday;
            this.friday = friday;
            this.saturday = saturday;
            this.sunday = sunday;
        }

        // Getters
        public String getTime() { return time; }
        public String getMonday() { return monday; }
        public String getTuesday() { return tuesday; }
        public String getWednesday() { return wednesday; }
        public String getThursday() { return thursday; }
        public String getFriday() { return friday; }
        public String getSaturday() { return saturday; }
        public String getSunday() { return sunday; }
    }
} 