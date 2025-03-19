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
    @FXML private Tab warningTab;
    
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
     * 切换到历年成绩标签页并加载历年成绩数据
     */
    @FXML
    private void showHistoricalScoresTab() {
        TabPane tabPane = historicalScoresTab.getTabPane();
        tabPane.getSelectionModel().select(historicalScoresTab);
        
        // 加载历年成绩数据
        loadHistoricalScoresData();
    }
    
    /**
     * 加载历年成绩数据
     */
    private void loadHistoricalScoresData() {
        // 这里可以根据需要加载不同的历年成绩数据
        // 本示例仅展示UI效果，使用与当前学期相同的数据结构
        
        ObservableList<ScoreRecord> historicalScores = FXCollections.observableArrayList(
                new ScoreRecord(1, "MATH1001", "高等数学(I)", 4.0, "必修课", "李明", 90, 4.0, "7/48"),
                new ScoreRecord(2, "PHYS1001", "大学物理(I)", 3.0, "必修课", "王华", 87, 3.7, "10/45"),
                new ScoreRecord(3, "COMP1001", "计算机导论", 2.0, "必修课", "张伟", 92, 4.0, "5/62"),
                new ScoreRecord(4, "ENGL1001", "大学英语(I)", 3.0, "必修课", "Sarah Johnson", 86, 3.7, "12/38"),
                new ScoreRecord(5, "CHEM1001", "普通化学", 3.0, "必修课", "赵刚", 84, 3.3, "15/50"),
                new ScoreRecord(6, "MATH1002", "线性代数", 3.0, "必修课", "周红", 88, 3.7, "9/55"),
                new ScoreRecord(7, "PHYS1002", "大学物理(II)", 3.0, "必修课", "王华", 82, 3.3, "18/45"),
                new ScoreRecord(8, "COMP1002", "程序设计基础", 3.0, "必修课", "刘强", 93, 4.0, "3/60"),
                new ScoreRecord(9, "ENGL1002", "大学英语(II)", 3.0, "必修课", "Sarah Johnson", 85, 3.7, "14/38"),
                new ScoreRecord(10, "MATH2001", "概率论", 3.0, "必修课", "郑明", 89, 3.7, "8/52"),
                new ScoreRecord(11, "COMP2001", "数据结构", 4.0, "必修课", "张伟", 91, 4.0, "6/58"),
                new ScoreRecord(12, "ECON1001", "微观经济学", 2.0, "选修课", "黄明", 87, 3.7, "5/25")
        );
        
        // 这里我们假设有一个专门用于显示历年成绩的表格
        // 如果UI中没有单独的表格，可以在切换标签页时更新原有表格的数据
        scoreTableView.setItems(historicalScores);
        
        System.out.println("已加载历年成绩数据");
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
     * 切换到学业预警标签页
     */
    @FXML
    private void showWarningTab() {
        TabPane tabPane = warningTab.getTabPane();
        tabPane.getSelectionModel().select(warningTab);
        
        // 加载学业预警数据
        loadWarningData();
    }
    
    /**
     * 加载学业预警数据
     */
    private void loadWarningData() {
        // 初始化学业预警内容区域
        if (warningTab.getContent() instanceof VBox) {
            VBox warningContainer = (VBox) warningTab.getContent();
            
            // 确保容器是空的，避免重复添加内容
            if (warningContainer.getChildren().isEmpty()) {
                // 添加标题
                Label titleLabel = new Label("学业预警信息");
                titleLabel.getStyleClass().add("section-title");
                
                // 添加学业预警统计卡片
                HBox statsBox = new HBox(20);
                statsBox.setPadding(new Insets(15));
                statsBox.getStyleClass().add("warning-stats-box");
                
                VBox warningCountCard = createStatsCard("本学期预警课程", "0", "warning-count-card");
                VBox lowGpaCard = createStatsCard("低于2.0课程", "0", "low-gpa-card");
                VBox failedCard = createStatsCard("累计不及格课程", "0", "failed-count-card");
                
                statsBox.getChildren().addAll(warningCountCard, lowGpaCard, failedCard);
                HBox.setHgrow(warningCountCard, Priority.ALWAYS);
                HBox.setHgrow(lowGpaCard, Priority.ALWAYS);
                HBox.setHgrow(failedCard, Priority.ALWAYS);
                
                // 添加无预警信息提示
                VBox noWarningBox = new VBox();
                noWarningBox.setAlignment(Pos.CENTER);
                noWarningBox.setPadding(new Insets(50, 20, 50, 20));
                noWarningBox.getStyleClass().add("no-warning-box");
                
                Label noWarningLabel = new Label("当前没有学业预警信息");
                noWarningLabel.getStyleClass().add("no-warning-text");
                
                Label tipLabel = new Label("请继续保持良好的学习状态!");
                tipLabel.getStyleClass().add("tip-text");
                
                noWarningBox.getChildren().addAll(noWarningLabel, tipLabel);
                
                // 将所有元素添加到容器
                warningContainer.getChildren().addAll(titleLabel, statsBox, noWarningBox);
            }
        }
        
        System.out.println("已加载学业预警数据");
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
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel文件 (*.xlsx)", "*.xlsx"));
            
            // 显示保存对话框
            Stage stage = (Stage) scoreTableView.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);
            
            if (file != null) {
                // 创建一个简单的CSV文件作为替代解决方案
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    // 写入表头
                    writer.println("序号,课程代码,课程名称,学分,课程性质,任课教师,成绩,绩点,排名");
                    
                    // 写入数据
                    for (ScoreRecord record : scores) {
                        writer.println(String.format("%d,%s,%s,%.1f,%s,%s,%d,%.1f,%s",
                                record.getIndex(),
                                record.getCourseCode(),
                                record.getCourseName(),
                                record.getCredit(),
                                record.getCourseType(),
                                record.getTeacher(),
                                record.getScore(),
                                record.getGpa(),
                                record.getRank()));
                    }
                    ShowMessage.showInfoMessage("导出成功", "成绩已成功导出到: " + file.getAbsolutePath());
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
            historyYearComboBox.setOnAction(e -> applyFilters());
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
            
            // 假设我们有一个存放历年成绩的容器，将筛选框添加到该容器
            // 注意：这需要在FXML中有对应的容器，或者通过代码找到合适的位置添加
            TabPane tabPane = historicalScoresTab.getTabPane();
            Tab tab = historicalScoresTab;
            
            // 获取标签页的内容
            Node content = tab.getContent();
            if (content instanceof VBox) {
                VBox vbox = (VBox) content;
                // 在表格前添加筛选框
                vbox.getChildren().add(0, filterBox);
            }
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
        
        Predicate<ScoreRecord> yearFilter = year.equals("全部") ? 
            p -> true : p -> p.getCourseCode().startsWith(year.substring(2, 4));
        
        Predicate<ScoreRecord> semesterFilter = semester.equals("全部") ?
            p -> true : p -> p.getCourseCode().contains(semester.equals("第一学期") ? "1" : 
                                                      semester.equals("第二学期") ? "2" : "3");
        
        Predicate<ScoreRecord> typeFilter = courseType.equals("全部") ?
            p -> true : p -> p.getCourseType().equals(courseType);
        
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
        historyYearComboBox.setValue("全部");
        historySemesterComboBox.setValue("全部");
        historyCourseTypeComboBox.setValue("全部");
        searchTextField.clear();
        
        // 重置表格数据
        ObservableList<ScoreRecord> allScores = loadAllHistoricalScoresData();
        scoreTableView.setItems(allScores);
        
        updateFilterStatus(allScores.size(), allScores.size());
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
        // 返回所有历年成绩数据，实际应用中应该从数据库或API获取
        return FXCollections.observableArrayList(
                new ScoreRecord(1, "MATH1001", "高等数学(I)", 4.0, "必修课", "李明", 90, 4.0, "7/48"),
                new ScoreRecord(2, "PHYS1001", "大学物理(I)", 3.0, "必修课", "王华", 87, 3.7, "10/45"),
                new ScoreRecord(3, "COMP1001", "计算机导论", 2.0, "必修课", "张伟", 92, 4.0, "5/62"),
                new ScoreRecord(4, "ENGL1001", "大学英语(I)", 3.0, "必修课", "Sarah Johnson", 86, 3.7, "12/38"),
                new ScoreRecord(5, "CHEM1001", "普通化学", 3.0, "必修课", "赵刚", 84, 3.3, "15/50"),
                new ScoreRecord(6, "MATH1002", "线性代数", 3.0, "必修课", "周红", 88, 3.7, "9/55"),
                new ScoreRecord(7, "PHYS1002", "大学物理(II)", 3.0, "必修课", "王华", 82, 3.3, "18/45"),
                new ScoreRecord(8, "COMP1002", "程序设计基础", 3.0, "必修课", "刘强", 93, 4.0, "3/60"),
                new ScoreRecord(9, "ENGL1002", "大学英语(II)", 3.0, "必修课", "Sarah Johnson", 85, 3.7, "14/38"),
                new ScoreRecord(10, "MATH2001", "概率论", 3.0, "必修课", "郑明", 89, 3.7, "8/52"),
                new ScoreRecord(11, "COMP2001", "数据结构", 4.0, "必修课", "张伟", 91, 4.0, "6/58"),
                new ScoreRecord(12, "ECON1001", "微观经济学", 2.0, "选修课", "黄明", 87, 3.7, "5/25"),
                new ScoreRecord(13, "MATH2005", "高等数学(II)", 4.0, "必修课", "李明", 92, 4.0, "5/45"),
                new ScoreRecord(14, "PHYS1003", "大学物理实验", 2.0, "必修课", "王华", 85, 3.7, "8/30"),
                new ScoreRecord(15, "COMP2013", "数据结构", 3.0, "必修课", "张伟", 94, 4.0, "3/60"),
                new ScoreRecord(16, "ENGL1002", "大学英语(II)", 3.0, "必修课", "Sarah Johnson", 88, 3.7, "6/35"),
                new ScoreRecord(17, "COMP2022", "Java程序设计", 2.5, "选修课", "刘强", 78, 3.0, "22/40"),
                new ScoreRecord(18, "ARTS1001", "艺术欣赏", 2.0, "通识选修", "陈丽", 89, 3.7, "4/25")
        );
    }
} 