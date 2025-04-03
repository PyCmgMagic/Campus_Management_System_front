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

    // --- FXML Injections ---

    // Info Cards Labels
    @FXML private Label pendingClassesLabel;
    @FXML private Label enteredClassesLabel;
    @FXML private Label dueClassesLabel;
    @FXML private Label failedStudentsLabel;

    // Filter Controls
    @FXML private ComboBox<String> courseComboBox;
    @FXML private ComboBox<String> classComboBox;
    @FXML private ComboBox<String> examTypeComboBox;
    @FXML private DatePicker datePicker;

    // TableView and Columns
    @FXML private TableView<ScoreEntry> scoreTableView;
    @FXML private TableColumn<ScoreEntry, String> studentIdCol;
    @FXML private TableColumn<ScoreEntry, String> nameCol;
    @FXML private TableColumn<ScoreEntry, String> classCol;
    @FXML private TableColumn<ScoreEntry, String> courseCol;
    @FXML private TableColumn<ScoreEntry, Double> regularScoreCol;
    @FXML private TableColumn<ScoreEntry, Double> finalScoreCol;
    @FXML private TableColumn<ScoreEntry, Double> totalScoreCol;
    @FXML private TableColumn<ScoreEntry, String> statusCol;
    @FXML private TableColumn<ScoreEntry, String> remarksCol;
    @FXML private TableColumn<ScoreEntry, Void> actionCol;

    // Stats Labels
    @FXML private Label avgScoreLabel;
    @FXML private Label maxScoreLabel;
    @FXML private Label minScoreLabel;
    @FXML private Label passRateLabel;
    @FXML private Label excellentRateLabel;

    // Charts
    @FXML private BarChart<String, Number> gradeDistributionChart;
    @FXML private CategoryAxis barChartXAxis;
    @FXML private NumberAxis barChartYAxis;
    @FXML private PieChart gradeLevelChart;
    @FXML private VBox pieChartLegend; // VBox for custom legend


    // --- Data ---
    private ObservableList<ScoreEntry> scoreData = FXCollections.observableArrayList();

    // --- Initialization ---

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. Populate ComboBoxes
        setupComboBoxes();

        // 2. Configure TableView Columns
        setupTableView();

        // 3. Load Initial Data (Example)
        loadSampleData();

        // 4. Setup Charts
        setupCharts();

        // 5. Calculate and Display Initial Stats
        updateStatistics();

        // 6. Set default date (optional)
        datePicker.setValue(LocalDate.now()); // Set current date
    }

    // --- Setup Methods ---

    private void setupComboBoxes() {
        courseComboBox.setItems(FXCollections.observableArrayList("高等数学 (II)", "程序设计基础", "数据结构", "计算机网络", "全部课程"));
        classComboBox.setItems(FXCollections.observableArrayList("计算机系2班", "计算机系1班", "软件工程1班", "全部班级"));
        examTypeComboBox.setItems(FXCollections.observableArrayList("期末考试", "期中考试", "平时测验", "全部类型"));

        // Select default values if needed
         courseComboBox.getSelectionModel().selectFirst();
         classComboBox.getSelectionModel().selectFirst();
         examTypeComboBox.getSelectionModel().selectFirst();
    }

    private void setupTableView() {
        // Bind columns to ScoreEntry properties
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        classCol.setCellValueFactory(new PropertyValueFactory<>("className"));
        courseCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        totalScoreCol.setCellValueFactory(new PropertyValueFactory<>("totalScore")); // Calculated, not editable
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));     // Calculated, not editable

        // Make score columns editable with validation (using DoubleStringConverter)
        regularScoreCol.setCellValueFactory(new PropertyValueFactory<>("regularScore"));
        regularScoreCol.setCellFactory(TextFieldTableCell.forTableColumn(new ScoreStringConverter()));
        regularScoreCol.setOnEditCommit(event -> {
            ScoreEntry entry = event.getRowValue();
            entry.setRegularScore(event.getNewValue());
            scoreTableView.refresh(); // Refresh row to show updated total and status
            updateStatistics();     // Recalculate stats
        });

        finalScoreCol.setCellValueFactory(new PropertyValueFactory<>("finalScore"));
        finalScoreCol.setCellFactory(TextFieldTableCell.forTableColumn(new ScoreStringConverter()));
        finalScoreCol.setOnEditCommit(event -> {
            ScoreEntry entry = event.getRowValue();
            entry.setFinalScore(event.getNewValue());
            scoreTableView.refresh();
            updateStatistics();
        });

        // Make remarks column editable
        remarksCol.setCellValueFactory(new PropertyValueFactory<>("remarks"));
        remarksCol.setCellFactory(TextFieldTableCell.forTableColumn());
        remarksCol.setOnEditCommit(event -> {
            ScoreEntry entry = event.getRowValue();
            entry.setRemarks(event.getNewValue());
            // Potentially save remark change immediately or mark row as dirty
        });

        // Style the status column
        statusCol.setCellFactory(column -> new TableCell<ScoreEntry, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                    getStyleClass().removeAll("status-normal", "status-absent", "status-fail"); // Clear previous styles
                } else {
                    setText(item);
                    // Remove old styles before adding new ones
                    getStyleClass().removeAll("status-normal", "status-absent", "status-fail");
                    if ("通过".equals(item)) {
                        // Use CSS class for styling
                        getStyleClass().add("status-normal");
                    } else if ("不及格".equals(item)) {
                        getStyleClass().add("status-fail"); // Match prototype name
                    } else {
                         // Potentially handle other statuses like "缺考" (Absent)
                         if("缺考".equals(item)) { // Example for absent status
                            getStyleClass().add("status-absent");
                         }
                        // Default style (no specific class)
                    }
                }
            }
        });


        // Add Save button to action column
        actionCol.setCellFactory(param -> new TableCell<ScoreEntry, Void>() {
            private final Button saveButton = new Button("保存");
            {
                saveButton.getStyleClass().add("secondary-button"); // Apply CSS style
                saveButton.setOnAction(event -> {
                    ScoreEntry entry = getTableView().getItems().get(getIndex());
                    System.out.println("Saving changes for: " + entry.getName());
                    // --- Add actual save logic here ---
                    // e.g., call a service to update the database
                    // Indicate success/failure to the user
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

        // Set the data source for the table
        scoreTableView.setItems(scoreData);
    }

    private void setupCharts() {
        // Bar Chart Setup
        barChartXAxis.setLabel("分数段");
        barChartYAxis.setLabel("人数");
        gradeDistributionChart.setTitle("");
        gradeDistributionChart.setLegendVisible(false);

        // --- Configuration for Integer Ticks on Y-Axis ---
        barChartYAxis.setAutoRanging(false); // Disable auto-ranging to manually control ticks
        barChartYAxis.setLowerBound(0);      // Start axis at 0
        // We'll set the upper bound and tick unit dynamically in updateBarChart
        // based on the maximum value.
        barChartYAxis.setMinorTickCount(0); // No minor ticks between integers
        // Optional: Format tick labels to ensure they show as integers without decimals
        barChartYAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                // Only show integer values
                 if (object.doubleValue() == object.intValue()) {
                    return String.valueOf(object.intValue());
                }
                return ""; // Hide non-integer tick labels if any appear
            }

            @Override
            public Number fromString(String string) {
                // Not needed for formatting
                return null;
            }
        });
        // --- End Y-Axis Configuration ---


        // Pie Chart Setup
        gradeLevelChart.setTitle("");
        gradeLevelChart.setLabelsVisible(false);
        gradeLevelChart.setLegendVisible(false);
    }


    // --- Data Loading & Handling ---

    private void loadSampleData() {
        // In a real app, load this from a database or API based on filters
        scoreData.addAll(
            new ScoreEntry("2025001", "张三", "计算机系2班", "高等数学 (II)", 85.0, 78.0, "-"),
            new ScoreEntry("2025015", "李四", "计算机系2班", "高等数学 (II)", 65.0, 56.0, ""),
            new ScoreEntry("2025022", "王五", "计算机系2班", "高等数学 (II)", 92.0, 88.0, "-"),
            new ScoreEntry("2025037", "赵六", "计算机系2班", "高等数学 (II)", 73.0, 81.0, "-"),
            new ScoreEntry("2025043", "孙七", "计算机系2班", "高等数学 (II)", 61.0, 50.0, "")
            // Add more entries if needed
        );

        // Update Info Card Labels (Example - replace with real data)
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
                                            .filter(score -> score != null) // Only consider entries with a calculated score
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
        long totalCount = validScores.size(); // Count only those with valid scores
        long passCount = validScores.stream().filter(score -> score >= 60.0).count();
        long excellentCount = validScores.stream().filter(score -> score >= 90.0).count(); // Assuming 90+ is excellent

        avgScoreLabel.setText(String.format("%.1f", avg));
        maxScoreLabel.setText(String.format("%.1f", max));
        minScoreLabel.setText(String.format("%.1f", min));
        passRateLabel.setText(String.format("%.0f%%", totalCount == 0 ? 0 : (double) passCount / totalCount * 100));
        excellentRateLabel.setText(String.format("%.0f%%", totalCount == 0 ? 0 : (double) excellentCount / totalCount * 100)); // Corrected calculation

        // Update Charts
        updateBarChart(validScores);
        updatePieChart(validScores);

         // Update failed student count in info card
         failedStudentsLabel.setText(String.valueOf(scoreData.stream().filter(s -> "不及格".equals(s.getStatus())).count()));
    }

    private void updateBarChart(List<Double> scores) {
        long failCount = scores.stream().filter(s -> s < 60).count();
        long sixtyToSeventy = scores.stream().filter(s -> s >= 60 && s < 70).count();
        long seventyToEighty = scores.stream().filter(s -> s >= 70 && s < 80).count();
        long eightyToNinety = scores.stream().filter(s -> s >= 80 && s < 90).count();
        long ninetyToHundred = scores.stream().filter(s -> s >= 90 && s <= 100).count();

        // Find the maximum count to set the upper bound of the axis
        long maxCount = Math.max(failCount, Math.max(sixtyToSeventy, Math.max(seventyToEighty, Math.max(eightyToNinety, ninetyToHundred))));

        // --- Dynamically set Upper Bound and Tick Unit ---
        barChartYAxis.setUpperBound(maxCount + 1); // Set upper bound slightly above max count
        barChartYAxis.setTickUnit(1);            // Set tick step to 1
        // --- End Dynamic Axis Update ---


        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("不及格", failCount));
        series.getData().add(new XYChart.Data<>("60-70", sixtyToSeventy));
        series.getData().add(new XYChart.Data<>("70-80", seventyToEighty));
        series.getData().add(new XYChart.Data<>("80-90", eightyToNinety));
        series.getData().add(new XYChart.Data<>("90-100", ninetyToHundred));

        gradeDistributionChart.getData().setAll(series); // Replace existing data

         // Apply CSS Styles for bar colors
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
         long passCount = scores.stream().filter(s -> s >= 60 && s < 80).count(); // 及格 60-79 (Adjusted range from prototype)
         long failCount = scores.stream().filter(s -> s < 60).count();      // 不及格 <60

         long totalValid = scores.size();
         if (totalValid == 0) {
             gradeLevelChart.setData(FXCollections.observableArrayList());
             pieChartLegend.getChildren().clear();
             return;
         }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        // Add slices only if count > 0 to avoid clutter
        if (excellentCount > 0) pieData.add(new PieChart.Data(String.format("优秀 (%d)", excellentCount), excellentCount));
        if (goodCount > 0) pieData.add(new PieChart.Data(String.format("良好 (%d)", goodCount), goodCount));
        if (passCount > 0) pieData.add(new PieChart.Data(String.format("及格 (%d)", passCount), passCount));
        if (failCount > 0) pieData.add(new PieChart.Data(String.format("不及格 (%d)", failCount), failCount));

        gradeLevelChart.setData(pieData);

         // Update Custom Legend
         pieChartLegend.getChildren().clear(); // Clear old legend items
         // Define colors corresponding to the order above (Excellent, Good, Pass, Fail)
         String[] legendColors = {"#3F51B5", "#4CAF50", "#FFC107", "#FF9800"}; // Matches prototype colors
         int colorIndex = 0;
         if (excellentCount > 0) pieChartLegend.getChildren().add(createLegendItem(legendColors[0], String.format("优秀(90+): %d人 (%.0f%%)", excellentCount, (double)excellentCount/totalValid*100)));
         if (goodCount > 0) pieChartLegend.getChildren().add(createLegendItem(legendColors[1], String.format("良好(80-89): %d人 (%.0f%%)", goodCount, (double)goodCount/totalValid*100)));
         if (passCount > 0) pieChartLegend.getChildren().add(createLegendItem(legendColors[2], String.format("及格(60-79): %d人 (%.0f%%)", passCount, (double)passCount/totalValid*100)));
         if (failCount > 0) pieChartLegend.getChildren().add(createLegendItem(legendColors[3], String.format("不及格(<60): %d人 (%.0f%%)", failCount, (double)failCount/totalValid*100)));


        // Apply CSS Styles for pie colors after data is set
        Platform.runLater(() -> {
            int i = 0;
            for (PieChart.Data data : gradeLevelChart.getData()) {
                // Remove old styles
                data.getNode().getStyleClass().removeIf(style -> style.startsWith("pie-color-"));
                // Add new style based on index
                data.getNode().getStyleClass().add("pie-color-" + i++);
             }
         });
    }

    // Helper to create legend items
    private Node createLegendItem(String color, String text) {
        HBox legendItem = new HBox(8);
        legendItem.setAlignment(Pos.CENTER_LEFT);
        Rectangle colorRect = new Rectangle(15, 15);
        colorRect.setFill(Color.web(color)); // Use web color string
        colorRect.setArcWidth(3);
        colorRect.setArcHeight(3);
        Label label = new Label(text);
        label.getStyleClass().add("legend-label"); // Add class for styling if needed
        legendItem.getChildren().addAll(colorRect, label);
        return legendItem;
    }


    // --- Event Handlers (from FXML onAction) ---

    @FXML
    void handleQuery(ActionEvent event) {
        String selectedCourse = courseComboBox.getValue();
        String selectedClass = classComboBox.getValue();
        String selectedExamType = examTypeComboBox.getValue();
        LocalDate selectedDate = datePicker.getValue();

        System.out.println("Querying with:");
        System.out.println("  Course: " + selectedCourse);
        System.out.println("  Class: " + selectedClass);
        System.out.println("  Exam Type: " + selectedExamType);
        System.out.println("  Date: " + selectedDate);

        // --- Add actual query logic here ---
        // 1. Call a service/DAO to fetch data based on filter criteria.
        // 2. Clear the existing scoreData: scoreData.clear();
        // 3. Populate scoreData with the new results: scoreData.addAll(newResults);
        // 4. Update statistics: updateStatistics();
        // Example: Simulating a new query result
        scoreData.clear();
        if("数据结构".equals(selectedCourse)) {
             scoreData.addAll(
                new ScoreEntry("2025101", "小明", "软件工程1班", "数据结构", 75.0, 80.0, ""),
                new ScoreEntry("2025102", "小红", "软件工程1班", "数据结构", 95.0, 92.0, "优秀")
             );
        } else {
            loadSampleData(); // Reload sample data if not data structure
        }
        updateStatistics(); // Update stats for the new data
    }

    @FXML
    void handleBatchImport(ActionEvent event) {
        System.out.println("Batch Import button clicked");
        // --- Add file chooser and import logic here ---
    }

    @FXML
    void handleExportExcel(ActionEvent event) {
        System.out.println("Export Excel button clicked");
        // --- Add Excel export logic here ---
    }

    @FXML
    void handleSortById(ActionEvent event) {
        System.out.println("Sort by ID button clicked");
        scoreTableView.getSortOrder().clear(); // Clear previous sorts
        studentIdCol.setSortType(TableColumn.SortType.ASCENDING);
        scoreTableView.getSortOrder().add(studentIdCol);
        scoreTableView.sort();
    }

    @FXML
    void handleSortByScore(ActionEvent event) {
        System.out.println("Sort by Score button clicked");
         scoreTableView.getSortOrder().clear();
         totalScoreCol.setSortType(TableColumn.SortType.DESCENDING); // Sort descending by default
         scoreTableView.getSortOrder().add(totalScoreCol);
         scoreTableView.sort();
    }

    @FXML
    void handleCancel(ActionEvent event) {
        System.out.println("Cancel button clicked");
        // --- Add cancel logic here ---
        // Maybe revert changes or reload original data
         loadSampleData(); // Reload original sample data as example
         updateStatistics();
    }

    @FXML
    void handleBatchSave(ActionEvent event) {
        System.out.println("Batch Save button clicked");
        // --- Add batch save logic here ---
        // Iterate through scoreData, identify modified rows, and save them
        System.out.println("Items to potentially save:");
        for(ScoreEntry entry : scoreData) {
            // Here you might check an 'isDirty' flag if you implement one
            System.out.println("  " + entry.getName() + ": " + entry.getTotalScore());
        }
        // Show confirmation to user
    }

    @FXML
    void handleSubmitLock(ActionEvent event) {
        System.out.println("Submit and Lock button clicked");
        // --- Add submit and lock logic here ---
        // 1. Perform final validation.
        // 2. Save all data.
        // 3. Potentially disable editing controls.
        // 4. Show confirmation.
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定提交并锁定所有成绩吗？此操作可能无法撤销。", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                System.out.println("Submitting and locking...");
                // ... actual submission logic ...
                scoreTableView.setEditable(false); // Example: Disable table editing
                // Disable other buttons if needed
            }
        });
    }


    // --- Inner Class for Data Model ---
    // (Could be in its own file if preferred)
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
        // private boolean dirty = false; // Optional flag for tracking changes

        public ScoreEntry(String studentId, String name, String className, String courseName, Double regularScore, Double finalScore, String remarks) {
            this.studentId = studentId;
            this.name = name;
            this.className = className;
            this.courseName = courseName;
            this.regularScore = regularScore;
            this.finalScore = finalScore;
            this.remarks = remarks;
            calculateTotalScore(); // Initial calculation
            updateStatus();        // Initial status update
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


        // --- Calculation Logic ---
        private void calculateTotalScore() {
            // Assuming 30% regular, 70% final. Handle nulls.
            if (regularScore != null && finalScore != null) {
                 // Round to one decimal place for display consistency
                this.totalScore = Math.round((regularScore * 0.3 + finalScore * 0.7) * 10.0) / 10.0;
            } else {
                this.totalScore = null;
            }
        }

        private void updateStatus() {
             if (totalScore == null) {
                this.status = "-"; // Or "未计算"
            } else if (totalScore >= 60) {
                this.status = "通过";
            } else {
                this.status = "不及格";
            }
            // Could add "缺考" logic if specific input indicates absence
        }
    }


     // --- Helper Class for Score Input Validation ---
    private static class ScoreStringConverter extends StringConverter<Double> {
        @Override
        public String toString(Double object) {
            return object == null ? "" : String.format("%.1f", object); // Format to one decimal place
        }

        @Override
        public Double fromString(String string) {
            if (string == null || string.trim().isEmpty()) {
                return null; // Allow empty input (representing null score)
            }
            try {
                double value = Double.parseDouble(string);
                if (value < 0 || value > 100) {
                    // Optionally show an error message to the user
                    System.err.println("Score must be between 0 and 100: " + string);
                     showErrorAlert("输入错误", "分数必须在 0 到 100 之间。");
                    // How to handle invalid input? Revert or keep?
                    // Returning null might be safest to prevent invalid data propagation
                     return null; // Indicate conversion failure
                }
                 // Round to one decimal place upon input as well
                return Math.round(value * 10.0) / 10.0;
            } catch (NumberFormatException e) {
                System.err.println("Invalid number format for score: " + string);
                 showErrorAlert("输入错误", "请输入有效的分数数字。");
                return null; // Indicate conversion failure
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