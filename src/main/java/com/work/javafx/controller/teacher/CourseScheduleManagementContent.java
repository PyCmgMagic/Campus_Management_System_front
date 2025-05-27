package com.work.javafx.controller.teacher;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.work.javafx.controller.admin.CourseDetailsController;
import com.work.javafx.controller.student.CourseScheduleContentController;
import com.work.javafx.entity.Data;
import com.work.javafx.model.CourseRow;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ShowMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 课表管理控制器
 * 负责处理课表管理界面的交互逻辑
 */
public class CourseScheduleManagementContent implements Initializable {

    // 查询条件控件
    @FXML private ComboBox<String> academicYearComboBox;
    @FXML private ComboBox<String> weekComboBox;
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
    @FXML private Button exportButton;

    // 当前活动的按钮
    private Button currentActiveButton;
    Gson gson  =new Gson();
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // 初始化下拉框选项
        initComboBoxes();

        // 初始化表格
        initTableView();

        // 加载示例课表数据
        loadData();
        //初始化右上角显示
        handleTermChange(new ActionEvent());
        System.out.println("课表管理界面初始化成功");
    }

    /**
     * 初始化下拉框选项
     */
    private void initComboBoxes() {
        // 学年下拉框
        ObservableList<String> academicYears = Data.getInstance().getSemesterList();
        academicYearComboBox.setItems(academicYears);
        // 选择第一个选项
        if (academicYears != null && !academicYears.isEmpty()) {
            academicYearComboBox.setValue(academicYears.getFirst());
        }
        //周下拉框
        ObservableList<String> weeks = FXCollections.observableArrayList(
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                "11", "12", "13", "14", "15", "16", "17", "18"
        );
        weekComboBox.setItems(weeks);
        weekComboBox.setValue("1");
       

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
                private int courseId = -1; // 存储课程ID

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty || item.trim().isEmpty()) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                        courseId = -1; // 重置课程ID
                    } else {
                        setText(null);

                        VBox courseContainer = new VBox();
                        courseContainer.getStyleClass().add("course-content");
                        courseContainer.setSpacing(5);
                        courseContainer.setAlignment(Pos.CENTER);

                        // 分割课程信息
                        String[] parts = item.split("\n");
                        String info = item;
                        
                        // 检查是否包含课程ID
                        if (parts[0].startsWith("id:")) {
                            String[] idParts = parts[0].split(":", 3);
                            if (idParts.length >= 3) {
                                courseId = Integer.parseInt(idParts[1]);
                                info = idParts[2] + (parts.length > 1 ? "\n" + parts[1] : "");
                                parts[0] = idParts[2];
                            }
                        }
                        
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
                            Label courseName = new Label(parts[0]);
                            courseName.setWrapText(true);
                            courseName.setStyle("-fx-font-weight: bold; -fx-text-fill: #d32f2f;");
                            courseContainer.getChildren().add(courseName);
                        }

                        // 添加点击事件处理
                        final int finalCourseId = courseId;
                        courseContainer.setOnMouseClicked(event -> {
                            if (finalCourseId != -1) {
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/CourseDetails.fxml"));
                                    Parent root = loader.load();
                                    //获取控制器
                                    CourseDetailsController controller = loader.getController();
                                    //创建新窗口
                                    Stage stage = new Stage();
                                    stage.initStyle(StageStyle.DECORATED);
                                    stage.setTitle("课程详情——"+parts[0]);
                                    stage.setScene(new Scene(root));
                                    // 设置最小窗口大小
                                    stage.setMinWidth(700);
                                    stage.setMinHeight(550);
                                    // 设置课程ID并加载数据
                                    controller.loadCourseDetails(courseId);
                                    // 设置为非审批页面
                                    controller.setApplicable(false);

                                    controller.setStage(stage);
                                    stage.showAndWait();
                                } catch (Exception e) {
                                  e.printStackTrace();
                                }
                            }
                        });

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
     * 加载课表数据
     */
    private void loadData() {
        // 设置课程列的单元格工厂
        setCourseColumnCellFactory(mondayColumn);
        setCourseColumnCellFactory(tuesdayColumn);
        setCourseColumnCellFactory(wednesdayColumn);
        setCourseColumnCellFactory(thursdayColumn);
        setCourseColumnCellFactory(fridayColumn);
        setCourseColumnCellFactory(saturdayColumn);
        setCourseColumnCellFactory(sundayColumn);
//获取课程数据
        String url = "/class/getClassSchedule/";
        url += weekComboBox.getValue();
        Map<String,String> params = new HashMap<>();
        params.put("term", academicYearComboBox.getValue());
        NetworkUtils.get(url, params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) throws IOException {
                ObservableList<CourseRow> scheduleList = FXCollections.observableArrayList();
                JsonObject res = gson.fromJson(result,JsonObject.class);
                if(res.has("code") && res.get("code").getAsInt() == 200){
                    JsonArray data = res.getAsJsonArray("data");
                    CourseRow First = new CourseRow();
                    First.setTime("第1-2节\n08:00-09:50");
                    CourseRow Second = new CourseRow();
                    Second.setTime("第3-4节\n10:10-12:00");CourseRow Third = new CourseRow();
                    Third.setTime("第5-6节\n14:00-15:50");
                    CourseRow Fourth = new CourseRow();
                    Fourth.setTime("第7-8节\n16:10-18:00");
                    CourseRow Fifth = new CourseRow();
                    Fifth.setTime("第9-10节\n19:00-20:50");
                    for (int i = 0; i < data.size(); i++) {
                        JsonObject course = data.get(i).getAsJsonObject();
                        int index = 0;
                        try{
                            index = course.get("time").getAsInt();
                        }catch (Exception e){
                            continue;
                        }
                        String courseName = course.get("name").getAsString();
                        String classroom = "";
                        try{
                         classroom = course.get("classroom").getAsString();
                        } catch (Exception ignored){
                        }
                        int id = course.get("id").getAsInt();

                        switch (index % 5){
                            case 1:
                                if(index / 5 ==0){
                                    First.setMonday("id:" + id + ":" + courseName + "\n"+classroom);
                                } else if (index /5 == 1) {
                                    First.setTuesday("id:" + id + ":" + courseName + "\n" + classroom);
                                } else if (index / 5 == 2) {
                                    First.setWednesday("id:" + id + ":" + courseName + "\n" + classroom);
                                } else if (index / 5 == 3) {
                                    First.setThursday("id:" + id + ":" + courseName + "\n" + classroom);
                                } else if (index / 5 == 4) {
                                    First.setFriday("id:" + id + ":" + courseName + "\n" + classroom);
                                }
                                break;
                            case 2:
                                if(index / 5 ==0){
                                    Second.setMonday("id:" + id + ":" + courseName + "\n"+classroom);
                                } else if (index /5 == 1) {
                                    Second.setTuesday("id:" + id + ":" + courseName + "\n" + classroom);
                                } else if (index / 5 == 2) {
                                    Second.setWednesday("id:" + id + ":" + courseName + "\n" + classroom);
                                } else if (index / 5 == 3) {
                                    Second.setThursday("id:" + id + ":" + courseName + "\n" + classroom);
                                } else if (index / 5 == 4) {
                                    Second.setFriday("id:" + id + ":" + courseName + "\n" + classroom);
                                }
                                break;
                            case 3:
                                if(index / 5 ==0){
                                    Third.setMonday("id:" + id + ":" + courseName + "\n"+classroom);
                                } else if (index /5 == 1) {
                                    Third.setTuesday("id:" + id + ":" + courseName + "\n" + classroom);
                                } else if (index / 5 == 2) {
                                    Third.setWednesday("id:" + id + ":" + courseName + "\n" + classroom);
                                } else if (index / 5 == 3) {
                                    Third.setThursday("id:" + id + ":" + courseName + "\n" + classroom);
                                } else if (index / 5 == 4) {
                                    Third.setFriday("id:" + id + ":" + courseName + "\n" + classroom);
                                }
                                break;
                            case 4:
                                if(index / 5 ==0){
                                    Fourth.setMonday("id:" + id + ":" + courseName + "\n"+classroom);
                                } else if (index /5 == 1) {
                                    Fourth.setTuesday("id:" + id + ":" + courseName + "\n" + classroom);
                                } else if (index / 5 == 2) {
                                    Fourth.setWednesday("id:" + id + ":" + courseName + "\n" + classroom);
                                } else if (index / 5 == 3) {
                                    Fourth.setThursday("id:" + id + ":" + courseName + "\n" + classroom);
                                } else if (index / 5 == 4) {
                                    Fourth.setFriday("id:" + id + ":" + courseName + "\n" + classroom);
                                }
                                break;
                            case 0:
                                if(index / 5 ==0){
                                    Fifth.setMonday("id:" + id + ":" + courseName + "\n"+classroom);
                                } else if (index /5 == 1) {
                                    Fifth.setTuesday("id:" + id + ":" + courseName + "\n" + classroom);
                                } else if (index / 5 == 2) {
                                    Fifth.setWednesday("id:" + id + ":" + courseName + "\n" + classroom);
                                } else if (index / 5 == 3) {
                                    Fifth.setThursday("id:" + id + ":" + courseName + "\n" + classroom);
                                } else if (index / 5 == 4) {
                                    Fifth.setFriday("id:" + id + ":" + courseName + "\n" + classroom);
                                }
                                break;

                        }

                    }
                    scheduleList.addAll(First,Second,Third,Fourth,Fifth);
                    scheduleTableView.setItems(scheduleList);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });

    }
    /**
     * 查询课表
     */
    @FXML
    private void queryCourseSchedule() {
        String academicYear = academicYearComboBox.getValue();
        String week = weekComboBox.getValue();
        String scheduleType = "教师课表";
        loadData();

        ShowMessage.showInfoMessage("查询成功", "已加载" + academicYear + week + "的" + scheduleType);
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
            com.work.javafx.util.ExportUtils.exportToExcelForTeacher(
                scheduleTableView,
                academicYearComboBox.getValue(),
                    weekComboBox.getValue(),
                "教师课表",
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

    public void handleTermChange(ActionEvent actionEvent) {
        semesterLabel.setText(academicYearComboBox.getValue() + " 第 " + weekComboBox.getValue()+ " 周 ");


    }


}
