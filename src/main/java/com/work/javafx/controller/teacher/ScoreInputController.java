package com.work.javafx.controller.teacher;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.work.javafx.entity.Data;
import com.work.javafx.model.CourseForScoreInput;
import com.work.javafx.model.ScoreEntry;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ResUtil;
import com.work.javafx.util.ShowMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ScoreInputController implements Initializable {


    // 信息卡标签
    @FXML
    private Label pendingClassesLabel;
    @FXML
    private Label enteredClassesLabel;



    // 筛选控件
    @FXML
    private ComboBox<CourseForScoreInput> courseComboBox;

    // 表格视图和列
    @FXML
    private TableView<ScoreEntry> scoreTableView;
    @FXML
    private TableColumn<ScoreEntry, String> studentIdCol;
    @FXML
    private TableColumn<ScoreEntry, String> nameCol;
    @FXML
    private TableColumn<ScoreEntry, String> classCol;
    @FXML
    private TableColumn<ScoreEntry, String> courseCol;
    @FXML
    private TableColumn<ScoreEntry, Integer> regularScoreCol;
    @FXML
    private TableColumn<ScoreEntry, Integer> finalScoreCol;
    @FXML
    private TableColumn<ScoreEntry, Integer> totalScoreCol;
    @FXML
    private TableColumn<ScoreEntry, String> statusCol;
    @FXML
    private TableColumn<ScoreEntry, Void> actionCol;

    // 统计标签
    @FXML
    private Label avgScoreLabel;
    @FXML
    private Label maxScoreLabel;
    @FXML
    private Label minScoreLabel;
    @FXML
    private Label passRateLabel;
    @FXML
    private Label excellentRateLabel;

    // 图表
    @FXML
    private BarChart<String, Number> gradeDistributionChart;
    @FXML
    private CategoryAxis barChartXAxis;
    @FXML
    private NumberAxis barChartYAxis;
    @FXML
    private PieChart gradeLevelChart;
    @FXML
    private VBox pieChartLegend; // 自定义图例的VBox
    //按钮
    @FXML
    private Button submitLockButton;
    Gson gson = new Gson();
    // 数据
    private ObservableList<ScoreEntry> scoreData = FXCollections.observableArrayList();
    private CourseForScoreInput currentCourse; // 添加当前课程引用



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //  填充下拉框
        setupComboBoxes();

        // 配置表格视图列
        setupTableView();


        //  设置图表
        setupCharts();

        //计算并显示初始统计数据
        updateStatistics();
        //统计数据
//        fetchStatistics();

    }
/**
 * 初始化获取统计数据
 * */
