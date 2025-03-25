package com.work.javafx.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import com.work.javafx.model.ScoreRecord;
import com.work.javafx.util.ShowMessage;
import com.work.javafx.util.ExportUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 成绩查询控制器
 * 负责处理成绩查询界面的交互逻辑
 */
public class ScoreSearchContentController implements Initializable {

    // 查询条件控件
    @FXML private ComboBox<String> academicYearComboBox;
    @FXML private ComboBox<String> semesterComboBox;
    @FXML private Button queryButton;
    
    // 导航标签页
    @FXML private Tab currentSemesterTab;
    @FXML private Tab historicalScoresTab;
    @FXML private Tab statisticsTab;

    // 统计数据控件
    @FXML private Label avgGpaLabel;
    @FXML private Label totalCreditsLabel;
    @FXML private Label completedCoursesLabel;
    @FXML private Label rankingLabel;
    
    // 成绩统计图表
    @FXML private BarChart<String, Number> scoreDistributionChart;
    @FXML private LineChart<String, Number> gpaLineChart;
    
    // 本学期成绩图表（动态创建）
    private BarChart<String, Number> currentSemesterBarChart;
    private LineChart<String, Number> currentSemesterLineChart;
    
    // 成绩表格
    @FXML private TableView<ScoreRecord> scoreTableView;
    @FXML private TableColumn<ScoreRecord, Integer> indexColumn;
    @FXML private TableColumn<ScoreRecord, String> courseCodeColumn;
    @FXML private TableColumn<ScoreRecord, String> courseNameColumn;
    @FXML private TableColumn<ScoreRecord, Double> creditColumn;
    @FXML private TableColumn<ScoreRecord, String> courseTypeColumn;
    @FXML private TableColumn<ScoreRecord, String> teacherColumn;
    @FXML private TableColumn<ScoreRecord, Integer> scoreColumn;
    @FXML private TableColumn<ScoreRecord, Double> gpaColumn;
    @FXML private TableColumn<ScoreRecord, String> rankColumn;
    
    // 导出/打印按钮
    @FXML private Button exportButton;
    @FXML private Button printButton;

    // 历年成绩筛选控件
    @FXML private ComboBox<String> historyYearComboBox;
    @FXML private ComboBox<String> historySemesterComboBox;
    @FXML private ComboBox<String> historyCourseTypeComboBox;
    @FXML private TextField searchTextField;
    @FXML private Button clearFilterButton;
    
    // 成绩统计控件
    @FXML private PieChart creditPieChart;
    @FXML private HBox semesterSelectBox;
    @FXML private GridPane statsCardGrid;
    @FXML private VBox failedCoursesContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化下拉框
        initComboBoxes();
        
        // 初始化表格
        initTableView();
        
        // 初始化图表
        initCharts();
        
        // 初始化历年成绩筛选控件
        initHistoricalFilters();
        
        // 加载示例数据
        loadSampleData();
        
        // 强制刷新图表显示
        Platform.runLater(() -> {
            // 触发布局刷新
            if (scoreDistributionChart != null && scoreDistributionChart.getScene() != null) {
                scoreDistributionChart.layout();
                scoreDistributionChart.setVisible(true);
                System.out.println("成绩分布图表强制刷新完成");
            }
            
            if (gpaLineChart != null && gpaLineChart.getScene() != null) {
                gpaLineChart.layout();
                gpaLineChart.setVisible(true);
                System.out.println("GPA趋势图表强制刷新完成");
            }
        });
        
