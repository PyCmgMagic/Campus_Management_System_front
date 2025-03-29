package com.work.javafx.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.application.Platform;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class TeachingEvaluationContentController implements Initializable {

    // 导航按钮
    @FXML private Button pendingEvalBtn;
    @FXML private Button completedEvalBtn;
    @FXML private Button evaluationStatsBtn;
    
    // 查询条件控件
    @FXML private ComboBox<String> academicYearComboBox;
    @FXML private ComboBox<String> semesterComboBox;
    @FXML private Button searchButton;
    
    // 内容容器
    @FXML private VBox pendingEvaluationContainer;
    @FXML private VBox completedEvaluationContainer;
    @FXML private VBox evaluationStatsContainer;
    @FXML private ScrollPane contentScrollPane; 
    
    // 待评价课程表格
    @FXML private TableView<CourseEvaluation> pendingCoursesTableView;
    @FXML private TableColumn<CourseEvaluation, Integer> pendingNumberColumn;
    @FXML private TableColumn<CourseEvaluation, String> pendingCourseCodeColumn;
    @FXML private TableColumn<CourseEvaluation, String> pendingCourseNameColumn;
    @FXML private TableColumn<CourseEvaluation, String> pendingTeacherColumn;
    @FXML private TableColumn<CourseEvaluation, Double> pendingCreditColumn;
    @FXML private TableColumn<CourseEvaluation, String> pendingStatusColumn;
    @FXML private TableColumn<CourseEvaluation, Void> pendingActionColumn;
    @FXML private Label pendingCountLabel;
    
    // 已评价课程表格
    @FXML private TableView<CourseEvaluation> completedCoursesTableView;
    @FXML private TableColumn<CourseEvaluation, Integer> completedNumberColumn;
    @FXML private TableColumn<CourseEvaluation, String> completedCourseCodeColumn;
    @FXML private TableColumn<CourseEvaluation, String> completedCourseNameColumn;
    @FXML private TableColumn<CourseEvaluation, String> completedTeacherColumn;
    @FXML private TableColumn<CourseEvaluation, Double> completedCreditColumn;
    @FXML private TableColumn<CourseEvaluation, String> completedTimeColumn;
    @FXML private TableColumn<CourseEvaluation, Double> completedScoreColumn;
    @FXML private TableColumn<CourseEvaluation, Void> completedActionColumn;
    @FXML private Label completedCountLabel;
    
    // 评价统计
    @FXML private Label totalCoursesLabel;
    @FXML private Label evaluatedCoursesLabel;
    @FXML private Label completionRateLabel;
    @FXML private Label averageScoreLabel;
    
    // 评价详情表格
    @FXML private TableView<EvaluationDetail> evaluationDetailsTableView;
    @FXML private TableColumn<EvaluationDetail, Integer> detailsNumberColumn;
    @FXML private TableColumn<EvaluationDetail, String> detailsCourseNameColumn;
    @FXML private TableColumn<EvaluationDetail, String> detailsTeacherColumn;
    @FXML private TableColumn<EvaluationDetail, Double> detailsContentScoreColumn;
    @FXML private TableColumn<EvaluationDetail, Double> detailsMethodScoreColumn;
    @FXML private TableColumn<EvaluationDetail, Double> detailsAttitudeScoreColumn;
    @FXML private TableColumn<EvaluationDetail, Double> detailsEffectScoreColumn;
    @FXML private TableColumn<EvaluationDetail, Double> detailsOverallScoreColumn;
    
    // 数据集合
    private ObservableList<CourseEvaluation> pendingCourses = FXCollections.observableArrayList();
    private ObservableList<CourseEvaluation> completedCourses = FXCollections.observableArrayList();
    private ObservableList<EvaluationDetail> evaluationDetails = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化下拉框
        initComboBoxes();
        
        // 初始化表格
        initPendingCoursesTable();
        initCompletedCoursesTable();
        initEvaluationDetailsTable();
        pendingEvaluationContainer.setVisible(true);
        pendingEvaluationContainer.setManaged(true);

        completedEvaluationContainer.setVisible(false);
        completedEvaluationContainer.setManaged(false);

        evaluationStatsContainer.setVisible(false);
        evaluationStatsContainer.setManaged(false);
        // 加载示例数据
        loadSampleData();
        
        // 更新统计信息
        updateStatistics();
        
        System.out.println("教学评价界面初始化成功");
    }
    
    /**
     * 初始化下拉框
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
     * 初始化待评价课程表格
     */
    private void initPendingCoursesTable() {
        // 设置列的单元格值工厂
        pendingNumberColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(pendingCourses.indexOf(cellData.getValue()) + 1).asObject());
        pendingCourseCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCourseCode()));
        pendingCourseNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCourseName()));
        pendingTeacherColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTeacher()));
        pendingCreditColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getCredit()).asObject());
        pendingStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        
        // 设置状态列样式
        pendingStatusColumn.setCellFactory(column -> {
            return new TableCell<CourseEvaluation, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item);
                        if ("待评价".equals(item)) {
                            getStyleClass().add("status-pending");
                        } else {
                            getStyleClass().add("status-completed");
                        }
                    }
                }
            };
        });
        // 设置自定义行工厂来处理样式
        pendingCoursesTableView.setRowFactory(tv -> {
            TableRow<CourseEvaluation> row = new TableRow<CourseEvaluation>() {
                @Override
                protected void updateItem(CourseEvaluation item, boolean empty) {
                    super.updateItem(item, empty);

                    // 重置所有自定义样式
                    getStyleClass().removeAll("selected-row");

                    if (empty) {
                        setStyle("");
                    } else {
                        // 根据行索引设置交替行样式

                    }
                }
            };

            // 监听选择状态变化
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    row.getStyleClass().add("selected-row");
                } else {
                    row.getStyleClass().remove("selected-row");
//                    // 恢复交替行样式
//                    if (row.getIndex() % 2 == 0) {
//                        row.setStyle("-fx-background-color: white;");
//                    } else {
//                        row.setStyle("-fx-background-color: #fafafa;");
//                    }
                }
            });

            return row;
        });
        // 设置操作列
        pendingActionColumn.setCellFactory(createPendingActionCellFactory());
        
        // 设置表格数据源
        pendingCoursesTableView.setItems(pendingCourses);
    }
    
    /**
     * 初始化已评价课程表格
     */
    private void initCompletedCoursesTable() {
        // 设置列的单元格值工厂
        completedNumberColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(completedCourses.indexOf(cellData.getValue()) + 1).asObject());
        completedCourseCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCourseCode()));
        completedCourseNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCourseName()));
        completedTeacherColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTeacher()));
        completedCreditColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getCredit()).asObject());
        completedTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEvaluationTime()));
        completedScoreColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getScore()).asObject());
        
        // 设置评分列样式
        completedScoreColumn.setCellFactory(column -> {
            return new TableCell<CourseEvaluation, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);

                    // 清除所有之前的样式
                    getStyleClass().removeAll("high-score", "medium-score", "low-score");

                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(String.format("%.1f", item));

                        if (item >= 4.5) {
                            getStyleClass().add("high-score");
                        } else if (item >= 3.5) {
                            getStyleClass().add("medium-score");
                        } else {
                            getStyleClass().add("low-score");
                        }
                    }
                }
            };
        });
        // 设置自定义行工厂来处理样式
        completedCoursesTableView.setRowFactory(tv -> {
            TableRow<CourseEvaluation> row = new TableRow<CourseEvaluation>() {
                @Override
                protected void updateItem(CourseEvaluation item, boolean empty) {
                    super.updateItem(item, empty);

                    // 重置所有自定义样式
                    getStyleClass().removeAll("selected-row");

                    if (empty) {
                        setStyle("");
                    } else {
                        // 根据行索引设置交替行样式
                        if (getIndex() % 2 == 0) {
                            setStyle("-fx-background-color: white;");
                        } else {
                            setStyle("-fx-background-color: #fafafa;");
                        }
                    }
                }
            };

            // 监听选择状态变化
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    row.getStyleClass().add("selected-row");
                } else {
                    row.getStyleClass().remove("selected-row");
                    // 恢复交替行样式
                    if (row.getIndex() % 2 == 0) {
                        row.setStyle("-fx-background-color: white;");
                    } else {
                        row.setStyle("-fx-background-color: #fafafa;");
                    }
                }
            });

            return row;
        });
        // 设置操作列
        completedActionColumn.setCellFactory(createCompletedActionCellFactory());
        
        // 设置表格数据源
        completedCoursesTableView.setItems(completedCourses);
    }
    
    /**
     * 初始化评价详情表格
     */
    private void initEvaluationDetailsTable() {
        // 设置列的单元格值工厂
        detailsNumberColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(evaluationDetails.indexOf(cellData.getValue()) + 1).asObject());
        detailsCourseNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCourseName()));
        detailsTeacherColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTeacher()));
        detailsContentScoreColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getContentScore()).asObject());
        detailsMethodScoreColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getMethodScore()).asObject());
        detailsAttitudeScoreColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getAttitudeScore()).asObject());
        detailsEffectScoreColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getEffectScore()).asObject());
        detailsOverallScoreColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getOverallScore()).asObject());
        
        // 设置评分列样式
        Callback<TableColumn<EvaluationDetail, Double>, TableCell<EvaluationDetail, Double>> scoreCellFactory = 
                column -> new TableCell<EvaluationDetail, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (item == null || empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(String.format("%.1f", item));
                            getStyleClass().removeAll("high-score", "medium-score", "low-score");
                            
                            if (item >= 4.5) {
                                getStyleClass().add("high-score");
                            } else if (item >= 3.5) {
                                getStyleClass().add("medium-score");
                            } else {
                                getStyleClass().add("low-score");
                            }
                        }
                    }
                };
        
        detailsContentScoreColumn.setCellFactory(scoreCellFactory);
        detailsMethodScoreColumn.setCellFactory(scoreCellFactory);
        detailsAttitudeScoreColumn.setCellFactory(scoreCellFactory);
        detailsEffectScoreColumn.setCellFactory(scoreCellFactory);
        detailsOverallScoreColumn.setCellFactory(scoreCellFactory);
        
        // 设置表格数据源
        evaluationDetailsTableView.setItems(evaluationDetails);
    }
    
    /**
     * 创建待评价课程操作列单元格工厂
     */
    private Callback<TableColumn<CourseEvaluation, Void>, TableCell<CourseEvaluation, Void>> createPendingActionCellFactory() {
        return new Callback<TableColumn<CourseEvaluation, Void>, TableCell<CourseEvaluation, Void>>() {
            @Override
            public TableCell<CourseEvaluation, Void> call(TableColumn<CourseEvaluation, Void> param) {
                return new TableCell<CourseEvaluation, Void>() {
                    private final Button evaluateButton = new Button("评价");
                    
                    {
                        evaluateButton.getStyleClass().add("evaluate-button");
                        evaluateButton.setOnAction(event -> {
                            CourseEvaluation course = getTableView().getItems().get(getIndex());
                            evaluateCourse(course);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(evaluateButton);
                        }
                    }
                };
            }
        };
    }
    
    /**
     * 创建已评价课程操作列单元格工厂
     */
    private Callback<TableColumn<CourseEvaluation, Void>, TableCell<CourseEvaluation, Void>> createCompletedActionCellFactory() {
        return new Callback<TableColumn<CourseEvaluation, Void>, TableCell<CourseEvaluation, Void>>() {
            @Override
            public TableCell<CourseEvaluation, Void> call(TableColumn<CourseEvaluation, Void> param) {
                return new TableCell<CourseEvaluation, Void>() {
                    private final Button viewButton = new Button("查看");
                    
                    {
                        viewButton.getStyleClass().add("view-button");
                        viewButton.setOnAction(event -> {
                            CourseEvaluation course = getTableView().getItems().get(getIndex());
                            viewEvaluation(course);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(viewButton);
                        }
                    }
                };
            }
        };
    }
    
    /**
     * 加载示例数据
     */
    private void loadSampleData() {
        // 待评价课程
        pendingCourses.addAll(
                new CourseEvaluation("CS101", "计算机导论", "张教授", 3.0, "待评价"),
                new CourseEvaluation("MA102", "高等数学", "李教授", 4.0, "待评价"),
                new CourseEvaluation("PH103", "大学物理", "王教授", 3.5, "待评价"),
                new CourseEvaluation("EN104", "大学英语", "刘教授", 2.0, "待评价"),
                new CourseEvaluation("CS105", "数据结构", "赵教授", 4.0, "待评价")
        );
        
        // 已评价课程
        completedCourses.addAll(
                new CourseEvaluation("CS201", "面向对象程序设计", "钱教授", 3.0, "已评价", "2024-05-10 14:30", 4.8),
                new CourseEvaluation("CS202", "操作系统", "孙教授", 4.0, "已评价", "2024-05-08 09:15", 4.2),
                new CourseEvaluation("CS203", "计算机网络", "周教授", 3.5, "已评价", "2024-05-05 16:45", 3.9)
        );
        
        // 评价详情
        evaluationDetails.addAll(
                new EvaluationDetail("面向对象程序设计", "钱教授", 5.0, 4.8, 4.9, 4.5, 4.8),
                new EvaluationDetail("操作系统", "孙教授", 4.5, 4.0, 4.3, 4.0, 4.2),
                new EvaluationDetail("计算机网络", "周教授", 3.8, 4.0, 4.2, 3.6, 3.9)
        );
        
        // 更新计数标签
        pendingCountLabel.setText(String.format("共有%d门课程待评价", pendingCourses.size()));
        completedCountLabel.setText(String.format("共有%d门课程已评价", completedCourses.size()));
    }
    
    /**
     * 更新统计信息
     */
    private void updateStatistics() {
        int totalCourses = pendingCourses.size() + completedCourses.size();
        int evaluatedCourses = completedCourses.size();
        double completionRate = totalCourses > 0 ? (double) evaluatedCourses / totalCourses * 100 : 0;
        
        // 计算平均评分
        double totalScore = 0;
        for (CourseEvaluation course : completedCourses) {
            totalScore += course.getScore();
        }
        double averageScore = evaluatedCourses > 0 ? totalScore / evaluatedCourses : 0;
        
        // 更新统计标签
        totalCoursesLabel.setText(String.valueOf(totalCourses));
        evaluatedCoursesLabel.setText(String.valueOf(evaluatedCourses));
        completionRateLabel.setText(String.format("%.1f%%", completionRate));
        averageScoreLabel.setText(String.format("%.1f", averageScore));
    }
    
    /**
     * 切换到待评价课程视图
     */
    @FXML
    private void showPendingEvaluations() {
        // 显示待评价容器
        pendingEvaluationContainer.setVisible(true);
        pendingEvaluationContainer.setManaged(true);

        // 隐藏其他容器并从布局中移除
        completedEvaluationContainer.setVisible(false);
        completedEvaluationContainer.setManaged(false);
        evaluationStatsContainer.setVisible(false);
        evaluationStatsContainer.setManaged(false);

        // 更新导航按钮样式
        pendingEvalBtn.getStyleClass().add("active-nav-button");
        completedEvalBtn.getStyleClass().remove("active-nav-button");
        evaluationStatsBtn.getStyleClass().remove("active-nav-button");
    }

    @FXML
    private void showCompletedEvaluations() {
        // 隐藏待评价容器并从布局中移除
        pendingEvaluationContainer.setVisible(false);
        pendingEvaluationContainer.setManaged(false);

        // 显示已评价容器
        completedEvaluationContainer.setVisible(true);
        completedEvaluationContainer.setManaged(true);

        // 隐藏统计容器并从布局中移除
        evaluationStatsContainer.setVisible(false);
        evaluationStatsContainer.setManaged(false);

        // 更新导航按钮样式
        pendingEvalBtn.getStyleClass().remove("active-nav-button");
        completedEvalBtn.getStyleClass().add("active-nav-button");
        evaluationStatsBtn.getStyleClass().remove("active-nav-button");
    }

    @FXML
    private void showEvaluationStats() {
        // 隐藏待评价容器并从布局中移除
        pendingEvaluationContainer.setVisible(false);
        pendingEvaluationContainer.setManaged(false);

        // 隐藏已评价容器并从布局中移除
        completedEvaluationContainer.setVisible(false);
        completedEvaluationContainer.setManaged(false);

        // 显示统计容器
        evaluationStatsContainer.setVisible(true);
        evaluationStatsContainer.setManaged(true);

        // 更新导航按钮样式
        pendingEvalBtn.getStyleClass().remove("active-nav-button");
        completedEvalBtn.getStyleClass().remove("active-nav-button");
        evaluationStatsBtn.getStyleClass().add("active-nav-button");
    }

    /**
     * 查询课程
     */
    @FXML
    private void searchCourses() {
        String academicYear = academicYearComboBox.getValue();
        String semester = semesterComboBox.getValue();

        System.out.println("查询条件: " + academicYear + " " + semester);

        // 重新加载数据（实际应用中应该从数据库获取）
        pendingCourses.clear();
        completedCourses.clear();
        evaluationDetails.clear();

        // 模拟根据条件筛选数据
        if ("2024-2025".equals(academicYear) && "第二学期".equals(semester)) {
            // 加载当前学期数据
            loadSampleData();
        } else if ("2023-2024".equals(academicYear) && "第一学期".equals(semester)) {
            // 加载上一学年第一学期的示例数据
            pendingCourses.addAll(
                    new CourseEvaluation("CS301", "Java程序设计", "吴教授", 3.0, "待评价"),
                    new CourseEvaluation("CS302", "数据库原理", "郑教授", 4.0, "待评价")
            );

            completedCourses.addAll(
                    new CourseEvaluation("CS303", "软件工程", "陈教授", 3.0, "已评价", "2023-12-20 10:30", 4.6)
            );

            evaluationDetails.addAll(
                    new EvaluationDetail("软件工程", "陈教授", 4.8, 4.5, 4.7, 4.4, 4.6)
            );
        } else {
            // 其他学期暂无数据
        }

        // 更新计数标签和统计信息
        pendingCountLabel.setText(String.format("共有%d门课程待评价", pendingCourses.size()));
        completedCountLabel.setText(String.format("共有%d门课程已评价", completedCourses.size()));
        updateStatistics();
    }

    /**
     * 评价课程
     */
    private void evaluateCourse(CourseEvaluation course) {
        System.out.println("评价课程: " + course.getCourseName());

        // 创建评价对话框
        Dialog<EvaluationDetail> dialog = new Dialog<>();
        dialog.setTitle("课程评价");
        dialog.setHeaderText(course.getCourseName() + " - " + course.getTeacher());

        // 设置对话框样式
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("evaluation-dialog");
        dialogPane.getStylesheets().add(getClass().getResource("/com/work/javafx/css/TeachingEvaluation.css").toExternalForm());

        // 设置按钮
        ButtonType submitButtonType = new ButtonType("提交", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, cancelButtonType);

        // 自定义按钮样式
        Button submitButton = (Button) dialog.getDialogPane().lookupButton(submitButtonType);
        submitButton.getStyleClass().add("evaluation-submit-button");

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.getStyleClass().add("evaluation-cancel-button");
        cancelButton.setText("取消");

        // 创建评价表单
        GridPane grid = new GridPane();
        grid.getStyleClass().add("evaluation-form-grid");
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 20, 25));

        // 创建标题
        Label titleLabel = new Label("请对以下方面进行评分(1-5分)：");
        titleLabel.getStyleClass().add("evaluation-form-title");
        grid.add(titleLabel, 0, 0, 3, 1);

        // 创建评分滑块
        Slider contentSlider = createRatingSlider("教学内容评分");
        Slider methodSlider = createRatingSlider("教学方法评分");
        Slider attitudeSlider = createRatingSlider("教学态度评分");
        Slider effectSlider = createRatingSlider("教学效果评分");

        // 添加评分项到表单
        Label contentPrompt = new Label("教学内容:");
        contentPrompt.getStyleClass().add("evaluation-item-label");
        grid.add(contentPrompt, 0, 1);
        grid.add(contentSlider, 1, 1);

        Label contentScoreLabel = new Label(String.format("%.1f", contentSlider.getValue()));
        contentScoreLabel.getStyleClass().add("evaluation-score-label");
        grid.add(contentScoreLabel, 2, 1);

        Label methodPrompt = new Label("教学方法:");
        methodPrompt.getStyleClass().add("evaluation-item-label");
        grid.add(methodPrompt, 0, 2);
        grid.add(methodSlider, 1, 2);

        Label methodScoreLabel = new Label(String.format("%.1f", methodSlider.getValue()));
        methodScoreLabel.getStyleClass().add("evaluation-score-label");
        grid.add(methodScoreLabel, 2, 2);

        Label attitudePrompt = new Label("教学态度:");
        attitudePrompt.getStyleClass().add("evaluation-item-label");
        grid.add(attitudePrompt, 0, 3);
        grid.add(attitudeSlider, 1, 3);

        Label attitudeScoreLabel = new Label(String.format("%.1f", attitudeSlider.getValue()));
        attitudeScoreLabel.getStyleClass().add("evaluation-score-label");
        grid.add(attitudeScoreLabel, 2, 3);

        Label effectPrompt = new Label("教学效果:");
        effectPrompt.getStyleClass().add("evaluation-item-label");
        grid.add(effectPrompt, 0, 4);
        grid.add(effectSlider, 1, 4);

        Label effectScoreLabel = new Label(String.format("%.1f", effectSlider.getValue()));
        effectScoreLabel.getStyleClass().add("evaluation-score-label");
        grid.add(effectScoreLabel, 2, 4);

        // 添加评价意见文本区域
        Label commentsPrompt = new Label("评价意见:");
        commentsPrompt.getStyleClass().add("evaluation-item-label");
        grid.add(commentsPrompt, 0, 5);

        TextArea commentsArea = new TextArea();
        commentsArea.getStyleClass().add("evaluation-comments-area");
        commentsArea.setPrefRowCount(4);
        commentsArea.setPromptText("请输入您对本课程的评价建议...");
        commentsArea.setWrapText(true);
        grid.add(commentsArea, 1, 5, 2, 1);

        // 更新分数显示
        contentSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                contentScoreLabel.setText(String.format("%.1f", newVal.doubleValue())));

        methodSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                methodScoreLabel.setText(String.format("%.1f", newVal.doubleValue())));

        attitudeSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                attitudeScoreLabel.setText(String.format("%.1f", newVal.doubleValue())));

        effectSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                effectScoreLabel.setText(String.format("%.1f", newVal.doubleValue())));

        dialog.getDialogPane().setContent(grid);

        // 设置提交结果转换器
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                double contentScore = contentSlider.getValue();
                double methodScore = methodSlider.getValue();
                double attitudeScore = attitudeSlider.getValue();
                double effectScore = effectSlider.getValue();
                double overallScore = (contentScore + methodScore + attitudeScore + effectScore) / 4.0;

                return new EvaluationDetail(
                        course.getCourseName(),
                        course.getTeacher(),
                        contentScore,
                        methodScore,
                        attitudeScore,
                        effectScore,
                        overallScore
                );
            }
            return null;
        });

        // 显示对话框并处理结果
        Optional<EvaluationDetail> result = dialog.showAndWait();

        result.ifPresent(evaluationDetail -> {
            // 移除待评价课程
            pendingCourses.remove(course);

            // 添加到已评价列表
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String evaluationTime = now.format(formatter);

            CourseEvaluation evaluatedCourse = new CourseEvaluation(
                    course.getCourseCode(),
                    course.getCourseName(),
                    course.getTeacher(),
                    course.getCredit(),
                    "已评价",
                    evaluationTime,
                    evaluationDetail.getOverallScore()
            );

            completedCourses.add(evaluatedCourse);

            // 添加评价详情
            evaluationDetails.add(evaluationDetail);

            // 更新计数标签和统计信息
            pendingCountLabel.setText(String.format("共有%d门课程待评价", pendingCourses.size()));
            completedCountLabel.setText(String.format("共有%d门课程已评价", completedCourses.size()));
            updateStatistics();

            // 提示评价成功
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("评价成功");
            alert.setHeaderText(null);
            alert.setContentText(String.format("您已成功评价课程: %s", course.getCourseName()));
            alert.showAndWait();
        });
    }

    /**
     * 创建评分滑块
     */
    private Slider createRatingSlider(String name) {
        Slider slider = new Slider(1, 5, 4.5);
        slider.getStyleClass().add("evaluation-slider");
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(4);
        slider.setBlockIncrement(0.1);
        slider.setSnapToTicks(true);
        slider.setPrefWidth(220);
        return slider;
    }
    
    /**
     * 查看评价详情
     */
    private void viewEvaluation(CourseEvaluation course) {
        System.out.println("查看评价: " + course.getCourseName());
        
        // 模拟查看评价详情
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("评价详情");
        alert.setHeaderText(course.getCourseName() + " - " + course.getTeacher());
        
        // 查找对应的评价详情
        EvaluationDetail detail = null;
        for (EvaluationDetail d : evaluationDetails) {
            if (d.getCourseName().equals(course.getCourseName())) {
                detail = d;
                break;
            }
        }
        
        if (detail != null) {
            String content = String.format(
                    "评价时间: %s\n" +
                    "教学内容: %.1f\n" +
                    "教学方法: %.1f\n" +
                    "教学态度: %.1f\n" +
                    "教学效果: %.1f\n" +
                    "综合评分: %.1f",
                    course.getEvaluationTime(),
                    detail.getContentScore(),
                    detail.getMethodScore(),
                    detail.getAttitudeScore(),
                    detail.getEffectScore(),
                    detail.getOverallScore()
            );
            alert.setContentText(content);
        } else {
            alert.setContentText("未找到评价详情");
        }
        
        alert.showAndWait();
    }
    
    /**
     * 课程评价实体类
     */
    public static class CourseEvaluation {
        private final String courseCode;
        private final String courseName;
        private final String teacher;
        private final double credit;
        private final String status;
        private final String evaluationTime;
        private final double score;
        
        public CourseEvaluation(String courseCode, String courseName, String teacher, double credit, String status) {
            this(courseCode, courseName, teacher, credit, status, "", 0);
        }
        
        public CourseEvaluation(String courseCode, String courseName, String teacher, double credit, String status, String evaluationTime, double score) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.teacher = teacher;
            this.credit = credit;
            this.status = status;
            this.evaluationTime = evaluationTime;
            this.score = score;
        }
        
        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public String getTeacher() { return teacher; }
        public double getCredit() { return credit; }
        public String getStatus() { return status; }
        public String getEvaluationTime() { return evaluationTime; }
        public double getScore() { return score; }
    }
    
    /**
     * 评价详情实体类
     */
    public static class EvaluationDetail {
        private final String courseName;
        private final String teacher;
        private final double contentScore;
        private final double methodScore;
        private final double attitudeScore;
        private final double effectScore;
        private final double overallScore;
        
        public EvaluationDetail(String courseName, String teacher, double contentScore, double methodScore, double attitudeScore, double effectScore, double overallScore) {
            this.courseName = courseName;
            this.teacher = teacher;
            this.contentScore = contentScore;
            this.methodScore = methodScore;
            this.attitudeScore = attitudeScore;
            this.effectScore = effectScore;
            this.overallScore = overallScore;
        }
        
        public String getCourseName() { return courseName; }
        public String getTeacher() { return teacher; }
        public double getContentScore() { return contentScore; }
        public double getMethodScore() { return methodScore; }
        public double getAttitudeScore() { return attitudeScore; }
        public double getEffectScore() { return effectScore; }
        public double getOverallScore() { return overallScore; }
    }
}