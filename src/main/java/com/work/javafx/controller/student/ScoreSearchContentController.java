package com.work.javafx.controller.student;

import com.work.javafx.entity.Data;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.work.javafx.model.ScoreRecord;
import com.work.javafx.util.ShowMessage;
import com.work.javafx.util.ExportUtils;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.NetworkUtils.Callback;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.HashMap;
import java.util.Map;

/**
 * 成绩查询控制器
 * 负责处理成绩查询界面的交互逻辑
 */
public class ScoreSearchContentController implements Initializable {

    // 查询条件控件
    @FXML private ComboBox<String> academicYearComboBox;
    @FXML private ComboBox<String> weekCombox;
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
    @FXML private TableColumn<ScoreRecord, Integer> regularScoreColumn; // 平时成绩
    @FXML private TableColumn<ScoreRecord, Integer> finalScoreColumn;   // 期末成绩
    @FXML private TableColumn<ScoreRecord, Integer> scoreColumn;        // 总评成绩
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
    
    // 当前学期成绩数据
    private ObservableList<ScoreRecord> currentSemesterScores = FXCollections.observableArrayList();
    
    // 是否正在加载数据
    private boolean isLoading = false;
    
    // JSON解析器
    private Gson gson = new Gson();
    
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
        
        // 在initialize方法中添加标签页选择事件处理，触发历史成绩获取
        setupTabSelectionHandlers();
    }
    
    /**
     * 初始化下拉框选项
     */
    private void initComboBoxes() {
        // 学年下拉框
        ObservableList<String> academicYears = Data.getInstance().getSemesterList();
        academicYearComboBox.setItems(academicYears);
        academicYearComboBox.setValue("2024-2025-1");
        
        // 确保页面加载完成后自动查询数据
        Platform.runLater(() -> {
            queryScores();
        });
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
        regularScoreColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getRegularScore()).asObject());
        finalScoreColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getFinalScore()).asObject());
        scoreColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getScore()).asObject());
        gpaColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getGpa()).asObject());
        rankColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRank()));
        
        // 应用样式类
        indexColumn.getStyleClass().add("index-column");
        creditColumn.getStyleClass().add("credit-column");
        regularScoreColumn.getStyleClass().add("score-column");
        finalScoreColumn.getStyleClass().add("score-column");
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
                courseTypeColumn, teacherColumn, regularScoreColumn,
                finalScoreColumn, scoreColumn, gpaColumn, rankColumn
            );
        }
        
        // 设置表格可见性
        scoreTableView.setVisible(true);
        
        System.out.println("表格初始化完成，列数：" + scoreTableView.getColumns().size());
    }
    
    /**
     * 查询成绩
     */
    @FXML
    private void queryScores() {
        if (isLoading) {
            return;
        }
        
        String term = academicYearComboBox.getValue();
        System.out.println("查询成绩: term=" + term);
        
        // 显示加载提示
        isLoading = true;
        queryButton.setDisable(true);
        queryButton.setText("加载中...");
        
        // 创建参数
        Map<String, String> params = new HashMap<>();
        params.put("term", term);
        
        // 调用API获取成绩数据
        NetworkUtils.get("/grade/getGrade", params, new Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    System.out.println("API返回结果: " + result);
                    // 解析返回的JSON数据
                    List<ScoreRecord> grades = parseGradeData(result);
                    
                    // 更新表格数据
                    currentSemesterScores.clear();
                    currentSemesterScores.addAll(grades);
                    scoreTableView.setItems(currentSemesterScores);
                    
                    // 更新统计信息
                    updateStatistics(grades);
                    
                    // 更新图表
                    updateCharts(grades);
                    
                    // 重置按钮状态
                    Platform.runLater(() -> {
                        isLoading = false;
                        queryButton.setDisable(false);
                        queryButton.setText("查询");
                        ShowMessage.showInfoMessage("查询成功", "已加载" + term + "的成绩");
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        isLoading = false;
                        queryButton.setDisable(false);
                        queryButton.setText("查询");
                        ShowMessage.showErrorMessage("解析数据失败", "无法解析服务器返回的数据: " + e.getMessage());
                        e.printStackTrace();
                    });
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    isLoading = false;
                    queryButton.setDisable(false);
                    queryButton.setText("查询");
                    ShowMessage.showErrorMessage("查询失败", "无法从服务器获取成绩数据: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }
    
    /**
     * 解析成绩数据
     * @param jsonResponse API返回的JSON字符串
     * @return 成绩记录列表
     */
    private List<ScoreRecord> parseGradeData(String jsonResponse) {
        List<ScoreRecord> scoreRecords = new ArrayList<>();
        
        try {
            // 解析JSON响应
            JsonObject responseJson = gson.fromJson(jsonResponse, JsonObject.class);
            
            // 检查响应码
            if (!responseJson.has("code")) {
                System.err.println("API响应中没有code字段");
                return scoreRecords;
            }
            
            int code = responseJson.get("code").getAsInt();
            
            if (code != 200) {
                String msg = responseJson.has("msg") ? responseJson.get("msg").getAsString() : "未知错误";
                System.err.println("API返回错误: " + msg);
                return scoreRecords;
            }
            
            // 检查data字段是否存在
            if (!responseJson.has("data")) {
                System.err.println("API响应中没有data字段");
                return scoreRecords;
            }
            
            JsonElement dataElement = responseJson.get("data");
            
            // 检查data是否为null或非数组
            if (dataElement == null || dataElement.isJsonNull()) {
                System.err.println("API响应中data字段为null");
                return scoreRecords;
            }
            
            if (!dataElement.isJsonArray()) {
                System.err.println("API响应中data字段不是数组");
                return scoreRecords;
            }
            
            JsonArray dataArray = dataElement.getAsJsonArray();
            
            // 遍历成绩数据
            for (int i = 0; i < dataArray.size(); i++) {
                JsonElement gradeElement = dataArray.get(i);
                
                if (gradeElement == null || !gradeElement.isJsonObject()) {
                    continue;
                }
                
                JsonObject gradeObject = gradeElement.getAsJsonObject();
                
                try {
                    // 安全获取各字段值
                    int id = getIntFromJson(gradeObject, "id", 0);
                    int courseId = getIntFromJson(gradeObject, "courseId", 0);
                    int grade = getIntFromJson(gradeObject, "grade", 0);
                    int rank = getIntFromJson(gradeObject, "rank", 0);
                    int classNum = getIntFromJson(gradeObject, "classNum", 1); // 至少为1，避免除以0
                    double point = getDoubleFromJson(gradeObject, "point", 0.0);
                    String type = getStringFromJson(gradeObject, "type", "未知");
                    int teacherId = getIntFromJson(gradeObject, "teacherId", 0);
                    int regular = getIntFromJson(gradeObject, "regular", 0);  // 平时成绩
                    int finalScore = getIntFromJson(gradeObject, "finalScore", 0);  // 期末成绩
                    
                    // 如果classNum为0，设置为1，避免排名显示问题
                    if (classNum <= 0) classNum = 1;
                    
                    // 如果API返回了teacher字段，优先使用API返回的teacher
                    String teacherName;
                    if (gradeObject.has("teacher") && !gradeObject.get("teacher").isJsonNull()) {
                        teacherName = gradeObject.get("teacher").getAsString();
                    } else {
                        teacherName = "教师 " + teacherId;
                    }
                    
                    // 构建成绩记录对象，包含平时成绩和期末成绩
                    ScoreRecord record = new ScoreRecord(
                        i + 1,                          // 序号
                        "COURSE" + courseId,            // 课程代码
                        getCourseNameById(courseId),    // 课程名称
                        getCreditsById(courseId),       // 学分
                        type,                           // 课程类型
                        teacherName,                    // 任课教师
                        grade,                          // 总评成绩
                        point,                          // 绩点
                        rank + "/" + classNum,          // 排名
                        regular,                        // 平时成绩
                        finalScore                      // 期末成绩
                    );
                    
                    scoreRecords.add(record);
                } catch (Exception e) {
                    System.err.println("解析成绩项时出错: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("解析成绩数据时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return scoreRecords;
    }
    
    /**
     * 从JSON对象中安全获取整数值
     */
    private int getIntFromJson(JsonObject jsonObject, String key, int defaultValue) {
        if (jsonObject == null || !jsonObject.has(key)) {
            return defaultValue;
        }
        
        JsonElement element = jsonObject.get(key);
        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }
        
        try {
            return element.getAsInt();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * 从JSON对象中安全获取浮点数值
     */
    private double getDoubleFromJson(JsonObject jsonObject, String key, double defaultValue) {
        if (jsonObject == null || !jsonObject.has(key)) {
            return defaultValue;
        }
        
        JsonElement element = jsonObject.get(key);
        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }
        
        try {
            return element.getAsDouble();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * 从JSON对象中安全获取字符串值
     */
    private String getStringFromJson(JsonObject jsonObject, String key, String defaultValue) {
        if (jsonObject == null || !jsonObject.has(key)) {
            return defaultValue;
        }
        
        JsonElement element = jsonObject.get(key);
        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }
        
        try {
            return element.getAsString();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * 根据课程ID获取课程名称
     */
    private String getCourseNameById(int courseId) {
        // 直接返回课程ID，不再使用模拟数据
        return "课程 " + courseId;
    }
    
    /**
     * 根据课程ID获取学分
     */
    private double getCreditsById(int courseId) {
        // 默认返回2.0学分，不再使用模拟数据
        return 2.0;
    }
    
    /**
     * 根据教师ID获取教师姓名
     */
    private String getTeacherNameById(int teacherId) {
        // 直接返回教师ID，不再使用模拟数据
        return "教师 " + teacherId;
    }
    
    /**
     * 更新统计信息
     */
    private void updateStatistics(List<ScoreRecord> grades) {
        if (grades == null || grades.isEmpty()) {
            avgGpaLabel.setText("0.00");
            totalCreditsLabel.setText("0.0");
            completedCoursesLabel.setText("0");
            rankingLabel.setText("--/--");
            return;
        }
        
        int courseCount = grades.size();
        double totalCredits = 0;
        double totalGpaPoints = 0;
        
        for (ScoreRecord record : grades) {
            double credit = record.getCredit();
            double gpa = record.getGpa();
            
            totalCredits += credit;
            totalGpaPoints += credit * gpa;
        }
        
        // 计算平均GPA（加权平均）
        double avgGpa = totalCredits > 0 ? totalGpaPoints / totalCredits : 0;
        
        // 更新UI
        avgGpaLabel.setText(String.format("%.2f", avgGpa));
        totalCreditsLabel.setText(String.format("%.1f", totalCredits));
        completedCoursesLabel.setText(String.valueOf(courseCount));
        
        // 简单处理排名，实际应从API获取
        rankingLabel.setText("--/--");
    }
    
    /**
     * 更新图表
     */
    private void updateCharts(List<ScoreRecord> grades) {
        // 更新成绩分布图表
        updateScoreDistributionChart(grades);
        
        // 为本学期成绩标签页创建并添加图表
        createChartsForCurrentSemester(FXCollections.observableArrayList(grades));
    }
    
    /**
     * 更新成绩分布图表
     */
    private void updateScoreDistributionChart(List<ScoreRecord> grades) {
        if (scoreDistributionChart == null) return;
        
        // 清除现有数据
        scoreDistributionChart.getData().clear();
        
        // 统计各分数段的课程数量
        int count90Plus = 0, count80to89 = 0, count70to79 = 0, count60to69 = 0, countFail = 0;
        
        for (ScoreRecord record : grades) {
            int score = record.getScore();
            if (score >= 90) count90Plus++;
            else if (score >= 80) count80to89++;
            else if (score >= 70) count70to79++;
            else if (score >= 60) count60to69++;
            else countFail++;
        }
        
        // 创建新的数据系列
        XYChart.Series<String, Number> scoreSeries = new XYChart.Series<>();
        scoreSeries.setName("课程数量");
        
        scoreSeries.getData().add(new XYChart.Data<>("90+", count90Plus));
        scoreSeries.getData().add(new XYChart.Data<>("80-89", count80to89));
        scoreSeries.getData().add(new XYChart.Data<>("70-79", count70to79));
        scoreSeries.getData().add(new XYChart.Data<>("60-69", count60to69));
        scoreSeries.getData().add(new XYChart.Data<>("不及格", countFail));
        
        // 添加到图表
        scoreDistributionChart.getData().add(scoreSeries);
        
        // 增强图表交互效果
        enhanceChartInteraction();
    }
    
    /**
     * 获取所有需要查询的学期列表
     */
    private List<String> getAllTerms() {
        List<String> terms = new ArrayList<>();
        terms.addAll(Data.getInstance().getSemesterList());
        return terms;
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
            String fileName = academicYearComboBox.getValue() + "成绩表.xlsx";
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
                String title = academicYearComboBox.getValue() + " 成绩表";
                
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
                academicYearComboBox.getValue() + "成绩单"
            );
        } catch (Exception e) {
            e.printStackTrace();
            ShowMessage.showErrorMessage("打印错误", "打印成绩单时发生错误：" + e.getMessage());
        }
    }
    
    // ... 现有的其他方法保持不变 ... 
    
    // 在initialize方法中添加标签页选择事件处理，触发历史成绩获取
    @FXML
    private void setupTabSelectionHandlers() {
        if (historicalScoresTab != null) {
            historicalScoresTab.setOnSelectionChanged(event -> {
                if (historicalScoresTab.isSelected()) {
                    // 开始获取历年成绩
                    fetchHistoricalGrades();
                }
            });
        }
        
        if (statisticsTab != null) {
            statisticsTab.setOnSelectionChanged(event -> {
                if (statisticsTab.isSelected()) {
                    // 加载统计数据
                    loadStatisticsData();
                }
            });
        }
    }
    
    /**
     * 初始化统计图表
     */
    private void initCharts() {
        // 确保首先清空现有数据，避免重复添加
        scoreDistributionChart.getData().clear();
        gpaLineChart.getData().clear();

        // 成绩分布柱状图数据
        XYChart.Series<String, Number> scoreSeries = new XYChart.Series<>();
        scoreSeries.setName("课程数量");
        
        scoreSeries.getData().add(new XYChart.Data<>("90+", 0));
        scoreSeries.getData().add(new XYChart.Data<>("80-89", 0));
        scoreSeries.getData().add(new XYChart.Data<>("70-79", 0));
        scoreSeries.getData().add(new XYChart.Data<>("60-69", 0));
        scoreSeries.getData().add(new XYChart.Data<>("不及格", 0));
        
        scoreDistributionChart.getData().add(scoreSeries);
        scoreDistributionChart.getStyleClass().add("custom-chart");
        
        // GPA趋势折线图数据
        XYChart.Series<String, Number> gpaSeries = new XYChart.Series<>();
        gpaSeries.setName("GPA");
        
        gpaLineChart.getData().add(gpaSeries);
        gpaLineChart.getStyleClass().add("custom-chart");
        
        // 直接设置可见性
        scoreDistributionChart.setVisible(true);
        gpaLineChart.setVisible(true);
        
        // 确保图表正确布局
        scoreDistributionChart.setMinHeight(200);
        gpaLineChart.setMinHeight(200);
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
     * 初始化历年成绩筛选控件
     */
    private void initHistoricalFilters() {
        // 简化版的初始化历史成绩筛选控件
        if (historicalScoresTab == null) return;
        
        // 实际项目中，这里会初始化筛选控件
        System.out.println("初始化历年成绩筛选控件");
    }
    
    /**
     * 为本学期成绩标签页创建图表
     */
    private void createChartsForCurrentSemester(ObservableList<ScoreRecord> scores) {
        // 查找图表容器
        VBox currentSemesterContent = (VBox) currentSemesterTab.getContent();
        if (currentSemesterContent == null) return;
        
        // 更新当前学期的图表数据
        System.out.println("更新本学期成绩图表，课程数量：" + scores.size());
    }
    
    /**
     * 切换到成绩统计标签页并加载统计数据
     */
    private void loadStatisticsData() {
        // 更新图表数据
        updateScoreDistributionChart(FXCollections.observableArrayList());
        
        // 更新学期绩点统计
        avgGpaLabel.setText("0.00");
        totalCreditsLabel.setText("0.0");
        completedCoursesLabel.setText("0");
        rankingLabel.setText("--/--");
        
        System.out.println("已加载成绩统计数据");
    }
    
    /**
     * 获取历年成绩数据 
     */
    private void fetchHistoricalGrades() {
        // 获取所有学期
        List<String> allTerms = getAllTerms();
        List<ScoreRecord> allGrades = new ArrayList<>();
        final int[] completedRequests = {0};
        final int totalRequests = allTerms.size();
        
        // 显示加载提示
        Platform.runLater(() -> {
            ShowMessage.showInfoMessage("加载中", "正在加载历年成绩数据，请稍候...");
        });
        
        // 为每个学期发起请求
        for (String term : allTerms) {
            Map<String, String> params = new HashMap<>();
            params.put("term", term);
            
            NetworkUtils.get("/grade/getGrade", params, new Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    try {
                        // 解析返回的JSON数据
                        List<ScoreRecord> termGrades = parseGradeData(result);
                        synchronized (allGrades) {
                            allGrades.addAll(termGrades);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        synchronized (completedRequests) {
                            completedRequests[0]++;
                            
                            // 检查是否所有请求都已完成
                            if (completedRequests[0] == totalRequests) {
                                Platform.runLater(() -> {
                                    // 更新UI
                                    scoreTableView.setItems(FXCollections.observableArrayList(allGrades));
                                    ShowMessage.showInfoMessage("加载完成", "历年成绩加载完成");
                                });
                            }
                        }
                    }
                }
                
                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    synchronized (completedRequests) {
                        completedRequests[0]++;
                        
                        // 检查是否所有请求都已完成
                        if (completedRequests[0] == totalRequests) {
                            Platform.runLater(() -> {
                                // 如果没有获取到任何成绩，显示错误信息
                                if (allGrades.isEmpty()) {
                                    ShowMessage.showErrorMessage("获取失败", "无法获取历年成绩数据");
                                } else {
                                    // 更新UI
                                    scoreTableView.setItems(FXCollections.observableArrayList(allGrades));
                                    ShowMessage.showInfoMessage("加载完成", "历年成绩加载完成");
                                }
                            });
                        }
                    }
                }
            });
        }
    }
} 