//private  void fetchStatistics(){
//    NetworkUtils.get("/Teacher/countClass", new NetworkUtils.Callback<String>() {
//        @Override
//        public void onSuccess(String result) throws IOException {
//            JsonObject res = gson.fromJson(result, JsonObject.class);
//            if(res.get("code").getAsInt() == 200){
//                JsonObject data = res.getAsJsonObject("data");
//                int activeClass = data.get("activeClass").getAsInt();
//                int pendingClass = data.get("pendingClass").getAsInt();
//                pendingClassesLabel.setText(pendingClass+"");
//                enteredClassesLabel.setText(activeClass+"");
//            }
//        }
//
//        @Override
//        public void onFailure(Exception e) {
//
//        }
//    });
//}
    private void setupComboBoxes() {
        ObservableList<CourseForScoreInput> courseList = FXCollections.observableArrayList();
        Map<String, String> params = new HashMap<>();
        params.put("term", Data.getInstance().getCurrentTerm());
        System.out.println(Data.getInstance().getCurrentTerm());
        params.put("pageSize", "100");
        params.put("pageNum", "1");
        NetworkUtils.get("/class/list", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) throws IOException {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    JsonObject data = res.getAsJsonObject("data");
                    JsonArray list = data.getAsJsonArray("list");
                    for (int i = 0; i < list.size(); i++) {
                        JsonObject c = list.get(i).getAsJsonObject();
                        if(c.get("status").getAsString().equals("已通过")) {
                            courseList.add(new CourseForScoreInput(c.get("id").getAsString(), c.get("name").getAsString(), c.get("peopleNum").getAsInt(), c.get("regularRatio").getAsDouble(), c.get("finalRatio").getAsDouble()));
                        }
                        }
                }

                courseComboBox.setItems(courseList);
                courseComboBox.getSelectionModel().selectFirst();
            }

            @Override
            public void onFailure(Exception e) {
                JsonObject res = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")), JsonObject.class);
                System.err.println(res.get("msg").getAsString());
            }
        });
    }

    private <T> Callback<TableColumn<ScoreEntry, Integer>, TableCell<ScoreEntry, Integer>> createStrictFocusAwareCellFactory() {
        return column -> new TextFieldTableCell<>(new ScoreStringConverter()) {
            @Override
            public void startEdit() {
                super.startEdit();

                if (getGraphic() instanceof TextField tf) {
                    // 添加 focus 监听器
                    tf.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused) {
                            // 如果失去焦点，且当前是编辑状态，就保存
                            if (isEditing()) {
                                commitEdit(new ScoreStringConverter().fromString(tf.getText()));
                            }
                        }
                    });
                }
            }
        };
    }



    private void setupTableView() {
        // 设置表格为可编辑
        scoreTableView.setEditable(true);

        // 将列绑定到ScoreEntry属性
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("sduid"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        classCol.setCellValueFactory(new PropertyValueFactory<>("className"));
        courseCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));

        // 使用正确的属性绑定
        totalScoreCol.setCellValueFactory(cellData -> cellData.getValue().totalScoreProperty().asObject());
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // 使用验证功能使分数列可编辑（使用Integer ScoreStringConverter）
        regularScoreCol.setCellValueFactory(cellData -> cellData.getValue().regularScoreProperty().asObject());
        regularScoreCol.setCellFactory(createStrictFocusAwareCellFactory());
        regularScoreCol.setOnEditCommit(event -> {
            ScoreEntry entry = event.getRowValue();
            if (entry != null) {
                entry.setRegularScore(event.getNewValue());
                scoreTableView.refresh();
                updateStatistics();
            }
        });

        finalScoreCol.setCellValueFactory(cellData -> cellData.getValue().finalScoreProperty().asObject());
        finalScoreCol.setCellFactory(createStrictFocusAwareCellFactory());
        finalScoreCol.setOnEditCommit(event -> {
            ScoreEntry entry = event.getRowValue();
            if (entry != null) {
                entry.setFinalScore(event.getNewValue());
                scoreTableView.refresh();
                updateStatistics();
            }
        });


        // 样式化状态列
        statusCol.setCellFactory(column -> new TableCell<ScoreEntry, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                    getStyleClass().removeAll("status-normal", "status-absent", "status-fail"); // 清除先前的样式
                } else {
                    setText(item);
                    // 添加新样式前删除旧样式
                    getStyleClass().removeAll("status-normal", "status-absent", "status-fail");
                    if ("通过".equals(item)) {
                        // 使用CSS类进行样式设置
                        getStyleClass().add("status-normal");
                    } else if ("不及格".equals(item)) {
                        getStyleClass().add("status-fail"); // 匹配原型名称
                    } else {
                        // 可能处理其他状态如"缺考"
                        if ("缺考".equals(item)) { // 缺考状态的示例
                            getStyleClass().add("status-absent");
                        }
                        // 默认样式（无特定类）
                    }
                }
            }
        });


        // 添加保存按钮到操作列
        actionCol.setCellFactory(param -> new TableCell<ScoreEntry, Void>() {
            private final Button saveButton = new Button("保存");

            {
                saveButton.getStyleClass().add("secondary-button"); // 应用CSS样式
                saveButton.setOnAction(event -> {
                    ScoreEntry entry = getTableView().getItems().get(getIndex());
                    Map<String, String> params = new HashMap<>();
                    params.put("studentId", entry.getStudentId());
                    params.put("courseId", currentCourse.getId());
                    params.put("regular", String.valueOf(entry.getRegularScore().intValue()));
                    params.put("finalScore", String.valueOf(entry.getFinalScore().intValue()));
                    params.put("grade", String.valueOf(entry.getTotalScore().intValue()));
                    params.put("rank", "0");
                    params.put("term", Data.getInstance().getCurrentTerm());
                    for (Map.Entry<String, String> entry1 : params.entrySet()) {
                        System.out.println(entry1.getKey() + " : " + entry1.getValue());
                    }
                    NetworkUtils.post("/grade/setGrade", params, null, new NetworkUtils.Callback<String>() {
                        @Override
                        public void onSuccess(String result) throws IOException {
                            JsonObject res = gson.fromJson(result, JsonObject.class);
                            if (res.has("code") && res.get("code").getAsInt() == 200) {
                                ShowMessage.showInfoMessage("保存成功", res.get("msg").getAsString());
                            } else {
                                ShowMessage.showErrorMessage("保存失败", res.get("msg").getAsString());
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            JsonObject res = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")), JsonObject.class);
                            ShowMessage.showErrorMessage("保存失败", res.get("msg").getAsString());
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(saveButton);
                }
            }
        });

        // 设置表格的数据源
        scoreTableView.setItems(scoreData);
    }

    private void setupCharts() {
        // 条形图设置
        barChartXAxis.setLabel("分数段");
        barChartYAxis.setLabel("人数");
        gradeDistributionChart.setTitle("");
        gradeDistributionChart.setLegendVisible(false);

        // --- Y轴整数刻度的配置 ---
        barChartYAxis.setAutoRanging(false); // 禁用自动范围以手动控制刻度
        barChartYAxis.setLowerBound(0);      // 坐标轴从0开始
        barChartYAxis.setMinorTickCount(0); // 整数之间没有次要刻度
        barChartYAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                // 只显示整数值
                if (object.doubleValue() == object.intValue()) {
                    return String.valueOf(object.intValue());
                }
                return ""; // 如有出现，隐藏非整数刻度标签
            }

            @Override
            public Number fromString(String string) {
                // 不需要用于格式化
                return null;
            }
        });


        // 饼图设置
        gradeLevelChart.setTitle("");
        gradeLevelChart.setLabelsVisible(false);
        gradeLevelChart.setLegendVisible(false);
    }


    public void updateStatistics() {
        if (scoreData.isEmpty()) {
            avgScoreLabel.setText("-");
            maxScoreLabel.setText("-");
            minScoreLabel.setText("-");
            passRateLabel.setText("-");
            excellentRateLabel.setText("-");
            updateBarChart(FXCollections.observableArrayList());
            updatePieChart(FXCollections.observableArrayList());
            return;
        }

        List<Integer> validScores = scoreData.stream()
                .map(ScoreEntry::getTotalScore)
                .filter(score -> score != null) // 只考虑有计算分数的条目
                .collect(Collectors.toList());

        if (validScores.isEmpty()) {
            avgScoreLabel.setText("-");
            maxScoreLabel.setText("-");
            minScoreLabel.setText("-");
            passRateLabel.setText("0%");
            excellentRateLabel.setText("0%");
            updateBarChart(FXCollections.observableArrayList());
            updatePieChart(FXCollections.observableArrayList());
            return;
        }


        double avg = validScores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        int max = validScores.stream().mapToInt(Integer::intValue).max().orElse(0);
        int min = validScores.stream().mapToInt(Integer::intValue).min().orElse(0);
        long totalCount = validScores.size(); // 只计算有效分数
        long passCount = validScores.stream().filter(score -> score >= 60).count();
        long excellentCount = validScores.stream().filter(score -> score >= 90).count(); // 假设90+是优秀

        avgScoreLabel.setText(String.format("%.1f", avg));
        maxScoreLabel.setText(String.format("%d", max));
        minScoreLabel.setText(String.format("%d", min));
        passRateLabel.setText(String.format("%.0f%%", totalCount == 0 ? 0 : (double) passCount / totalCount * 100));
        excellentRateLabel.setText(String.format("%.0f%%", totalCount == 0 ? 0 : (double) excellentCount / totalCount * 100)); // 修正计算

        // 更新图表
        updateBarChart(validScores);
        updatePieChart(validScores);

    }

    private void updateBarChart(List<Integer> scores) {
        long failCount = scores.stream().filter(s -> s < 60).count();
        long sixtyToSeventy = scores.stream().filter(s -> s >= 60 && s < 70).count();
        long seventyToEighty = scores.stream().filter(s -> s >= 70 && s < 80).count();
        long eightyToNinety = scores.stream().filter(s -> s >= 80 && s < 90).count();
        long ninetyToHundred = scores.stream().filter(s -> s >= 90 && s <= 100).count();

        // 找出最大计数以设置坐标轴的上限
        long maxCount = Math.max(failCount, Math.max(sixtyToSeventy, Math.max(seventyToEighty, Math.max(eightyToNinety, ninetyToHundred))));

        // --- 动态设置上限和刻度单位 ---
        barChartYAxis.setUpperBound(maxCount + 1); // 设置上限略高于最大计数
        barChartYAxis.setTickUnit(1);            // 设置刻度步长为1
        // --- 动态坐标轴更新结束 ---


        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("不及格", failCount));
        series.getData().add(new XYChart.Data<>("60-70", sixtyToSeventy));
        series.getData().add(new XYChart.Data<>("70-80", seventyToEighty));
        series.getData().add(new XYChart.Data<>("80-90", eightyToNinety));
        series.getData().add(new XYChart.Data<>("90-100", ninetyToHundred));

        gradeDistributionChart.getData().setAll(series); // 替换现有数据

        // 应用CSS样式设置条形颜色
        Platform.runLater(() -> {
            int i = 0;
            for (Node n : gradeDistributionChart.lookupAll(".chart-bar")) {
                n.getStyleClass().removeIf(style -> style.startsWith("bar-color-"));
                n.getStyleClass().add("bar-color-" + i++);
            }
        });
    }

    private void updatePieChart(List<Integer> scores) {
        long excellentCount = scores.stream().filter(s -> s >= 90).count(); // 优秀 90+
        long goodCount = scores.stream().filter(s -> s >= 80 && s < 90).count(); // 良好 80-89
        long passCount = scores.stream().filter(s -> s >= 60 && s < 80).count(); // 及格 60-79 (调整自原型的范围)
        long failCount = scores.stream().filter(s -> s < 60).count();      // 不及格 <60

        long totalValid = scores.size();
        if (totalValid == 0) {
            gradeLevelChart.setData(FXCollections.observableArrayList());
            pieChartLegend.getChildren().clear();
            return;
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        // 只添加计数>0的切片以避免混乱
        if (excellentCount > 0)
            pieData.add(new PieChart.Data(String.format("优秀 (%d)", excellentCount), excellentCount));
        if (goodCount > 0) pieData.add(new PieChart.Data(String.format("良好 (%d)", goodCount), goodCount));
        if (passCount > 0) pieData.add(new PieChart.Data(String.format("及格 (%d)", passCount), passCount));
        if (failCount > 0) pieData.add(new PieChart.Data(String.format("不及格 (%d)", failCount), failCount));

        gradeLevelChart.setData(pieData);

        // 更新自定义图例
        pieChartLegend.getChildren().clear(); // 清除旧图例项
        // 定义与上述顺序对应的颜色（优秀，良好，及格，不及格）
        String[] legendColors = {"#3F51B5", "#4CAF50", "#FFC107", "#FF9800"}; // 匹配原型颜色
        int colorIndex = 0;
        if (excellentCount > 0)
            pieChartLegend.getChildren().add(createLegendItem(legendColors[0], String.format("优秀(90+): %d人 (%.0f%%)", excellentCount, (double) excellentCount / totalValid * 100)));
        if (goodCount > 0)
            pieChartLegend.getChildren().add(createLegendItem(legendColors[1], String.format("良好(80-89): %d人 (%.0f%%)", goodCount, (double) goodCount / totalValid * 100)));
        if (passCount > 0)
            pieChartLegend.getChildren().add(createLegendItem(legendColors[2], String.format("及格(60-79): %d人 (%.0f%%)", passCount, (double) passCount / totalValid * 100)));
        if (failCount > 0)
            pieChartLegend.getChildren().add(createLegendItem(legendColors[3], String.format("不及格(<60): %d人 (%.0f%%)", failCount, (double) failCount / totalValid * 100)));


        // 设置数据后应用CSS样式设置饼图颜色
        Platform.runLater(() -> {
            int i = 0;
            for (PieChart.Data data : gradeLevelChart.getData()) {
                // 删除旧样式
                data.getNode().getStyleClass().removeIf(style -> style.startsWith("pie-color-"));
                // 基于索引添加新样式
                data.getNode().getStyleClass().add("pie-color-" + i++);
            }
        });
    }

    // 创建图例项的辅助方法
    private Node createLegendItem(String color, String text) {
        HBox legendItem = new HBox(8);
        legendItem.setAlignment(Pos.CENTER_LEFT);
        Rectangle colorRect = new Rectangle(15, 15);
        colorRect.setFill(Color.web(color)); // 使用web颜色字符串
        colorRect.setArcWidth(3);
        colorRect.setArcHeight(3);
        Label label = new Label(text);
        label.getStyleClass().add("legend-label"); // 如需要添加样式类
        legendItem.getChildren().addAll(colorRect, label);
        return legendItem;
    }


    // --- 事件处理器

    @FXML
    void handleQuery(ActionEvent event) {
        CourseForScoreInput selectedCourse = courseComboBox.getValue();
        currentCourse = selectedCourse; // 保存当前课程
        regularScoreCol.setText("平时分(" + selectedCourse.getRegularRatio() + ")");
        finalScoreCol.setText("期末分(" + selectedCourse.getFinalRatio() + ")");
        String url = "/class/";
        url += selectedCourse.getId();
        url += "/students";
        NetworkUtils.get(url, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) throws IOException {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    JsonArray data = res.getAsJsonArray("data");
                    scoreData.clear();
                    for (int i = 0; i < data.size(); i++) {
                        JsonObject element = data.get(i).getAsJsonObject();
                        ScoreEntry entry = new ScoreEntry(element.get("id").getAsString(), element.get("sduid").getAsString(), element.get("username").getAsString(), element.get("major").getAsString() + element.get("number").getAsString(), selectedCourse.getName(), element.get("regular").getAsInt(), element.get("finalScore").getAsInt(), "");
                        // 设置当前课程的成绩比例
                        entry.setScoreRatios(selectedCourse.getRegularRatio(), selectedCourse.getFinalRatio());
                        // 设置控制器引用，以便更新统计
                        entry.setController(ScoreInputController.this);
                        scoreData.add(entry);
                    }
                }
                updateStatistics(); // 为新数据更新统计
            }

            @Override
            public void onFailure(Exception e) {
                // 处理错误
            }
        });
    }


    @FXML
    void handleSortById(ActionEvent event) {
        System.out.println("按学号排序按钮已点击");
        scoreTableView.getSortOrder().clear(); // 清除先前的排序
        studentIdCol.setSortType(TableColumn.SortType.ASCENDING);
        scoreTableView.getSortOrder().add(studentIdCol);
        scoreTableView.sort();
    }

    @FXML
    void handleSortByScore(ActionEvent event) {
        System.out.println("按分数排序按钮已点击");
        scoreTableView.getSortOrder().clear();
        totalScoreCol.setSortType(TableColumn.SortType.DESCENDING); // 默认降序排序
        scoreTableView.getSortOrder().add(totalScoreCol);
        scoreTableView.sort();
    }

    @FXML
    void handleCancel(ActionEvent event) {
        System.out.println("取消按钮已点击");
        // --- 在此添加取消逻辑 ---
        updateStatistics();
    }

    @FXML
    void handleBatchSave(ActionEvent event) {
        System.out.println("批量保存按钮已点击");
        // --- 在此添加批量保存逻辑 ---
        // 遍历scoreData，识别已修改的行并保存
        System.out.println("可能要保存的项目：");
        for (ScoreEntry entry : scoreData) {
            // 如果实现了，这里可能检查'isDirty'标志
            System.out.println("  " + entry.getName() + ": " + entry.getTotalScore());
        }
        // 向用户显示确认
    }

    @FXML
    void handleSubmitLock(ActionEvent event) {
        System.out.println("提交并锁定按钮已点击");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定提交并锁定所有成绩吗？此操作可能无法撤销。", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Map<String, String> params = new HashMap<>();
                params.put("courseId", currentCourse.getId());
                // 打印参数信息
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    System.out.println(entry.getKey() + " : " + entry.getValue());
                }
                // 调用后端API
                NetworkUtils.post("/grade/releaseGrade", params, null, new NetworkUtils.Callback<String>() {
                    @Override
                    public void onSuccess(String result) throws IOException {
                        JsonObject res = gson.fromJson(result, JsonObject.class);
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            Platform.runLater(() -> {
                                ShowMessage.showInfoMessage("提交成功", res.get("msg").getAsString());
//                                scoreTableView.setEditable(false);
//                                submitLockButton.setDisable(true);
                            });
                        } else {
                            Platform.runLater(() -> {
                                ShowMessage.showErrorMessage("提交失败", res.get("msg").getAsString());
                            });
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        String msg = ResUtil.getMsgFromException(e);
                                ShowMessage.showErrorMessage("提交失败", msg);

                    }
                });
            }
        });
    }





    // --- 分数输入验证的辅助类 ---
    private static class ScoreStringConverter extends StringConverter<Integer> {
        @Override
        public String toString(Integer object) {
            return object == null ? "" : object.toString();
        }

        @Override
        public Integer fromString(String string) {
            if (string == null || string.trim().isEmpty()) {
                return 0;
            }
            try {
                int value = Integer.parseInt(string);
                if (value < 0 || value > 100) {
                    return 0;
                }
                return value;
            } catch (NumberFormatException e) {
                return 0;
            }
        }


    }

}