        System.out.println("成绩查询界面初始化成功");
    }
    
    /**
     * 初始化下拉框选项
     */
    private void initComboBoxes() {
        // 学年下拉框
        ObservableList<String> academicYears = FXCollections.observableArrayList(
                "2024-2025", "2023-2024", "2022-2023", "2021-2022"
        );
        academicYearComboBox.setItems(academicYears);
        academicYearComboBox.setValue("2024-2025");
        
        // 学期下拉框
        ObservableList<String> semesters = FXCollections.observableArrayList(
                "第一学期", "第二学期", "暑期学期"
        );
        semesterComboBox.setItems(semesters);
        semesterComboBox.setValue("第二学期");
    }
    
    /**
     * 初始化表格
     */
    private void initTableView() {
        // 设置列的单元格值工厂
        indexColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getIndex()).asObject());
        courseCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCourseCode()));
        courseNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCourseName()));
        creditColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getCredit()).asObject());
        courseTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCourseType()));
        teacherColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTeacher()));
        scoreColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getScore()).asObject());
        gpaColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getGpa()).asObject());
        rankColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRank()));
        
        // 应用样式类
        indexColumn.getStyleClass().add("index-column");
        creditColumn.getStyleClass().add("credit-column");
        scoreColumn.getStyleClass().add("score-column");
        gpaColumn.getStyleClass().add("gpa-column");
        rankColumn.getStyleClass().add("rank-column");
        
        // 设置表格自动调整大小
        scoreTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // 设置成绩列单元格样式
        scoreColumn.setCellFactory(column -> {
            return new TableCell<ScoreRecord, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item.toString());
                        
                        // 根据分数设置不同样式
                        if (item >= 90) {
                            getStyleClass().removeAll("grade-good", "grade-average", "grade-pass", "grade-fail");
                            getStyleClass().add("grade-excellent");
                        } else if (item >= 80) {
                            getStyleClass().removeAll("grade-excellent", "grade-average", "grade-pass", "grade-fail");
                            getStyleClass().add("grade-good");
                        } else if (item >= 70) {
                            getStyleClass().removeAll("grade-excellent", "grade-good", "grade-pass", "grade-fail");
                            getStyleClass().add("grade-average");
                        } else if (item >= 60) {
                            getStyleClass().removeAll("grade-excellent", "grade-good", "grade-average", "grade-fail");
                            getStyleClass().add("grade-pass");
                        } else {
                            getStyleClass().removeAll("grade-excellent", "grade-good", "grade-average", "grade-pass");
                            getStyleClass().add("grade-fail");
                        }
                    }
                }
            };
        });
        
        // 课程代码/名称列的单元格样式
        courseNameColumn.setCellFactory(column -> {
            return new TableCell<ScoreRecord, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (item == null || empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        ScoreRecord record = getTableView().getItems().get(getIndex());
                        
                        Label codeLabel = new Label(record.getCourseCode());
                        codeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
                        
                        Label nameLabel = new Label(item);
                        
                        VBox vbox = new VBox(codeLabel, nameLabel);
                        vbox.setSpacing(3);
                        
                        setGraphic(vbox);
                        setText(null);
                    }
                }
            };
        });
        
        // 确保表格所有列都已添加
        if (scoreTableView.getColumns().isEmpty()) {
            System.out.println("添加表格列到表格中");
            scoreTableView.getColumns().addAll(
                indexColumn, courseNameColumn, creditColumn, 
                courseTypeColumn, teacherColumn, scoreColumn, 
                gpaColumn, rankColumn
            );
        }
        
        // 设置表格可见性
        scoreTableView.setVisible(true);
        
        System.out.println("表格初始化完成，列数：" + scoreTableView.getColumns().size());
    }
    
    /**
     * 初始化统计图表
     */
    private void initCharts() {
        // 确保首先清空现有数据，避免重复添加
        scoreDistributionChart.getData().clear();
        gpaLineChart.getData().clear();
//
        // 成绩分布柱状图数据
        XYChart.Series<String, Number> scoreSeries = new XYChart.Series<>();
        scoreSeries.setName("课程数量");
        
        scoreSeries.getData().add(new XYChart.Data<>("90+", 2));
        scoreSeries.getData().add(new XYChart.Data<>("80-89", 3));
        scoreSeries.getData().add(new XYChart.Data<>("70-79", 1));
        scoreSeries.getData().add(new XYChart.Data<>("60-69", 0));
        scoreSeries.getData().add(new XYChart.Data<>("不及格", 0));
        
        scoreDistributionChart.getData().add(scoreSeries);
//        scoreDistributionChart.setAnimated(false);
        scoreDistributionChart.getStyleClass().add("custom-chart");
        
        // GPA趋势折线图数据
        XYChart.Series<String, Number> gpaSeries = new XYChart.Series<>();
        gpaSeries.setName("GPA");
        
        gpaSeries.getData().add(new XYChart.Data<>("大一上", 3.4));
        gpaSeries.getData().add(new XYChart.Data<>("大一下", 3.5));
        gpaSeries.getData().add(new XYChart.Data<>("大二上", 3.6));
        gpaSeries.getData().add(new XYChart.Data<>("大二下", 3.7));
        gpaSeries.getData().add(new XYChart.Data<>("大三上", 3.72));
        
        gpaLineChart.getData().add(gpaSeries);
//        gpaLineChart.setAnimated(false); // 禁用动画可能有助于解决一些渲染问题
        gpaLineChart.getStyleClass().add("custom-chart");
        
        // 直接设置可见性
        scoreDistributionChart.setVisible(true);
        gpaLineChart.setVisible(true);
        
        // 确保图表正确布局
        scoreDistributionChart.setMinHeight(200);
        gpaLineChart.setMinHeight(200);
        // 等待JavaFX完成渲染后再添加交互效果
        Platform.runLater(() -> {
            enhanceChartInteraction();
        });
    }
    
    /**
     * 增强图表交互效果
     */
    private void enhanceChartInteraction() {
        try {
            // 检查图表是否有数据
            if (scoreDistributionChart.getData() == null || scoreDistributionChart.getData().isEmpty() ||
                gpaLineChart.getData() == null || gpaLineChart.getData().isEmpty()) {
                System.out.println("图表数据为空，无法增强交互效果");
                return;
            }
            
            // 为每个系列中的每个数据点设置交互效果
            for (XYChart.Series<String, Number> series : scoreDistributionChart.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    // 使用监听器等待节点可用
                    data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            // 添加悬停效果
                            Tooltip tooltip = new Tooltip(
                                data.getXValue() + ": " + data.getYValue() + " 门课程"
                            );
                            Tooltip.install(newNode, tooltip);
                            
                            // 添加悬停样式
                            newNode.setOnMouseEntered(e -> 
                                newNode.getStyleClass().add("chart-bar-highlighted"));
                            newNode.setOnMouseExited(e -> 
                                newNode.getStyleClass().remove("chart-bar-highlighted"));
                        }
                    });
                }
            }
            
            // 为折线图的数据点设置交互效果
            for (XYChart.Series<String, Number> series : gpaLineChart.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    // 使用监听器等待节点可用
                    data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            // 添加悬停提示
                            Tooltip tooltip = new Tooltip(
                                data.getXValue() + ": GPA " + String.format("%.2f", data.getYValue())
                            );
                            Tooltip.install(newNode, tooltip);
                            
                            // 添加悬停效果
                            DropShadow highlight = new DropShadow();
                            highlight.setColor(Color.DODGERBLUE);
                            highlight.setRadius(10);
                            
                            newNode.setOnMouseEntered(e -> newNode.setEffect(highlight));
                            newNode.setOnMouseExited(e -> newNode.setEffect(null));
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("增强图表交互效果时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 加载示例成绩数据
     */
    private void loadSampleData() {
        ObservableList<ScoreRecord> scores = FXCollections.observableArrayList(
                new ScoreRecord(1, "MATH2005", "高等数学(II)", 4.0, "必修课", "李明", 92, 4.0, "5/45"),
                new ScoreRecord(2, "PHYS1003", "大学物理实验", 2.0, "必修课", "王华", 85, 3.7, "8/30"),
                new ScoreRecord(3, "COMP2013", "数据结构", 3.0, "必修课", "张伟", 94, 4.0, "3/60"),
                new ScoreRecord(4, "ENGL1002", "大学英语(II)", 3.0, "必修课", "Sarah Johnson", 88, 3.7, "6/35"),
                new ScoreRecord(5, "COMP2022", "Java程序设计", 2.5, "选修课", "刘强", 78, 3.0, "22/40"),
                new ScoreRecord(6, "ARTS1001", "艺术欣赏", 2.0, "通识选修", "陈丽", 89, 3.7, "4/25")
        );
        
        scoreTableView.setItems(scores);
        
        // 设置统计数据
        avgGpaLabel.setText("3.72");
        totalCreditsLabel.setText("16.5");
        completedCoursesLabel.setText("6");
        rankingLabel.setText("15/78");
        
        // 设置统计卡片的布局
        if (avgGpaLabel.getParent().getParent() instanceof HBox) {
            HBox summaryContainer = (HBox) avgGpaLabel.getParent().getParent();
            for (int i = 0; i < summaryContainer.getChildren().size(); i++) {
                if (summaryContainer.getChildren().get(i) instanceof VBox) {
                    VBox card = (VBox) summaryContainer.getChildren().get(i);
                    card.setMinWidth(0);
                    card.setPrefWidth(Region.USE_COMPUTED_SIZE);
                    card.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(card, Priority.ALWAYS);
                }
            }
        }
        
        // 确保图表数据被正确加载并显示
        Platform.runLater(() -> {
            // 为本学期成绩标签页创建并添加图表
            createChartsForCurrentSemester(scores);
            
            // 手动刷新当前标签页
            if (currentSemesterTab != null) {
                currentSemesterTab.getTabPane().requestLayout();
            }
            
            System.out.println("本学期成绩图表刷新完成");
        });
    }
    
    /**
     * 为本学期成绩标签页创建图表
     */
    private void createChartsForCurrentSemester(ObservableList<ScoreRecord> scores) {
        // 查找图表容器
        VBox currentSemesterContent = (VBox) currentSemesterTab.getContent();
        if (currentSemesterContent == null) return;
        
        // 查找图表容器
        HBox chartContainer = null;
        for (Node node : currentSemesterContent.getChildren()) {
            if (node instanceof VBox && ((VBox) node).getStyleClass().contains("summary-section")) {
                for (Node child : ((VBox) node).getChildren()) {
                    if (child instanceof HBox && ((HBox) child).getStyleClass().contains("chart-container")) {
                        chartContainer = (HBox) child;
                        break;
                    }
                }
                if (chartContainer != null) break;
            }
        }
        
        if (chartContainer == null) {
            System.out.println("找不到图表容器");
            return;
        }
        
        // 清空现有容器
        chartContainer.getChildren().clear();
        
        // 创建成绩分布图表
        VBox barChartContainer = new VBox();
        barChartContainer.getStyleClass().add("chart");
        HBox.setHgrow(barChartContainer, Priority.ALWAYS);
        
        Label barChartTitle = new Label("成绩分布");
        barChartTitle.getStyleClass().add("chart-title");
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("课程数量");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(5);
        yAxis.setTickUnit(1);
        
        currentSemesterBarChart = new BarChart<>(xAxis, yAxis);
        currentSemesterBarChart.setTitle("");
        currentSemesterBarChart.setLegendVisible(false);
        currentSemesterBarChart.setAnimated(false);
        currentSemesterBarChart.setMinHeight(200);
        currentSemesterBarChart.setPrefHeight(200);
        currentSemesterBarChart.getStyleClass().add("custom-chart");
        VBox.setVgrow(currentSemesterBarChart, Priority.ALWAYS);
        
        barChartContainer.getChildren().addAll(barChartTitle, currentSemesterBarChart);
        
        // 创建GPA趋势图表
        VBox lineChartContainer = new VBox();
        lineChartContainer.getStyleClass().add("chart");
        HBox.setHgrow(lineChartContainer, Priority.ALWAYS);
        
        Label lineChartTitle = new Label("GPA趋势");
        lineChartTitle.getStyleClass().add("chart-title");
        
        CategoryAxis xAxis2 = new CategoryAxis();
        NumberAxis yAxis2 = new NumberAxis();
        yAxis2.setLabel("GPA");
        yAxis2.setAutoRanging(false);
        yAxis2.setLowerBound(0);
        yAxis2.setUpperBound(4);
        yAxis2.setTickUnit(0.5);
        
        currentSemesterLineChart = new LineChart<>(xAxis2, yAxis2);
        currentSemesterLineChart.setTitle("");
        currentSemesterLineChart.setLegendVisible(false);
        currentSemesterLineChart.setAnimated(false);
        currentSemesterLineChart.setMinHeight(200);
        currentSemesterLineChart.setPrefHeight(200);
        currentSemesterLineChart.getStyleClass().add("custom-chart");
        VBox.setVgrow(currentSemesterLineChart, Priority.ALWAYS);
        
        lineChartContainer.getChildren().addAll(lineChartTitle, currentSemesterLineChart);
        
        // 添加到容器
        chartContainer.getChildren().addAll(barChartContainer, lineChartContainer);
        
        // 添加数据到图表
        updateCurrentSemesterCharts(scores);
    }
    
    /**
     * 更新本学期成绩的图表数据
     */
    private void updateCurrentSemesterCharts(ObservableList<ScoreRecord> scores) {
        if (currentSemesterBarChart == null || currentSemesterLineChart == null) return;
        
        // 清除现有数据
        currentSemesterBarChart.getData().clear();
        currentSemesterLineChart.getData().clear();
        
        // 创建成绩分布数据
        XYChart.Series<String, Number> scoreSeries = new XYChart.Series<>();
        scoreSeries.setName("课程数量");
        
        // 根据表格数据统计各分数段的课程数量
        int count90Plus = 0, count80to89 = 0, count70to79 = 0, count60to69 = 0, countFail = 0;
        
        for (ScoreRecord record : scores) {
            int score = record.getScore();
            if (score >= 90) count90Plus++;
            else if (score >= 80) count80to89++;
            else if (score >= 70) count70to79++;
            else if (score >= 60) count60to69++;
            else countFail++;
        }
        
        // 添加统计数据
        scoreSeries.getData().add(new XYChart.Data<>("90+", count90Plus));
        scoreSeries.getData().add(new XYChart.Data<>("80-89", count80to89));
        scoreSeries.getData().add(new XYChart.Data<>("70-79", count70to79));
        scoreSeries.getData().add(new XYChart.Data<>("60-69", count60to69));
        scoreSeries.getData().add(new XYChart.Data<>("不及格", countFail));
        
        // 添加到图表
        currentSemesterBarChart.getData().add(scoreSeries);
        
        // 设置GPA趋势图数据
        XYChart.Series<String, Number> gpaSeries = new XYChart.Series<>();
        gpaSeries.setName("GPA");
        
        gpaSeries.getData().add(new XYChart.Data<>("大一上", 3.4));
        gpaSeries.getData().add(new XYChart.Data<>("大一下", 3.5));
        gpaSeries.getData().add(new XYChart.Data<>("大二上", 3.6));
        gpaSeries.getData().add(new XYChart.Data<>("大二下", 3.7));
        gpaSeries.getData().add(new XYChart.Data<>("大三上", 3.72));
        
        currentSemesterLineChart.getData().add(gpaSeries);
        
        // 设置图表可见并启用交互
        currentSemesterBarChart.setVisible(true);
        currentSemesterLineChart.setVisible(true);
        
        // 添加交互效果
        addInteractionToChart(currentSemesterBarChart, currentSemesterLineChart);
    }
    
    /**
     * 为图表添加交互效果
     */
    private void addInteractionToChart(BarChart<String, Number> barChart, LineChart<String, Number> lineChart) {
        try {
            // 为柱状图的数据点添加交互效果
            for (XYChart.Series<String, Number> series : barChart.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    // 使用监听器等待节点可用
                    data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            // 添加悬停效果
                            Tooltip tooltip = new Tooltip(
                                data.getXValue() + ": " + data.getYValue() + " 门课程"
                            );
                            Tooltip.install(newNode, tooltip);
                            
                            // 添加悬停样式
                            newNode.setOnMouseEntered(e -> 
                                newNode.getStyleClass().add("chart-bar-highlighted"));
                            newNode.setOnMouseExited(e -> 
                                newNode.getStyleClass().remove("chart-bar-highlighted"));
                        }
                    });
                }
            }
            
            // 为折线图的数据点添加交互效果
            for (XYChart.Series<String, Number> series : lineChart.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    // 使用监听器等待节点可用
                    data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            // 添加悬停提示
                            Tooltip tooltip = new Tooltip(
                                data.getXValue() + ": GPA " + String.format("%.2f", data.getYValue())
                            );
                            Tooltip.install(newNode, tooltip);
                            
                            // 添加悬停效果
                            DropShadow highlight = new DropShadow();
                            highlight.setColor(Color.DODGERBLUE);
                            highlight.setRadius(10);
                            
                            newNode.setOnMouseEntered(e -> newNode.setEffect(highlight));
                            newNode.setOnMouseExited(e -> newNode.setEffect(null));
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("为图表添加交互效果时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 切换到本学期成绩标签页
     */
    @FXML
    private void showCurrentSemesterTab() {
        TabPane tabPane = currentSemesterTab.getTabPane();
        tabPane.getSelectionModel().select(currentSemesterTab);
    }



    /**
     * 按学年分组展示成绩
     */
    private void groupScoresByYear() {
        if (historyYearComboBox != null && !historyYearComboBox.getValue().equals("全部")) {
            String selectedYear = historyYearComboBox.getValue();
            
            // 应用学年筛选
            ObservableList<ScoreRecord> allScores = loadAllHistoricalScoresData();
            FilteredList<ScoreRecord> filteredData = new FilteredList<>(allScores);
            
            // 根据课程代码筛选学年
            // 例如：选择2021-2022学年，则筛选课程代码中第5位为"1"或"2"的课程
            String yearCode;
            switch (selectedYear) {
                case "2021-2022":
                    yearCode = "[12]"; // 匹配1或2
                    break;
                case "2022-2023":
                    yearCode = "[34]"; // 匹配3或4
                    break;
                case "2023-2024":
                    yearCode = "[56]"; // 匹配5或6
                    break;
                case "2024-2025":
                    yearCode = "[78]"; // 匹配7或8
                    break;
                default:
                    yearCode = "\\d"; // 匹配任意数字（不应该到达这里）
                    break;
            }
            
            filteredData.setPredicate(record -> {
                String courseCode = record.getCourseCode();
                if (courseCode.length() >= 5) {
                    String yearDigit = courseCode.substring(4, 5);
                    return yearDigit.matches(yearCode);
                }
                return false;
            });
            
            // 更新表格数据
            scoreTableView.setItems(filteredData);

        } else {
            // 显示全部成绩
            ObservableList<ScoreRecord> allScores = loadAllHistoricalScoresData();
            scoreTableView.setItems(allScores);
            

        }
    }
    
    /**
     * 切换到成绩统计标签页并加载统计数据
     */
    @FXML
    private void showStatisticsTab() {
        TabPane tabPane = statisticsTab.getTabPane();
        tabPane.getSelectionModel().select(statisticsTab);
        
        // 加载成绩统计数据
        loadStatisticsData();
    }
    
    /**
     * 加载成绩统计数据
     */
    private void loadStatisticsData() {
        // 更新图表数据
        updateCharts();
        
        // 更新学期绩点统计
        updateSemesterStats();
        
        System.out.println("已加载成绩统计数据");
    }
    
    /**
     * 更新图表数据
     */
    private void updateCharts() {
        // 清除现有数据
        scoreDistributionChart.getData().clear();
        gpaLineChart.getData().clear();
        
        // 根据学期选择不同的示例数据
        switch (semesterComboBox.getValue()) {
            case "2023-2024-1":
                // 成绩分布数据
                XYChart.Series<String, Number> scoreSeries = new XYChart.Series<>();
                scoreSeries.setName("课程数量");
                
                scoreSeries.getData().add(new XYChart.Data<>("90+", 2));
                scoreSeries.getData().add(new XYChart.Data<>("80-89", 3));
                scoreSeries.getData().add(new XYChart.Data<>("70-79", 1));
                scoreSeries.getData().add(new XYChart.Data<>("60-69", 0));
                scoreSeries.getData().add(new XYChart.Data<>("不及格", 0));
                
                scoreDistributionChart.getData().add(scoreSeries);
                scoreDistributionChart.getStyleClass().add("custom-chart");
                scoreDistributionChart.setVisible(true);
                
                // GPA趋势数据
                XYChart.Series<String, Number> gpaSeries = new XYChart.Series<>();
                gpaSeries.setName("GPA");
                
                gpaSeries.getData().add(new XYChart.Data<>("大一上", 3.4));
                gpaSeries.getData().add(new XYChart.Data<>("大一下", 3.5));
                gpaSeries.getData().add(new XYChart.Data<>("大二上", 3.6));
                gpaSeries.getData().add(new XYChart.Data<>("大二下", 3.7));
                gpaSeries.getData().add(new XYChart.Data<>("大三上", 3.72));
                
                gpaLineChart.getData().add(gpaSeries);
                gpaLineChart.getStyleClass().add("custom-chart");
                gpaLineChart.setVisible(true);
                break;
                
            case "2023-2024-2":
                // 成绩分布数据
                XYChart.Series<String, Number> scoreSeries2 = new XYChart.Series<>();
                scoreSeries2.setName("课程数量");
                
                scoreSeries2.getData().add(new XYChart.Data<>("90+", 3));
                scoreSeries2.getData().add(new XYChart.Data<>("80-89", 2));
                scoreSeries2.getData().add(new XYChart.Data<>("70-79", 2));
                scoreSeries2.getData().add(new XYChart.Data<>("60-69", 1));
                scoreSeries2.getData().add(new XYChart.Data<>("不及格", 0));
                
                scoreDistributionChart.getData().add(scoreSeries2);
                scoreDistributionChart.getStyleClass().add("custom-chart");
                scoreDistributionChart.setVisible(true);
                
                // GPA趋势数据
                XYChart.Series<String, Number> gpaSeries2 = new XYChart.Series<>();
                gpaSeries2.setName("GPA");
                
                gpaSeries2.getData().add(new XYChart.Data<>("大一上", 3.4));
                gpaSeries2.getData().add(new XYChart.Data<>("大一下", 3.5));
                gpaSeries2.getData().add(new XYChart.Data<>("大二上", 3.6));
                gpaSeries2.getData().add(new XYChart.Data<>("大二下", 3.7));
                gpaSeries2.getData().add(new XYChart.Data<>("大三上", 3.72));
                gpaSeries2.getData().add(new XYChart.Data<>("大三下", 3.75));
                
                gpaLineChart.getData().add(gpaSeries2);
                gpaLineChart.getStyleClass().add("custom-chart");
                gpaLineChart.setVisible(true);
                break;
                
            default:
                // 默认数据
                XYChart.Series<String, Number> defaultScoreSeries = new XYChart.Series<>();
                defaultScoreSeries.setName("课程数量");
                
                defaultScoreSeries.getData().add(new XYChart.Data<>("90+", 2));
                defaultScoreSeries.getData().add(new XYChart.Data<>("80-89", 3));
                defaultScoreSeries.getData().add(new XYChart.Data<>("70-79", 1));
                defaultScoreSeries.getData().add(new XYChart.Data<>("60-69", 0));
                defaultScoreSeries.getData().add(new XYChart.Data<>("不及格", 0));
                
                scoreDistributionChart.getData().add(defaultScoreSeries);
                scoreDistributionChart.getStyleClass().add("custom-chart");
                scoreDistributionChart.setVisible(true);
                
                // GPA趋势数据
                XYChart.Series<String, Number> defaultGpaSeries = new XYChart.Series<>();
                defaultGpaSeries.setName("GPA");
                
                defaultGpaSeries.getData().add(new XYChart.Data<>("大一上", 3.4));
                defaultGpaSeries.getData().add(new XYChart.Data<>("大一下", 3.5));
                defaultGpaSeries.getData().add(new XYChart.Data<>("大二上", 3.6));
                defaultGpaSeries.getData().add(new XYChart.Data<>("大二下", 3.7));
                defaultGpaSeries.getData().add(new XYChart.Data<>("大三上", 3.72));
                
                gpaLineChart.getData().add(defaultGpaSeries);
                gpaLineChart.getStyleClass().add("custom-chart");
                gpaLineChart.setVisible(true);
                break;
        }
        
        // 增强图表交互效果
        enhanceChartInteraction();
        
        // 刷新图表布局
        Platform.runLater(() -> {
            if (scoreDistributionChart != null) {
                scoreDistributionChart.layout();
            }
            if (gpaLineChart != null) {
                gpaLineChart.layout();
            }
        });
    }
    
    /**
     * 更新学期绩点统计
     */
    private void updateSemesterStats() {
        // 更新总体统计数据
        avgGpaLabel.setText("3.64");  // 所有学期的平均GPA
        totalCreditsLabel.setText("98.5");  // 所有学期的总学分
        completedCoursesLabel.setText("32");  // 所有学期已修课程
        rankingLabel.setText("12/78");  // 总体排名
    }


    /**
     * 查询成绩
     */
    @FXML
    private void queryScores() {
        String academicYear = academicYearComboBox.getValue();
        String semester = semesterComboBox.getValue();
        
        System.out.println("查询成绩: 学年=" + academicYear + ", 学期=" + semester);
        
        // 这里应该根据查询条件请求后端API获取成绩数据
        // 简单示例：重新加载示例数据
        loadSampleData();
        
        ShowMessage.showInfoMessage("查询成功", "已加载" + academicYear + semester + "的成绩");
    }
    
    /**
     * 导出成绩为Excel
     */
    @FXML
    private void exportScores() {
        System.out.println("导出成绩");
        try {
            // 获取当前表格中的数据
            ObservableList<ScoreRecord> scores = scoreTableView.getItems();
            if (scores == null || scores.isEmpty()) {
                ShowMessage.showWarningMessage("导出提示", "没有可导出的数据");
                return;
            }
            
            // 创建文件选择器
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存Excel文件");
            
            // 设置文件名
            String fileName = academicYearComboBox.getValue() + "_" + 
                             semesterComboBox.getValue() + "_成绩表.xlsx";
            fileChooser.setInitialFileName(fileName);
            
            // 添加扩展名过滤器
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Excel文件 (*.xlsx)", "*.xlsx"),
                    new FileChooser.ExtensionFilter("所有文件 (*.*)", "*.*"));
            
            // 显示保存对话框
            Stage stage = (Stage) scoreTableView.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);
            
            if (file != null) {
                // 获取当前窗口
                String title = academicYearComboBox.getValue() + " " + semesterComboBox.getValue() + " 成绩表";
                
                // 调用ExportUtils导出为Excel
                boolean success = ExportUtils.exportScoresToExcel(
                    scoreTableView,
                    title,
                    stage,
                    file
                );
                
                if (success) {
                    ShowMessage.showInfoMessage("导出成功", "成绩已成功导出到: " + file.getAbsolutePath());
                } else {
                    ShowMessage.showErrorMessage("导出失败", "导出成绩时发生错误");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ShowMessage.showErrorMessage("导出错误", "导出成绩时发生错误：" + e.getMessage());
        }
    }
    
    /**
     * 打印成绩单
     */
    @FXML
    private void printScores() {
        System.out.println("打印成绩单");
        try {
            // 使用打印工具类进行打印
            ExportUtils.printNode(
                scoreTableView,
                academicYearComboBox.getValue() + " " +
                semesterComboBox.getValue() + " 成绩单"
            );
        } catch (Exception e) {
            e.printStackTrace();
            ShowMessage.showErrorMessage("打印错误", "打印成绩单时发生错误：" + e.getMessage());
        }
    }
    
    /**
     * 初始化历年成绩筛选控件
     */
    private void initHistoricalFilters() {
        // 如果筛选控件没有在FXML中定义，在这里动态创建
        if (historyYearComboBox == null) {
            // 创建一个工具栏用于筛选
            HBox filterBox = new HBox(10);
            filterBox.setPadding(new Insets(10, 15, 10, 15));
            filterBox.setAlignment(Pos.CENTER_LEFT);
            filterBox.getStyleClass().add("filter-box");

            // 学年筛选
            Label yearLabel = new Label("学年:");
            historyYearComboBox = new ComboBox<>();
            historyYearComboBox.getItems().addAll("全部", "2024-2025", "2023-2024", "2022-2023", "2021-2022");
            historyYearComboBox.setValue("全部");
            historyYearComboBox.setPrefWidth(120);

            // 学期筛选
            Label semesterLabel = new Label("学期:");
            historySemesterComboBox = new ComboBox<>();
            historySemesterComboBox.getItems().addAll("全部", "第一学期", "第二学期", "暑期学期");
            historySemesterComboBox.setValue("全部");
            historySemesterComboBox.setPrefWidth(120);

            // 课程类型筛选
            Label courseTypeLabel = new Label("课程类型:");
            historyCourseTypeComboBox = new ComboBox<>();
            historyCourseTypeComboBox.getItems().addAll("全部", "必修课", "选修课", "通识选修");
            historyCourseTypeComboBox.setValue("全部");
            historyCourseTypeComboBox.setPrefWidth(120);

            // 搜索框
            searchTextField = new TextField();
            searchTextField.setPromptText("搜索课程名称或代码");
            searchTextField.setPrefWidth(200);

            // 清除筛选按钮
            clearFilterButton = new Button("清除筛选");
            clearFilterButton.getStyleClass().add("utility-button");

            // 添加事件监听
            historyYearComboBox.setOnAction(e -> {
                applyFilters();
                // 按学年分组展示成绩
                groupScoresByYear();
            });
            historySemesterComboBox.setOnAction(e -> applyFilters());
            historyCourseTypeComboBox.setOnAction(e -> applyFilters());
            searchTextField.textProperty().addListener((obs, old, newValue) -> applyFilters());
            clearFilterButton.setOnAction(e -> clearFilters());

            // 将控件添加到筛选框中
            filterBox.getChildren().addAll(
                yearLabel, historyYearComboBox,
                semesterLabel, historySemesterComboBox,
                courseTypeLabel, historyCourseTypeComboBox,
                searchTextField, clearFilterButton
            );



        }

        // 如果成绩统计相关控件未在FXML中定义，初始化它们
        if (semesterSelectBox == null) {
            initStatisticsControls();
        }
    }
    
    /**
     * 初始化成绩统计页面的控件
     */
    private void initStatisticsControls() {
        // 假设在FXML中有一个用于放置统计控件的容器
        Tab tab = statisticsTab;
        Node content = tab.getContent();
        
        if (content instanceof VBox) {
            VBox statsContainer = (VBox) content;
            
            // 创建学期选择区域
            semesterSelectBox = new HBox(15);
            semesterSelectBox.setPadding(new Insets(10, 20, 15, 20));
            semesterSelectBox.setAlignment(Pos.CENTER);
            semesterSelectBox.getStyleClass().add("semester-select-box");
            
            ToggleGroup semesterGroup = new ToggleGroup();
            
            // 创建各个学期的单选按钮
            String[] semesters = {"全部学期", "大一上", "大一下", "大二上", "大二下", "大三上", "大三下"};
            for (String semester : semesters) {
                RadioButton rb = new RadioButton(semester);
                rb.setToggleGroup(semesterGroup);
                rb.getStyleClass().add("semester-toggle");
                
                // 为第一个选项（全部学期）设置为选中状态
                if (semester.equals("全部学期")) {
                    rb.setSelected(true);
                }
                
                // 添加选择事件处理
                rb.setOnAction(e -> {
                    if (rb.isSelected()) {
                        updateStatisticsBySemester(semester);
                    }
                });
                
                semesterSelectBox.getChildren().add(rb);
            }
            
            // 创建统计卡片网格
            statsCardGrid = new GridPane();
            statsCardGrid.setPadding(new Insets(10, 20, 20, 20));
            statsCardGrid.setHgap(20);
            statsCardGrid.setVgap(20);
            statsCardGrid.getStyleClass().add("stats-card-grid");
            
            // 添加统计卡片
            String[][] statsCards = {
                {"总修学分", "98.5", "total-credits-card"},
                {"已修课程", "32", "completed-courses-card"},
                {"平均GPA", "3.64", "avg-gpa-card"},
                {"GPA排名", "12/78", "ranking-card"},
                {"优秀课程", "15门", "excellent-courses-card"},
                {"不及格课程", "0门", "failed-courses-card"}
            };
            
            int col = 0;
            int row = 0;
            for (String[] cardInfo : statsCards) {
                VBox card = createStatsCard(cardInfo[0], cardInfo[1], cardInfo[2]);
                statsCardGrid.add(card, col, row);
                col++;
                if (col > 2) {
                    col = 0;
                    row++;
                }
            }
            
            // 创建饼图（课程类型分布）
            creditPieChart = new PieChart();
            creditPieChart.setTitle("学分分布");
            creditPieChart.getStyleClass().add("custom-chart");
            creditPieChart.setLabelsVisible(true);
            creditPieChart.setLegendVisible(true);
            
            // 添加数据
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("必修课", 68),
                new PieChart.Data("选修课", 18),
                new PieChart.Data("通识课", 12.5)
            );
            creditPieChart.setData(pieData);
            
            // 添加悬停效果
            for (PieChart.Data data : pieData) {
                data.getNode().setOnMouseEntered(e -> {
                    data.getNode().setEffect(new Glow(0.5));
                    Tooltip.install(data.getNode(), new Tooltip(
                        data.getName() + ": " + data.getPieValue() + " 学分"
                    ));
                });
                
                data.getNode().setOnMouseExited(e -> {
                    data.getNode().setEffect(null);
                });
            }
            
            // 创建显示不及格/警告课程的容器
            failedCoursesContainer = new VBox(10);
            failedCoursesContainer.setPadding(new Insets(15, 20, 20, 20));
            failedCoursesContainer.getStyleClass().add("failed-courses-container");
            
            Label failedTitle = new Label("学业预警课程");
            failedTitle.getStyleClass().add("section-title");
            failedCoursesContainer.getChildren().add(failedTitle);
            
            // 添加无预警信息的提示
            Label noFailedLabel = new Label("当前没有学业预警课程，请继续保持良好的学习状态！");
            noFailedLabel.getStyleClass().add("no-warning-label");
            failedCoursesContainer.getChildren().add(noFailedLabel);
            
            // 将所有元素添加到统计页面容器
            statsContainer.getChildren().addAll(
                semesterSelectBox,
                new Label("学期概览"), // 这个作为小标题
                statsCardGrid,
                new HBox(20, scoreDistributionChart, creditPieChart), // 图表水平排列
                new VBox(10, gpaLineChart), // GPA趋势图
                failedCoursesContainer
            );
        }
    }
    
    /**
     * 创建统计卡片
     */
    private VBox createStatsCard(String title, String value, String styleClass) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15, 20, 15, 20));
        card.getStyleClass().addAll("summary-item", styleClass);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("summary-label");
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("summary-value");
        
        card.getChildren().addAll(titleLabel, valueLabel);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMaxHeight(Double.MAX_VALUE);
        GridPane.setHgrow(card, Priority.ALWAYS);
        GridPane.setVgrow(card, Priority.ALWAYS);
        
        return card;
    }
    
    /**
     * 应用筛选条件
     */
    private void applyFilters() {
        if (scoreTableView.getItems() == null) return;
        
        ObservableList<ScoreRecord> allScores = loadAllHistoricalScoresData();
        FilteredList<ScoreRecord> filteredData = new FilteredList<>(allScores, p -> true);
        
        // 定义过滤条件
        String year = historyYearComboBox.getValue();
        String semester = historySemesterComboBox.getValue();
        String courseType = historyCourseTypeComboBox.getValue();
        String searchText = searchTextField.getText().toLowerCase();
        
        // 学年筛选
        Predicate<ScoreRecord> yearFilter;
        if (year.equals("全部")) {
            yearFilter = p -> true;
        } else {
            String yearCode;
            switch (year) {
                case "2021-2022":
                    yearCode = "[12]"; // 匹配1或2
                    break;
                case "2022-2023":
                    yearCode = "[34]"; // 匹配3或4
                    break;
                case "2023-2024":
                    yearCode = "[56]"; // 匹配5或6
                    break;
                case "2024-2025":
                    yearCode = "[78]"; // 匹配7或8
                    break;
                default:
                    yearCode = "\\d"; // 匹配任意数字
                    break;
            }
            
            final String finalYearCode = yearCode;
            yearFilter = p -> {
                String courseCode = p.getCourseCode();
                if (courseCode.length() >= 5) {
                    String yearDigit = courseCode.substring(4, 5);
                    return yearDigit.matches(finalYearCode);
                }
                return false;
            };
        }
        
        // 学期筛选
        Predicate<ScoreRecord> semesterFilter;
        if (semester.equals("全部")) {
            semesterFilter = p -> true;
        } else {
            String semesterCode;
            switch (semester) {
                case "第一学期":
                    semesterCode = "[01]"; // 匹配0或1
                    break;
                case "第二学期":
                    semesterCode = "2"; // 匹配2
                    break;
                case "暑期学期":
                    semesterCode = "3"; // 匹配3
                    break;
                default:
                    semesterCode = "\\d"; // 匹配任意数字
                    break;
            }
            
            final String finalSemesterCode = semesterCode;
            semesterFilter = p -> {
                String courseCode = p.getCourseCode();
                if (courseCode.length() >= 6) {
                    String semesterDigit = courseCode.substring(5, 6);
                    return semesterDigit.matches(finalSemesterCode);
                }
                return false;
            };
        }
        
        // 课程类型筛选
        Predicate<ScoreRecord> typeFilter = courseType.equals("全部") ?
            p -> true : p -> p.getCourseType().equals(courseType);
        
        // 搜索筛选
        Predicate<ScoreRecord> searchFilter = searchText.isEmpty() ?
            p -> true : p -> p.getCourseName().toLowerCase().contains(searchText) || 
                            p.getCourseCode().toLowerCase().contains(searchText);
        
        // 应用所有过滤条件
        filteredData.setPredicate(yearFilter.and(semesterFilter).and(typeFilter).and(searchFilter));
        
        // 将过滤结果放入表格
        SortedList<ScoreRecord> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(scoreTableView.comparatorProperty());
        scoreTableView.setItems(sortedData);

        // 更新显示的记录数
        updateFilterStatus(filteredData.size(), allScores.size());
    }
    
    /**
     * 清除筛选条件
     */
    private void clearFilters() {
        System.out.println("清除筛选条件");
        historyYearComboBox.setValue("全部");
        historySemesterComboBox.setValue("全部");
        historyCourseTypeComboBox.setValue("全部");
        searchTextField.clear();
        
        // 重置表格数据
        ObservableList<ScoreRecord> allScores = loadAllHistoricalScoresData();
        scoreTableView.setItems(allScores);

        // 更新筛选状态
        updateFilterStatus(allScores.size(), allScores.size());
        
        // 刷新表格显示
        Platform.runLater(() -> {
            scoreTableView.refresh();
            if (scoreTableView.getItems().size() > 0) {
                scoreTableView.scrollTo(0);
            }
            System.out.println("清除筛选条件完成，当前显示记录数：" + scoreTableView.getItems().size());
        });
    }
    
    /**
     * 更新筛选状态显示
     */
    private void updateFilterStatus(int filteredCount, int totalCount) {
        // 这里可以显示筛选结果数量，例如在状态栏或标签中
        System.out.println("显示 " + filteredCount + " / " + totalCount + " 条记录");
    }
    
    /**
     * 根据选择的学期更新统计信息
     */
    private void updateStatisticsBySemester(String semester) {
        // 根据所选学期更新统计卡片和图表数据
        if (semester.equals("全部学期")) {
            // 显示所有学期的统计数据
            updateCharts(); // 使用现有方法
            updateSemesterStats(); // 使用现有方法
        } else {
            // 显示特定学期的统计数据
            updateChartsForSemester(semester);
            updateStatsForSemester(semester);
        }
    }
    
    /**
     * 更新特定学期的图表数据
     */
    private void updateChartsForSemester(String semester) {
        // 清除现有数据
        scoreDistributionChart.getData().clear();
        
        // 根据选定学期更新成绩分布数据
        XYChart.Series<String, Number> scoreSeries = new XYChart.Series<>();
        scoreSeries.setName("课程数量");
        
        // 这里应该根据实际的学期数据进行设置
        // 下面是示例数据
        switch (semester) {
            case "大一上":
                scoreSeries.getData().add(new XYChart.Data<>("90+", 1));
                scoreSeries.getData().add(new XYChart.Data<>("80-89", 2));
                scoreSeries.getData().add(new XYChart.Data<>("70-79", 1));
                scoreSeries.getData().add(new XYChart.Data<>("60-69", 1));
                scoreSeries.getData().add(new XYChart.Data<>("不及格", 0));
                break;
            case "大一下":
                scoreSeries.getData().add(new XYChart.Data<>("90+", 2));
                scoreSeries.getData().add(new XYChart.Data<>("80-89", 1));
                scoreSeries.getData().add(new XYChart.Data<>("70-79", 2));
                scoreSeries.getData().add(new XYChart.Data<>("60-69", 0));
                scoreSeries.getData().add(new XYChart.Data<>("不及格", 0));
                break;
            case "大二上":
                scoreSeries.getData().add(new XYChart.Data<>("90+", 3));
                scoreSeries.getData().add(new XYChart.Data<>("80-89", 2));
                scoreSeries.getData().add(new XYChart.Data<>("70-79", 0));
                scoreSeries.getData().add(new XYChart.Data<>("60-69", 0));
                scoreSeries.getData().add(new XYChart.Data<>("不及格", 0));
                break;
            // 其他学期...
            default:
                scoreSeries.getData().add(new XYChart.Data<>("90+", 2));
                scoreSeries.getData().add(new XYChart.Data<>("80-89", 3));
                scoreSeries.getData().add(new XYChart.Data<>("70-79", 1));
                scoreSeries.getData().add(new XYChart.Data<>("60-69", 0));
                scoreSeries.getData().add(new XYChart.Data<>("不及格", 0));
                break;
        }
        
        scoreDistributionChart.getData().add(scoreSeries);
        
        // 更新饼图数据
        creditPieChart.getData().clear();
        
        // 根据学期添加饼图数据
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        
        switch (semester) {
            case "大一上":
                pieData.add(new PieChart.Data("必修课", 14));
                pieData.add(new PieChart.Data("选修课", 2));
                pieData.add(new PieChart.Data("通识课", 2));
                break;
            case "大一下":
                pieData.add(new PieChart.Data("必修课", 12));
                pieData.add(new PieChart.Data("选修课", 4));
                pieData.add(new PieChart.Data("通识课", 2));
                break;
            case "大二上":
                pieData.add(new PieChart.Data("必修课", 15));
                pieData.add(new PieChart.Data("选修课", 4));
                pieData.add(new PieChart.Data("通识课", 0));
                break;
            // 其他学期...
            default:
                pieData.add(new PieChart.Data("必修课", 13));
                pieData.add(new PieChart.Data("选修课", 3));
                pieData.add(new PieChart.Data("通识课", 2));
                break;
        }
        
        creditPieChart.setData(pieData);
        
        // 重新添加交互效果
        for (PieChart.Data data : pieData) {
            data.getNode().setOnMouseEntered(e -> {
                data.getNode().setEffect(new Glow(0.5));
                Tooltip.install(data.getNode(), new Tooltip(
                    data.getName() + ": " + data.getPieValue() + " 学分"
                ));
            });
            
            data.getNode().setOnMouseExited(e -> {
                data.getNode().setEffect(null);
            });
        }
    }
    
    /**
     * 更新特定学期的统计卡片数据
     */
    private void updateStatsForSemester(String semester) {
        // 根据学期更新统计卡片数据
        // 这里是示例数据，实际应用中应该根据真实数据更新
        switch (semester) {
            case "大一上":
                updateStatCard(0, "18.0");  // 总修学分
                updateStatCard(1, "6");     // 已修课程
                updateStatCard(2, "3.4");   // 平均GPA
                updateStatCard(3, "20/78"); // GPA排名
                updateStatCard(4, "3门");   // 优秀课程
                updateStatCard(5, "0门");   // 不及格课程
                break;
            case "大一下":
                updateStatCard(0, "18.0");
                updateStatCard(1, "6");
                updateStatCard(2, "3.5");
                updateStatCard(3, "18/78");
                updateStatCard(4, "3门");
                updateStatCard(5, "0门");
                break;
            case "大二上":
                updateStatCard(0, "19.0");
                updateStatCard(1, "6");
                updateStatCard(2, "3.6");
                updateStatCard(3, "15/78");
                updateStatCard(4, "3门");
                updateStatCard(5, "0门");
                break;
            // 其他学期...
            default:
                // 默认显示全部学期的统计数据
                updateStatCard(0, "98.5");
                updateStatCard(1, "32");
                updateStatCard(2, "3.64");
                updateStatCard(3, "12/78");
                updateStatCard(4, "15门");
                updateStatCard(5, "0门");
                break;
        }
    }
    
    /**
     * 更新统计卡片的值
     */
    private void updateStatCard(int index, String value) {
        if (statsCardGrid != null && index >= 0 && index < 6) {
            Node cardNode = getNodeFromGridPane(statsCardGrid, index % 3, index / 3);
            if (cardNode instanceof VBox) {
                VBox card = (VBox) cardNode;
                ObservableList<Node> children = card.getChildren();
                if (children.size() >= 2 && children.get(1) instanceof Label) {
                    Label valueLabel = (Label) children.get(1);
                    valueLabel.setText(value);
                }
            }
        }
    }
    
    /**
     * 从GridPane获取特定位置的节点
     */
    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * 加载所有历年成绩数据
     */
    private ObservableList<ScoreRecord> loadAllHistoricalScoresData() {
        // 创建一个包含更多历年成绩数据的示例集合
        ObservableList<ScoreRecord> historicalScores = FXCollections.observableArrayList();

        // 2021-2022学年第一学期（大一上）
        historicalScores.addAll(
            new ScoreRecord(1, "MATH2101", "高等数学(I)", 4.0, "必修课", "李明", 89, 3.7, "8/48"),
            new ScoreRecord(2, "PHYS2101", "大学物理(I)", 3.0, "必修课", "王华", 92, 4.0, "5/45"),
            new ScoreRecord(3, "COMP2101", "计算机导论", 2.0, "必修课", "张伟", 88, 3.7, "7/62"),
            new ScoreRecord(4, "ENGL2101", "大学英语(I)", 3.0, "必修课", "Sarah Johnson", 85, 3.3, "12/38"),
            new ScoreRecord(5, "CHEM2101", "普通化学", 3.0, "必修课", "赵刚", 78, 3.0, "20/50"),
            new ScoreRecord(6, "MATH2102", "线性代数", 3.0, "必修课", "周红", 91, 4.0, "3/55")
        );

        // 2021-2022学年第二学期（大一下）
        historicalScores.addAll(
            new ScoreRecord(7, "MATH2201", "高等数学(II)", 4.0, "必修课", "李明", 87, 3.7, "10/47"),
            new ScoreRecord(8, "PHYS2201", "大学物理(II)", 3.0, "必修课", "王华", 84, 3.3, "15/46"),
            new ScoreRecord(9, "COMP2201", "程序设计基础", 3.0, "必修课", "刘强", 94, 4.0, "2/60"),
            new ScoreRecord(10, "ENGL2201", "大学英语(II)", 3.0, "必修课", "Sarah Johnson", 86, 3.7, "11/39"),
            new ScoreRecord(11, "ECON2201", "经济学原理", 2.0, "通识选修", "黄明", 82, 3.3, "8/35"),
            new ScoreRecord(12, "HIST2201", "中国近代史", 2.0, "通识选修", "王历", 88, 3.7, "5/30")
        );

        // 2022-2023学年第一学期（大二上）
        historicalScores.addAll(
            new ScoreRecord(13, "MATH2301", "概率论与数理统计", 3.0, "必修课", "郑明", 90, 4.0, "6/52"),
            new ScoreRecord(14, "COMP2301", "数据结构", 4.0, "必修课", "张伟", 93, 4.0, "4/58"),
            new ScoreRecord(15, "COMP2302", "离散数学", 3.0, "必修课", "刘强", 85, 3.7, "12/56"),
            new ScoreRecord(16, "COMP2303", "数字电路", 3.0, "必修课", "王电", 88, 3.7, "8/54"),
            new ScoreRecord(17, "LANG2301", "第二外语(日语)", 2.0, "选修课", "田中", 91, 4.0, "3/25"),
            new ScoreRecord(18, "ARTS2301", "音乐欣赏", 2.0, "通识选修", "陈艺", 95, 4.0, "1/28")
        );

        // 2022-2023学年第二学期（大二下）
        historicalScores.addAll(
            new ScoreRecord(19, "COMP2401", "计算机组成原理", 4.0, "必修课", "李华", 87, 3.7, "9/50"),
            new ScoreRecord(20, "COMP2402", "操作系统", 3.5, "必修课", "王强", 89, 3.7, "7/55"),
            new ScoreRecord(21, "COMP2403", "数据库原理", 3.0, "必修课", "张数", 91, 4.0, "5/52"),
            new ScoreRecord(22, "COMP2404", "算法设计与分析", 3.0, "必修课", "刘算", 92, 4.0, "3/48"),
            new ScoreRecord(23, "BUSI2401", "管理学基础", 2.0, "选修课", "陈管", 86, 3.7, "7/32"),
            new ScoreRecord(24, "PSYC2401", "心理学导论", 2.0, "通识选修", "林心", 93, 4.0, "2/30")
        );

        // 2023-2024学年第一学期（大三上）
        historicalScores.addAll(
            new ScoreRecord(25, "COMP2501", "计算机网络", 3.5, "必修课", "王网", 90, 4.0, "6/54"),
            new ScoreRecord(26, "COMP2502", "软件工程", 3.0, "必修课", "张软", 88, 3.7, "8/50"),
            new ScoreRecord(27, "COMP2503", "人工智能导论", 3.0, "必修课", "李智", 94, 4.0, "2/48"),
            new ScoreRecord(28, "COMP2504", "Java程序设计", 3.0, "必修课", "刘程", 91, 4.0, "4/52"),
            new ScoreRecord(29, "COMP2505", "大数据技术", 2.5, "选修课", "钱大", 87, 3.7, "9/35"),
            new ScoreRecord(30, "PHIL2501", "逻辑学", 2.0, "通识选修", "何逻", 85, 3.3, "10/28")
        );

        // 2023-2024学年第二学期（大三下，当前学期）
        historicalScores.addAll(
            new ScoreRecord(31, "COMP2601", "软件项目管理", 3.0, "必修课", "王管", 88, 3.7, "7/45"),
            new ScoreRecord(32, "COMP2602", "Web开发技术", 3.0, "必修课", "李网", 92, 4.0, "4/50"),
            new ScoreRecord(33, "COMP2603", "移动应用开发", 3.0, "必修课", "张移", 90, 4.0, "5/48"),
            new ScoreRecord(34, "COMP2604", "信息安全", 3.0, "必修课", "刘安", 85, 3.3, "12/45"),
            new ScoreRecord(35, "COMP2605", "云计算技术", 2.5, "选修课", "周云", 89, 3.7, "6/32"),
            new ScoreRecord(36, "MATH2601", "运筹学", 2.0, "选修课", "陈运", 84, 3.3, "13/30")
        );

        return historicalScores;
    }
} 