package com.work.javafx.controller.teacher;

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
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter; // 如果需要整数输入

import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ScoreInputController implements Initializable {

    // --- FXML 注入 ---

    // 信息卡标签
    @FXML private Label pendingClassesLabel;
    @FXML private Label enteredClassesLabel;
    @FXML private Label dueClassesLabel;
    @FXML private Label failedStudentsLabel;

    // 筛选控件
    @FXML private ComboBox<String> courseComboBox;

    // 表格视图和列
    @FXML private TableView<ScoreEntry> scoreTableView;
    @FXML private TableColumn<ScoreEntry, String> studentIdCol;
    @FXML private TableColumn<ScoreEntry, String> nameCol;
    @FXML private TableColumn<ScoreEntry, String> classCol;
    @FXML private TableColumn<ScoreEntry, String> courseCol;
    @FXML private TableColumn<ScoreEntry, Double> regularScoreCol;
    @FXML private TableColumn<ScoreEntry, Double> finalScoreCol;
    @FXML private TableColumn<ScoreEntry, Double> totalScoreCol;
    @FXML private TableColumn<ScoreEntry, String> statusCol;
    @FXML private TableColumn<ScoreEntry, Void> actionCol;

    // 统计标签
    @FXML private Label avgScoreLabel;
    @FXML private Label maxScoreLabel;
    @FXML private Label minScoreLabel;
    @FXML private Label passRateLabel;
    @FXML private Label excellentRateLabel;

    // 图表
    @FXML private BarChart<String, Number> gradeDistributionChart;
    @FXML private CategoryAxis barChartXAxis;
    @FXML private NumberAxis barChartYAxis;
    @FXML private PieChart gradeLevelChart;
    @FXML private VBox pieChartLegend; // 自定义图例的VBox


    // --- 数据 ---
    private ObservableList<ScoreEntry> scoreData = FXCollections.observableArrayList();

    // --- 初始化 ---

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. 填充下拉框
        setupComboBoxes();

        // 2. 配置表格视图列
        setupTableView();

        // 3. 加载初始数据（示例）
        loadSampleData();

        // 4. 设置图表
        setupCharts();

        // 5. 计算并显示初始统计数据
        updateStatistics();

    }

    // --- 设置方法 ---

    private void setupComboBoxes() {
        courseComboBox.setItems(FXCollections.observableArrayList("高等数学 (II)", "程序设计基础", "数据结构", "计算机网络", "全部课程"));

        // 如果需要，选择默认值
         courseComboBox.getSelectionModel().selectFirst();
    }

    private void setupTableView() {
        // 将列绑定到ScoreEntry属性
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        classCol.setCellValueFactory(new PropertyValueFactory<>("className"));
        courseCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        totalScoreCol.setCellValueFactory(new PropertyValueFactory<>("totalScore")); // 计算得出，不可编辑
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));     // 计算得出，不可编辑

        // 使用验证功能使分数列可编辑（使用DoubleStringConverter）
        regularScoreCol.setCellValueFactory(new PropertyValueFactory<>("regularScore"));
        regularScoreCol.setCellFactory(TextFieldTableCell.forTableColumn(new ScoreStringConverter()));
        regularScoreCol.setOnEditCommit(event -> {
            ScoreEntry entry = event.getRowValue();
            entry.setRegularScore(event.getNewValue());
            scoreTableView.refresh(); // 刷新行以显示更新的总分和状态
            updateStatistics();     // 重新计算统计数据
        });

        finalScoreCol.setCellValueFactory(new PropertyValueFactory<>("finalScore"));
        finalScoreCol.setCellFactory(TextFieldTableCell.forTableColumn(new ScoreStringConverter()));
        finalScoreCol.setOnEditCommit(event -> {
            ScoreEntry entry = event.getRowValue();
            entry.setFinalScore(event.getNewValue());
            scoreTableView.refresh();
            updateStatistics();
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
                         if("缺考".equals(item)) { // 缺考状态的示例
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
                    System.out.println("正在保存修改：" + entry.getName());
                    // --- 在此添加实际保存逻辑 ---
                    // 例如，调用服务更新数据库
                    // 向用户指示成功/失败
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
        // 我们将根据最大值在updateBarChart中动态设置上限和刻度单位
        barChartYAxis.setMinorTickCount(0); // 整数之间没有次要刻度
        // 可选：格式化刻度标签以确保它们显示为没有小数的整数
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
        // --- Y轴配置结束 ---


        // 饼图设置
        gradeLevelChart.setTitle("");
        gradeLevelChart.setLabelsVisible(false);
        gradeLevelChart.setLegendVisible(false);
    }


    // --- 数据加载和处理 ---

    private void loadSampleData() {
        // 在实际应用中，根据筛选条件从数据库或API加载
        scoreData.addAll(
            new ScoreEntry("2025001", "张三", "计算机系2班", "高等数学 (II)", 85.0, 78.0, "-"),
            new ScoreEntry("2025015", "李四", "计算机系2班", "高等数学 (II)", 65.0, 56.0, ""),
            new ScoreEntry("2025022", "王五", "计算机系2班", "高等数学 (II)", 92.0, 88.0, "-"),
            new ScoreEntry("2025037", "赵六", "计算机系2班", "高等数学 (II)", 73.0, 81.0, "-"),
            new ScoreEntry("2025043", "孙七", "计算机系2班", "高等数学 (II)", 61.0, 50.0, "")
            // 如需要可添加更多条目
        );

        // 更新信息卡标签（示例 - 使用实际数据替换）
        pendingClassesLabel.setText("5");
        enteredClassesLabel.setText("3");
        dueClassesLabel.setText("2");
        failedStudentsLabel.setText(String.valueOf(scoreData.stream().filter(s -> "不及格".equals(s.getStatus())).count()));
    }

     private void updateStatistics() {
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

        List<Double> validScores = scoreData.stream()
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


        double avg = validScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double max = validScores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double min = validScores.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        long totalCount = validScores.size(); // 只计算有效分数
        long passCount = validScores.stream().filter(score -> score >= 60.0).count();
        long excellentCount = validScores.stream().filter(score -> score >= 90.0).count(); // 假设90+是优秀

        avgScoreLabel.setText(String.format("%.1f", avg));
        maxScoreLabel.setText(String.format("%.1f", max));
        minScoreLabel.setText(String.format("%.1f", min));
        passRateLabel.setText(String.format("%.0f%%", totalCount == 0 ? 0 : (double) passCount / totalCount * 100));
        excellentRateLabel.setText(String.format("%.0f%%", totalCount == 0 ? 0 : (double) excellentCount / totalCount * 100)); // 修正计算

        // 更新图表
        updateBarChart(validScores);
        updatePieChart(validScores);

         // 更新信息卡中的不及格学生数
         failedStudentsLabel.setText(String.valueOf(scoreData.stream().filter(s -> "不及格".equals(s.getStatus())).count()));
    }

    private void updateBarChart(List<Double> scores) {
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
            for(Node n: gradeDistributionChart.lookupAll(".chart-bar")) {
                 n.getStyleClass().removeIf(style -> style.startsWith("bar-color-"));
                 n.getStyleClass().add("bar-color-" + i++);
            }
        });
    }
    private void updatePieChart(List<Double> scores) {
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
        if (excellentCount > 0) pieData.add(new PieChart.Data(String.format("优秀 (%d)", excellentCount), excellentCount));
        if (goodCount > 0) pieData.add(new PieChart.Data(String.format("良好 (%d)", goodCount), goodCount));
        if (passCount > 0) pieData.add(new PieChart.Data(String.format("及格 (%d)", passCount), passCount));
        if (failCount > 0) pieData.add(new PieChart.Data(String.format("不及格 (%d)", failCount), failCount));

        gradeLevelChart.setData(pieData);

         // 更新自定义图例
         pieChartLegend.getChildren().clear(); // 清除旧图例项
         // 定义与上述顺序对应的颜色（优秀，良好，及格，不及格）
         String[] legendColors = {"#3F51B5", "#4CAF50", "#FFC107", "#FF9800"}; // 匹配原型颜色
         int colorIndex = 0;
         if (excellentCount > 0) pieChartLegend.getChildren().add(createLegendItem(legendColors[0], String.format("优秀(90+): %d人 (%.0f%%)", excellentCount, (double)excellentCount/totalValid*100)));
         if (goodCount > 0) pieChartLegend.getChildren().add(createLegendItem(legendColors[1], String.format("良好(80-89): %d人 (%.0f%%)", goodCount, (double)goodCount/totalValid*100)));
         if (passCount > 0) pieChartLegend.getChildren().add(createLegendItem(legendColors[2], String.format("及格(60-79): %d人 (%.0f%%)", passCount, (double)passCount/totalValid*100)));
         if (failCount > 0) pieChartLegend.getChildren().add(createLegendItem(legendColors[3], String.format("不及格(<60): %d人 (%.0f%%)", failCount, (double)failCount/totalValid*100)));


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


    // --- 事件处理器（来自FXML onAction） ---

    @FXML
    void handleQuery(ActionEvent event) {
        String selectedCourse = courseComboBox.getValue();

        System.out.println("查询条件：");
        System.out.println("  课程: " + selectedCourse);

        // --- 在此添加实际查询逻辑 ---
        // 1. 调用服务/DAO根据筛选条件获取数据
        // 2. 清除现有scoreData: scoreData.clear();
        // 3. 用新结果填充scoreData: scoreData.addAll(newResults);
        // 4. 更新统计: updateStatistics();
        // 示例：模拟新查询结果
        scoreData.clear();
        if("数据结构".equals(selectedCourse)) {
             scoreData.addAll(
                new ScoreEntry("2025101", "小明", "软件工程1班", "数据结构", 75.0, 80.0, ""),
                new ScoreEntry("2025102", "小红", "软件工程1班", "数据结构", 95.0, 92.0, "优秀")
             );
        } else {
            loadSampleData(); // 如果不是数据结构则重新加载示例数据
        }
        updateStatistics(); // 为新数据更新统计
    }

    @FXML
    void handleBatchImport(ActionEvent event) {
        System.out.println("批量导入按钮已点击");
        // --- 在此添加文件选择器和导入逻辑 ---
    }

    @FXML
    void handleExportExcel(ActionEvent event) {
        System.out.println("导出Excel按钮已点击");
        // --- 在此添加Excel导出逻辑 ---
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
        // 可能是恢复更改或重新加载原始数据
         loadSampleData(); // 重新加载原始示例数据作为示例
         updateStatistics();
    }

    @FXML
    void handleBatchSave(ActionEvent event) {
        System.out.println("批量保存按钮已点击");
        // --- 在此添加批量保存逻辑 ---
        // 遍历scoreData，识别已修改的行并保存
        System.out.println("可能要保存的项目：");
        for(ScoreEntry entry : scoreData) {
            // 如果实现了，这里可能检查'isDirty'标志
            System.out.println("  " + entry.getName() + ": " + entry.getTotalScore());
        }
        // 向用户显示确认
    }

    @FXML
    void handleSubmitLock(ActionEvent event) {
        System.out.println("提交并锁定按钮已点击");
        // --- 在此添加提交并锁定逻辑 ---
        // 1. 执行最终验证
        // 2. 保存所有数据
        // 3. 可能禁用编辑控件
        // 4. 显示确认
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定提交并锁定所有成绩吗？此操作可能无法撤销。", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                System.out.println("正在提交并锁定...");
                // ... 实际提交逻辑 ...
                scoreTableView.setEditable(false); // 示例：禁用表格编辑
                // 如需要禁用其他按钮
            }
        });
    }


    // --- 数据模型内部类 ---
    // （如果偏好，可以放在自己的文件中）
    public static class ScoreEntry {
        private final String studentId;
        private final String name;
        private final String className;
        private final String courseName;
        private Double regularScore;
        private Double finalScore;
        private Double totalScore;
        private String status;
        private String remarks;
        // private boolean dirty = false; // 可选的用于跟踪更改的标志

        public ScoreEntry(String studentId, String name, String className, String courseName, Double regularScore, Double finalScore, String remarks) {
            this.studentId = studentId;
            this.name = name;
            this.className = className;
            this.courseName = courseName;
            this.regularScore = regularScore;
            this.finalScore = finalScore;
            this.remarks = remarks;
            calculateTotalScore(); // 初始计算
            updateStatus();        // 初始状态更新
        }

        // --- Getters ---
        public String getStudentId() { return studentId; }
        public String getName() { return name; }
        public String getClassName() { return className; }
        public String getCourseName() { return courseName; }
        public Double getRegularScore() { return regularScore; }
        public Double getFinalScore() { return finalScore; }
        public Double getTotalScore() { return totalScore; }
        public String getStatus() { return status; }
        public String getRemarks() { return remarks; }
        // public boolean isDirty() { return dirty; }

        // --- Setters ---
        public void setRegularScore(Double regularScore) {
            if (this.regularScore == null || !this.regularScore.equals(regularScore)) {
                 this.regularScore = regularScore;
                 calculateTotalScore();
                 updateStatus();
                 // this.dirty = true;
            }
        }
        public void setFinalScore(Double finalScore) {
             if (this.finalScore == null || !this.finalScore.equals(finalScore)) {
                this.finalScore = finalScore;
                calculateTotalScore();
                updateStatus();
                // this.dirty = true;
             }
        }
         public void setRemarks(String remarks) {
             if (this.remarks == null || !this.remarks.equals(remarks)) {
                this.remarks = remarks;
                // this.dirty = true;
             }
        }
        // public void setDirty(boolean dirty) { this.dirty = dirty; }


        // --- 计算逻辑 ---
        private void calculateTotalScore() {
            // 假设平时成绩占30%，期末成绩占70%。处理空值。
            if (regularScore != null && finalScore != null) {
                 // 为显示一致性，四舍五入到一位小数
                this.totalScore = Math.round((regularScore * 0.3 + finalScore * 0.7) * 10.0) / 10.0;
            } else {
                this.totalScore = null;
            }
        }

        private void updateStatus() {
             if (totalScore == null) {
                this.status = "-"; // 或 "未计算"
            } else if (totalScore >= 60) {
                this.status = "通过";
            } else {
                this.status = "不及格";
            }
            // 如果特定输入表明缺勤，可以添加"缺考"逻辑
        }
    }


     // --- 分数输入验证的辅助类 ---
    private static class ScoreStringConverter extends StringConverter<Double> {
        @Override
        public String toString(Double object) {
            return object == null ? "" : String.format("%.1f", object); // 格式化为一位小数
        }

        @Override
        public Double fromString(String string) {
            if (string == null || string.trim().isEmpty()) {
                return null; // 允许空输入（表示空分数）
            }
            try {
                double value = Double.parseDouble(string);
                if (value < 0 || value > 100) {
                    // 可选向用户显示错误消息
                    System.err.println("分数必须在0到100之间: " + string);
                     showErrorAlert("输入错误", "分数必须在 0 到 100 之间。");
                    // 如何处理无效输入？恢复或保留？
                    // 返回null可能是最安全的，以防止无效数据传播
                     return null; // 表示转换失败
                }
                 // 输入时也四舍五入到一位小数
                return Math.round(value * 10.0) / 10.0;
            } catch (NumberFormatException e) {
                System.err.println("分数格式无效: " + string);
                 showErrorAlert("输入错误", "请输入有效的分数数字。");
                return null; // 表示转换失败
            }
        }

         private void showErrorAlert(String title, String content) {
             Alert alert = new Alert(Alert.AlertType.ERROR);
             alert.setTitle(title);
             alert.setHeaderText(null);
             alert.setContentText(content);
             alert.showAndWait();
         }
    }

}