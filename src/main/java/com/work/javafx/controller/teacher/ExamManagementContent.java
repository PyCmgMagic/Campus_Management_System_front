package com.work.javafx.controller.teacher;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class ExamManagementContent implements Initializable {

    //<editor-fold desc="FXML Fields - Info Cards">
    @FXML private Label pendingExamCountLabel;
    @FXML private Label ongoingExamCountLabel;
    @FXML private Label thisWeekExamCountLabel;
    //</editor-fold>

    //<editor-fold desc="FXML Fields - Tabs">
    @FXML private TabPane mainTabPane;
    //</editor-fold>

    //<editor-fold desc="FXML Fields - Exam Plan Tab">
    @FXML private ComboBox<String> planSemesterCombo;
    @FXML private ComboBox<String> planCourseCombo;
    @FXML private ComboBox<String> planTypeCombo;
    @FXML private ComboBox<String> planStatusCombo;
    @FXML private Button planQueryButton;
    @FXML private Button planClearButton;
    @FXML private Button addExamButton;
    @FXML private Button importExamButton;
    @FXML private Button exportExamButton;
    @FXML private Button batchAssignInvigilatorButton;
    @FXML private Button batchAssignRoomButton;
    @FXML private TableView<ExamPlan> examPlanTable;
    @FXML private TableColumn<ExamPlan, Boolean> planSelectCol;
    @FXML private CheckBox planSelectAllCheckbox;
    @FXML private TableColumn<ExamPlan, String> planExamIdCol;
    @FXML private TableColumn<ExamPlan, String> planCourseCol;
    @FXML private TableColumn<ExamPlan, String> planTypeCol;
    @FXML private TableColumn<ExamPlan, String> planDateCol;
    @FXML private TableColumn<ExamPlan, String> planStartTimeCol;
    @FXML private TableColumn<ExamPlan, String> planEndTimeCol;
    @FXML private TableColumn<ExamPlan, String> planRoomCol;
    @FXML private TableColumn<ExamPlan, String> planInvigilatorCol;
    @FXML private TableColumn<ExamPlan, String> planStatusCol;
    @FXML private TableColumn<ExamPlan, ExamPlan> planActionsCol;
    @FXML private Pagination planPagination;
    //</editor-fold>

    //<editor-fold desc="FXML Fields - Exam Approval Tab">
    @FXML private ComboBox<String> approvalStatusCombo;
    @FXML private TextField applicantNameField;
    @FXML private DatePicker approvalStartDatePicker;
    @FXML private DatePicker approvalEndDatePicker;
    @FXML private Button approvalQueryButton;
    @FXML private Button approvalClearButton;
    @FXML private TableView<ExamApproval> examApprovalTable;
    @FXML private TableColumn<ExamApproval, String> approvalIdCol;
    @FXML private TableColumn<ExamApproval, String> approvalCourseCol;
    @FXML private TableColumn<ExamApproval, String> approvalTypeCol;
    @FXML private TableColumn<ExamApproval, String> approvalApplicantCol;
    @FXML private TableColumn<ExamApproval, String> approvalApplyDateCol;
    @FXML private TableColumn<ExamApproval, String> approvalExamDateCol;
    @FXML private TableColumn<ExamApproval, String> approvalStatusCol;
    @FXML private TableColumn<ExamApproval, ExamApproval> approvalActionsCol;
    @FXML private Pagination approvalPagination;
    //</editor-fold>

    private final ObservableList<ExamPlan> examPlanData = FXCollections.observableArrayList();
    private final ObservableList<ExamApproval> examApprovalData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupInfoCards();
        populateFilters();
        setupExamPlanTable();
        setupExamApprovalTable();
        loadInitialData(); // Load sample data and update counts

        // Add listeners/handlers
        planQueryButton.setOnAction(event -> handlePlanQuery());
        planClearButton.setOnAction(event -> handlePlanClear());
        approvalQueryButton.setOnAction(event -> handleApprovalQuery());
        approvalClearButton.setOnAction(event -> handleApprovalClear());
        addExamButton.setOnAction(event -> handleAddExam());
        importExamButton.setOnAction(e -> showPlaceholderAlert("批量导入"));
        exportExamButton.setOnAction(e -> showPlaceholderAlert("导出Excel"));
        batchAssignInvigilatorButton.setOnAction(e -> showPlaceholderAlert("批量安排监考"));
        batchAssignRoomButton.setOnAction(e -> showPlaceholderAlert("批量分配考场"));

        // Configure pagination (basic setup)
        planPagination.setPageCount(3); // Example page count
        planPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) ->
                System.out.println("Exam Plan Page Changed To: " + (newIndex.intValue() + 1)));

        approvalPagination.setPageCount(2); // Example page count
        approvalPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) ->
                System.out.println("Exam Approval Page Changed To: " + (newIndex.intValue() + 1)));

        // Select All Checkbox Logic
        planSelectAllCheckbox.setOnAction(event -> {
            boolean isSelected = planSelectAllCheckbox.isSelected();
            examPlanData.forEach(plan -> plan.setSelected(isSelected));
            examPlanTable.refresh(); // Refresh table view to show checkbox changes
        });

        // Add listener to handle indeterminate state for select all checkbox
        planSelectAllCheckbox.setAllowIndeterminate(true);
        ObservableList<ExamPlan> items = examPlanTable.getItems();
        items.addListener((javafx.collections.ListChangeListener.Change<? extends ExamPlan> c) -> {
            while (c.next()) {
                 // Check if the change involves the 'selected' property update or list modification
                if (c.wasUpdated() || c.wasAdded() || c.wasRemoved()){
                    updateSelectAllCheckboxState();
                    break; // Only need to update once per change event sequence
                 }
            }
        });
         // Initial check in case table loads with mixed selections
         updateSelectAllCheckboxState();
    }

    private void showPlaceholderAlert(String featureName) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("功能提示");
        alert.setHeaderText(null); // No header
        alert.setContentText("功能待实现: " + featureName);
        alert.showAndWait();
    }

    private void setupInfoCards() {
        // Counts will be updated in loadInitialData after data is available
        pendingExamCountLabel.setText("0");
        ongoingExamCountLabel.setText("0");
        thisWeekExamCountLabel.setText("0");
    }

    private void populateFilters() {
        // Exam Plan Filters
        planSemesterCombo.getItems().addAll("2025年春季学期", "2024年秋季学期", "2024年春季学期");
        planCourseCombo.getItems().addAll("全部课程", "高等数学 (II)", "程序设计基础", "数据结构", "计算机网络");
        planTypeCombo.getItems().addAll("全部类型", "期末考试", "期中考试", "补考");
        planStatusCombo.getItems().addAll("全部状态", "未开始", "进行中", "已完成", "已取消");

        planSemesterCombo.getSelectionModel().selectFirst();
        planCourseCombo.getSelectionModel().selectFirst();
        planTypeCombo.getSelectionModel().selectFirst();
        planStatusCombo.getSelectionModel().selectFirst();


        // Exam Approval Filters
        approvalStatusCombo.getItems().addAll("全部状态", "待审批", "已批准", "已拒绝");
        approvalStatusCombo.getSelectionModel().selectFirst();
    }

    private void resetPlanFilters() {
        planSemesterCombo.getSelectionModel().selectFirst();
        planCourseCombo.getSelectionModel().selectFirst();
        planTypeCombo.getSelectionModel().selectFirst();
        planStatusCombo.getSelectionModel().selectFirst();
        // Optionally trigger a query after clearing
        // handlePlanQuery();
    }

    private void resetApprovalFilters() {
        approvalStatusCombo.getSelectionModel().selectFirst();
        applicantNameField.clear();
        approvalStartDatePicker.setValue(null);
        approvalEndDatePicker.setValue(null);
        // Optionally trigger a query after clearing
        // handleApprovalQuery();
    }

    private void setupExamPlanTable() {
        // Add placeholder text
        examPlanTable.setPlaceholder(new Label("没有考试计划数据"));

        // Select Column with CheckBox
        planSelectCol.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        planSelectCol.setCellFactory(column -> new TableCell<ExamPlan, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction(event -> {
                    ExamPlan item = getTableView().getItems().get(getIndex());
                    item.setSelected(checkBox.isSelected());
                });
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item);
                    setGraphic(checkBox);
                    setAlignment(Pos.CENTER);
                }
            }
        });


        planExamIdCol.setCellValueFactory(new PropertyValueFactory<>("examId"));
        planCourseCol.setCellValueFactory(new PropertyValueFactory<>("course"));
        planTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        planDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        planStartTimeCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        planEndTimeCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        planRoomCol.setCellValueFactory(new PropertyValueFactory<>("room"));
        planInvigilatorCol.setCellValueFactory(new PropertyValueFactory<>("invigilator"));

        // Status Column with Styling
        planStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        planStatusCol.setCellFactory(column -> new TableCell<ExamPlan, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("status-pending", "status-ongoing", "status-completed", "status-canceled");
                } else {
                    Label statusLabel = new Label(item);
                    statusLabel.getStyleClass().add(getStatusStyleClass(item));
                    setGraphic(statusLabel);
                    setText(null);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Actions Column with Buttons
        planActionsCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        planActionsCol.setCellFactory(param -> new TableCell<ExamPlan, ExamPlan>() {
            private final Button editButton = new Button("编辑");
            private final Button cancelButton = new Button("取消");
            private final Button endExamButton = new Button("结束考试");
            private final Button registerButton = new Button("异常登记");
            private final Button detailsButton = new Button("查看详情");
            private final Button gradeButton = new Button("成绩录入");
             private final Button restoreButton = new Button("恢复");

            private final HBox pane = new HBox(5); // Spacing 5

            {
                editButton.getStyleClass().add("secondary");
                cancelButton.getStyleClass().add("danger"); // Red for destructive action
                endExamButton.getStyleClass().add("success"); // Green for completion action
                registerButton.getStyleClass().add("warning"); // Orange for attention/issue
                detailsButton.getStyleClass().add("secondary");
                gradeButton.getStyleClass().add("success"); // Green for positive action
                restoreButton.getStyleClass().add("warning"); // Orange for attention action

                editButton.setOnAction(event -> handleEditExam(getItem()));
                cancelButton.setOnAction(event -> handleCancelExam(getItem()));
                endExamButton.setOnAction(event -> handleEndExam(getItem()));
                registerButton.setOnAction(event -> handleRegisterIssue(getItem()));
                detailsButton.setOnAction(event -> handleViewDetails(getItem()));
                gradeButton.setOnAction(event -> handleGradeEntry(getItem()));
                restoreButton.setOnAction(event -> handleRestoreExam(getItem()));
            }

            @Override
            protected void updateItem(ExamPlan item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    pane.getChildren().clear();
                    switch (item.getStatus()) {
                        case "未开始":
                            pane.getChildren().addAll(editButton, cancelButton);
                            break;
                        case "进行中":
                            pane.getChildren().addAll(endExamButton, registerButton);
                            break;
                        case "已完成":
                            pane.getChildren().addAll(detailsButton, gradeButton);
                            break;
                        case "已取消":
                            pane.getChildren().addAll(detailsButton, restoreButton);
                            break;
                        default:
                            // Maybe just details button?
                            pane.getChildren().add(detailsButton);
                            break;
                    }
                    setGraphic(pane);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        examPlanTable.setItems(examPlanData);

        // Make table columns not resizable by default, if desired
        // examPlanTable.getColumns().forEach(col -> col.setResizable(false));
        planActionsCol.setResizable(false); // Action column often best as non-resizable
        planSelectCol.setResizable(false);
        planSelectCol.setSortable(false);
        planActionsCol.setSortable(false);
        examPlanTable.setSortPolicy(tv -> {
            FXCollections.sort(tv.getItems(), tv.getComparator());
            return true;
        });
    }

     private void setupExamApprovalTable() {
        // Add placeholder text
        examApprovalTable.setPlaceholder(new Label("没有考试申请数据"));

        approvalIdCol.setCellValueFactory(new PropertyValueFactory<>("approvalId"));
        approvalCourseCol.setCellValueFactory(new PropertyValueFactory<>("course"));
        approvalTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        approvalApplicantCol.setCellValueFactory(new PropertyValueFactory<>("applicant"));
        approvalApplyDateCol.setCellValueFactory(new PropertyValueFactory<>("applyDate"));
        approvalExamDateCol.setCellValueFactory(new PropertyValueFactory<>("examDate"));

        // Status Column with Styling
        approvalStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        approvalStatusCol.setCellFactory(column -> new TableCell<ExamApproval, String>() {
             @Override
             protected void updateItem(String item, boolean empty) {
                 super.updateItem(item, empty);
                 if (empty || item == null) {
                     setText(null);
                     setGraphic(null);
                     getStyleClass().removeAll("status-reviewing", "status-approved", "status-rejected");
                 } else {
                     Label statusLabel = new Label(item);
                      // Using Label for consistency, could use Badge-like node if needed
                     statusLabel.getStyleClass().add(getStatusStyleClass(item)); // Reuse status style logic
                     setGraphic(statusLabel);
                     setText(null); // Clear text if using graphic
                     setAlignment(Pos.CENTER_LEFT);
                 }
             }
         });

        // Actions Column with Buttons
        approvalActionsCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        approvalActionsCol.setCellFactory(param -> new TableCell<ExamApproval, ExamApproval>() {
            private final Button approveButton = new Button("批准");
            private final Button rejectButton = new Button("拒绝");
            private final Button detailsButton = new Button("详情"); // HTML uses "详情" or "查看详情"

            private final HBox pane = new HBox(5); // Spacing 5

            {
                approveButton.getStyleClass().addAll("button", "success");
                rejectButton.getStyleClass().addAll("button", "danger");
                detailsButton.getStyleClass().addAll("button", "secondary");

                approveButton.setOnAction(event -> handleApproveRequest(getItem()));
                rejectButton.setOnAction(event -> handleRejectRequest(getItem()));
                detailsButton.setOnAction(event -> handleViewApprovalDetails(getItem()));
            }

            @Override
            protected void updateItem(ExamApproval item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    pane.getChildren().clear();
                     if ("待审批".equals(item.getStatus())) {
                         pane.getChildren().addAll(approveButton, rejectButton, detailsButton);
                     } else {
                         pane.getChildren().add(detailsButton); // Only show details for approved/rejected
                     }
                    setGraphic(pane);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        examApprovalTable.setItems(examApprovalData);

        approvalActionsCol.setResizable(false);
        approvalActionsCol.setSortable(false);
        examApprovalTable.setSortPolicy(tv -> {
            FXCollections.sort(tv.getItems(), tv.getComparator());
            return true;
        });
    }


    private void loadInitialData() {
        // Clear existing data first
        examPlanData.clear();
        examApprovalData.clear();

        // Sample Exam Plan Data
        examPlanData.addAll(
                new ExamPlan("EX2025001", "高等数学 (II)", "期末考试", "2025-06-20", "08:30", "10:30", "教学楼A-201", "张教授, 王教授", "未开始"),
                new ExamPlan("EX2025002", "程序设计基础", "期末考试", "2025-06-22", "14:00", "16:00", "教学楼B-305", "李教授, 赵教授", "未开始"),
                new ExamPlan("EX2025003", "数据结构", "期中考试", "2025-05-10", "09:00", "11:00", "教学楼C-401", "刘教授", "进行中"),
                new ExamPlan("EX2025004", "计算机网络", "期中考试", "2025-05-05", "14:00", "16:00", "教学楼A-101", "孙教授", "已完成"),
                new ExamPlan("EX2025005", "操作系统", "补考", "2025-03-15", "10:00", "12:00", "教学楼B-201", "张教授", "已取消")
        );

        // Sample Exam Approval Data
        examApprovalData.addAll(
            new ExamApproval("APP2025001", "计算机图形学", "期末考试", "陈教授", "2025-05-10", "2025-06-25", "待审批"),
            new ExamApproval("APP2025002", "数据库系统", "期中考试", "林教授", "2025-05-08", "2025-05-20", "待审批"),
            new ExamApproval("APP2025003", "软件工程", "期末考试", "张教授", "2025-05-05", "2025-06-15", "已批准"),
            new ExamApproval("APP2025004", "人工智能", "补考", "王教授", "2025-04-28", "2025-05-25", "已拒绝")
        );
         examPlanTable.refresh();
         examApprovalTable.refresh();

         // Update info card counts based on loaded data
         updateInfoCardCounts();
    }

    //<editor-fold desc="Helper Methods">
    private String getStatusStyleClass(String status) {
        if (status == null) return "";
        switch (status) {
            case "未开始": return "status-pending";
            case "进行中": return "status-ongoing";
            case "已完成": return "status-completed";
            case "已取消": return "status-canceled";
            case "待审批": return "status-reviewing"; // Map to reviewing style
            case "已批准": return "status-approved";
            case "已拒绝": return "status-rejected";
            default: return "";
        }
    }

    private void updateInfoCardCounts() {
        long pendingCount = examPlanData.stream().filter(p -> "未开始".equals(p.getStatus())).count();
        long ongoingCount = examPlanData.stream().filter(p -> "进行中".equals(p.getStatus())).count();
        // This week calculation is simplified - needs proper date logic
        long thisWeekCount = examPlanData.stream().filter(p -> {
            try {
                LocalDate examDate = LocalDate.parse(p.getDate());
                LocalDate today = LocalDate.now();
                // Very basic check: same week (this is locale dependent and might need refinement)
                return examDate.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()) ==
                       today.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()) &&
                       examDate.getYear() == today.getYear();
            } catch (Exception e) {
                return false; // Ignore if date parsing fails
            }
        }).count();

        pendingExamCountLabel.setText(String.valueOf(pendingCount));
        ongoingExamCountLabel.setText(String.valueOf(ongoingCount));
        thisWeekExamCountLabel.setText(String.valueOf(thisWeekCount));
    }

    private Button createStyledButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        return button;
    }

    // Updates the state of the "Select All" checkbox based on row selections
    private void updateSelectAllCheckboxState() {
        long selectedCount = examPlanData.stream().filter(ExamPlan::isSelected).count();
        if (selectedCount == 0) {
            planSelectAllCheckbox.setIndeterminate(false);
            planSelectAllCheckbox.setSelected(false);
        } else if (selectedCount == examPlanData.size() && examPlanData.size() > 0) { // Ensure not empty
            planSelectAllCheckbox.setIndeterminate(false);
            planSelectAllCheckbox.setSelected(true);
        } else {
            planSelectAllCheckbox.setIndeterminate(true);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Event Handlers">
    @FXML
    private void handlePlanQuery() {
        String semester = planSemesterCombo.getValue();
        String course = planCourseCombo.getValue();
        String type = planTypeCombo.getValue();
        String status = planStatusCombo.getValue();
        System.out.println("Querying Exam Plans: Semester=" + semester + ", Course=" + course + ", Type=" + type + ", Status=" + status);
        // Add actual filtering logic here
        showPlaceholderAlert("查询考试计划 (查看控制台输出)");
    }

    @FXML
    private void handlePlanClear() {
        System.out.println("Clearing Exam Plan filters.");
        resetPlanFilters();
    }

    @FXML
    private void handleApprovalQuery() {
        String status = approvalStatusCombo.getValue();
        String applicant = applicantNameField.getText();
        LocalDate startDate = approvalStartDatePicker.getValue();
        LocalDate endDate = approvalEndDatePicker.getValue();
        System.out.println("Querying Exam Approvals: Status=" + status + ", Applicant=" + applicant + ", StartDate=" + startDate + ", EndDate=" + endDate);
        // Add actual filtering logic here
        showPlaceholderAlert("查询考试申请 (查看控制台输出)");
    }

     @FXML
    private void handleApprovalClear() {
        System.out.println("Clearing Exam Approval filters.");
        resetApprovalFilters();
    }

     @FXML
    private void handleAddExam() {
        System.out.println("Add New Exam button clicked.");
        // Open a dialog/new window for adding an exam
         // Example: You would typically load a new FXML for the modal
         /*
         try {
             FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/view/teacher/addExamDialog.fxml")); // Adjust path
             Parent root = loader.load();
             AddExamDialogController controller = loader.getController();
             // Pass any necessary initial data to the dialog controller if needed

             Stage dialogStage = new Stage();
             dialogStage.setTitle("新增考试");
             dialogStage.initModality(Modality.WINDOW_MODAL);
             // dialogStage.initOwner(addExamButton.getScene().getWindow()); // Set owner window
             Scene scene = new Scene(root);
             // Add stylesheets if needed: scene.getStylesheets().add(...)
             dialogStage.setScene(scene);

             dialogStage.showAndWait(); // Show dialog and wait for it to close

             // Optionally get results from the dialog controller after it closes
             // ExamPlan newExam = controller.getNewExam();
             // if (newExam != null) {
             //    examPlanData.add(newExam);
             // }

         } catch (IOException e) {
             e.printStackTrace();
             // Show error alert
             Alert alert = new Alert(Alert.AlertType.ERROR, "无法加载新增考试窗口。\n" + e.getMessage());
             alert.showAndWait();
         }
         */
         // Placeholder Alert:
         Alert alert = new Alert(Alert.AlertType.INFORMATION, "功能待实现：打开新增考试窗口。");
         alert.setHeaderText("新增考试");
         alert.showAndWait();
    }

    // --- Action Button Handlers (Placeholders) ---

    private void handleEditExam(ExamPlan item) {
        System.out.println("Edit Exam: " + item.getExamId());
         Alert alert = new Alert(Alert.AlertType.INFORMATION, "功能待实现：编辑考试 " + item.getExamId());
         alert.setHeaderText("编辑考试");
         alert.showAndWait();
    }

    private void handleCancelExam(ExamPlan item) {
        System.out.println("Cancel Exam: " + item.getExamId());
         Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要取消考试 " + item.getExamId() + " 吗？", ButtonType.YES, ButtonType.NO);
         alert.setHeaderText("取消考试");
         alert.showAndWait().ifPresent(response -> {
             if (response == ButtonType.YES) {
                 item.setStatus("已取消"); // Update status locally (in real app, update backend)
                 examPlanTable.refresh(); // Refresh table to show new status and buttons
                 System.out.println("Exam " + item.getExamId() + " cancelled.");
                 // TODO: Add backend call
             }
         });
    }

     private void handleEndExam(ExamPlan item) {
         System.out.println("End Exam: " + item.getExamId());
         Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要结束考试 " + item.getExamId() + " 吗？", ButtonType.YES, ButtonType.NO);
         alert.setHeaderText("结束考试");
         alert.showAndWait().ifPresent(response -> {
             if (response == ButtonType.YES) {
                 item.setStatus("已完成"); // Update status locally
                 examPlanTable.refresh();
                 System.out.println("Exam " + item.getExamId() + " ended.");
                 // TODO: Add backend call
             }
         });
     }

    private void handleRegisterIssue(ExamPlan item) {
        System.out.println("Register Issue for Exam: " + item.getExamId());
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "功能待实现：登记考试异常 " + item.getExamId());
        alert.setHeaderText("异常登记");
        alert.showAndWait();
    }

    private void handleViewDetails(ExamPlan item) {
        System.out.println("View Details for Exam: " + item.getExamId());
         Alert alert = new Alert(Alert.AlertType.INFORMATION,
                 "考试详情:\n" +
                         "ID: " + item.getExamId() + "\n" +
                         "课程: " + item.getCourse() + "\n" +
                         "类型: " + item.getType() + "\n" +
                         "日期: " + item.getDate() + " " + item.getStartTime() + "-" + item.getEndTime() + "\n" +
                         "考场: " + item.getRoom() + "\n" +
                         "监考: " + item.getInvigilator() + "\n" +
                         "状态: " + item.getStatus()
         );
         alert.setHeaderText("考试详情 (" + item.getExamId() + ")");
         alert.showAndWait();
    }

     private void handleGradeEntry(ExamPlan item) {
         System.out.println("Grade Entry for Exam: " + item.getExamId());
         Alert alert = new Alert(Alert.AlertType.INFORMATION, "功能待实现：进入考试成绩录入界面 " + item.getExamId());
         alert.setHeaderText("成绩录入");
         alert.showAndWait();
     }

     private void handleRestoreExam(ExamPlan item) {
         System.out.println("Restore Exam: " + item.getExamId());
         Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要恢复已取消的考试 " + item.getExamId() + " 吗？", ButtonType.YES, ButtonType.NO);
         alert.setHeaderText("恢复考试");
         alert.showAndWait().ifPresent(response -> {
             if (response == ButtonType.YES) {
                 item.setStatus("未开始"); // Or determine appropriate status
                 examPlanTable.refresh();
                 System.out.println("Exam " + item.getExamId() + " restored.");
                 // TODO: Add backend call
             }
         });
     }

     private void handleApproveRequest(ExamApproval item) {
         System.out.println("Approve Request: " + item.getApprovalId());
         Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要批准申请 " + item.getApprovalId() + " 吗？", ButtonType.YES, ButtonType.NO);
         alert.setHeaderText("批准申请");
         alert.showAndWait().ifPresent(response -> {
             if (response == ButtonType.YES) {
                 item.setStatus("已批准"); // Update status locally
                 examApprovalTable.refresh(); // Refresh table to show new status and buttons
                 System.out.println("Request " + item.getApprovalId() + " approved.");
                 // TODO: Add backend call, maybe create an exam plan entry
             }
         });
     }

     private void handleRejectRequest(ExamApproval item) {
         System.out.println("Reject Request: " + item.getApprovalId());
         // Optionally, add a dialog to enter rejection reason
         TextInputDialog dialog = new TextInputDialog();
         dialog.setTitle("拒绝申请");
         dialog.setHeaderText("拒绝考试申请 " + item.getApprovalId());
         dialog.setContentText("请输入拒绝理由:");

         dialog.showAndWait().ifPresent(reason -> {
             item.setStatus("已拒绝"); // Update status locally
             examApprovalTable.refresh(); // Refresh table
             System.out.println("Request " + item.getApprovalId() + " rejected. Reason: " + reason);
             // TODO: Add backend call with reason
         });
     }

     private void handleViewApprovalDetails(ExamApproval item) {
         System.out.println("View Details for Approval: " + item.getApprovalId());
         Alert alert = new Alert(Alert.AlertType.INFORMATION,
                 "申请详情:\n" +
                         "ID: " + item.getApprovalId() + "\n" +
                         "课程: " + item.getCourse() + "\n" +
                         "类型: " + item.getType() + "\n" +
                         "申请人: " + item.getApplicant() + "\n" +
                         "申请日期: " + item.getApplyDate() + "\n" +
                         "期望日期: " + item.getExamDate() + "\n" +
                         "状态: " + item.getStatus()
                 // TODO: Add rejection reason if applicable
         );
         alert.setHeaderText("申请详情 (" + item.getApprovalId() + ")");
         alert.showAndWait();
     }

    //</editor-fold>

    //<editor-fold desc="Data Models">

    /**
     * Data model for the Exam Plan table.
     */
    public static class ExamPlan {
        private final SimpleBooleanProperty selected;
        private final SimpleStringProperty examId;
        private final SimpleStringProperty course;
        private final SimpleStringProperty type;
        private final SimpleStringProperty date;
        private final SimpleStringProperty startTime;
        private final SimpleStringProperty endTime;
        private final SimpleStringProperty room;
        private final SimpleStringProperty invigilator;
        private final SimpleStringProperty status;

        public ExamPlan(String examId, String course, String type, String date, String startTime, String endTime, String room, String invigilator, String status) {
            this.selected = new SimpleBooleanProperty(false);
            this.examId = new SimpleStringProperty(examId);
            this.course = new SimpleStringProperty(course);
            this.type = new SimpleStringProperty(type);
            this.date = new SimpleStringProperty(date);
            this.startTime = new SimpleStringProperty(startTime);
            this.endTime = new SimpleStringProperty(endTime);
            this.room = new SimpleStringProperty(room);
            this.invigilator = new SimpleStringProperty(invigilator);
            this.status = new SimpleStringProperty(status);
        }

        public boolean isSelected() { return selected.get(); }
        public SimpleBooleanProperty selectedProperty() { return selected; }
        public void setSelected(boolean selected) { this.selected.set(selected); }

        public String getExamId() { return examId.get(); }
        public SimpleStringProperty examIdProperty() { return examId; }
        public String getCourse() { return course.get(); }
        public SimpleStringProperty courseProperty() { return course; }
        public String getType() { return type.get(); }
        public SimpleStringProperty typeProperty() { return type; }
        public String getDate() { return date.get(); }
        public SimpleStringProperty dateProperty() { return date; }
        public String getStartTime() { return startTime.get(); }
        public SimpleStringProperty startTimeProperty() { return startTime; }
        public String getEndTime() { return endTime.get(); }
        public SimpleStringProperty endTimeProperty() { return endTime; }
        public String getRoom() { return room.get(); }
        public SimpleStringProperty roomProperty() { return room; }
        public String getInvigilator() { return invigilator.get(); }
        public SimpleStringProperty invigilatorProperty() { return invigilator; }
        public String getStatus() { return status.get(); }
        public SimpleStringProperty statusProperty() { return status; }
         public void setStatus(String status) { this.status.set(status); }
    }

    /**
     * Data model for the Exam Approval table.
     */
    public static class ExamApproval {
        private final SimpleStringProperty approvalId;
        private final SimpleStringProperty course;
        private final SimpleStringProperty type;
        private final SimpleStringProperty applicant;
        private final SimpleStringProperty applyDate;
        private final SimpleStringProperty examDate;
        private final SimpleStringProperty status;

        public ExamApproval(String approvalId, String course, String type, String applicant, String applyDate, String examDate, String status) {
            this.approvalId = new SimpleStringProperty(approvalId);
            this.course = new SimpleStringProperty(course);
            this.type = new SimpleStringProperty(type);
            this.applicant = new SimpleStringProperty(applicant);
            this.applyDate = new SimpleStringProperty(applyDate);
            this.examDate = new SimpleStringProperty(examDate);
            this.status = new SimpleStringProperty(status);
        }

        public String getApprovalId() { return approvalId.get(); }
        public SimpleStringProperty approvalIdProperty() { return approvalId; }
        public String getCourse() { return course.get(); }
        public SimpleStringProperty courseProperty() { return course; }
        public String getType() { return type.get(); }
        public SimpleStringProperty typeProperty() { return type; }
        public String getApplicant() { return applicant.get(); }
        public SimpleStringProperty applicantProperty() { return applicant; }
        public String getApplyDate() { return applyDate.get(); }
        public SimpleStringProperty applyDateProperty() { return applyDate; }
        public String getExamDate() { return examDate.get(); }
        public SimpleStringProperty examDateProperty() { return examDate; }
        public String getStatus() { return status.get(); }
        public SimpleStringProperty statusProperty() { return status; }
         public void setStatus(String status) { this.status.set(status); }
    }
    //</editor-fold>
}
