package com.work.javafx.controller.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TeacherManagementController implements Initializable {

    // FXML Bindings
    @FXML private ScrollPane rootPane;
    @FXML private VBox mainTeachersView;
    @FXML private Label mainPageTitle;
    @FXML private HBox mainTitleContainer;

    // Quick Actions
    @FXML private BorderPane addTeacherCard;
    @FXML private BorderPane importTeacherCard;
    @FXML private BorderPane exportTeacherCard;

    // Search and Filter
    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private ComboBox<String> titleFilter; // Teacher Rank/Title
    @FXML private ComboBox<String> statusFilter;

    // Batch Actions
    @FXML private Button batchResetPasswordBtn;
    @FXML private Button batchDeleteBtn;

    // Teacher Table
    @FXML private TableView<TeacherInfo> teacherTable;
    @FXML private TableColumn<TeacherInfo, String> idColumn; // Employee ID
    @FXML private TableColumn<TeacherInfo, String> nameColumn;
    @FXML private TableColumn<TeacherInfo, String> departmentColumn;
    @FXML private TableColumn<TeacherInfo, String> titleColumn;
    @FXML private TableColumn<TeacherInfo, String> contactColumn; // e.g., Email or Phone
    @FXML private TableColumn<TeacherInfo, String> statusColumn;
    @FXML private TableColumn<TeacherInfo, Void> actionColumn;

    // Pagination
    @FXML private Pagination teacherPagination;
    @FXML private Label pageInfo;

    // Data Storage
    private ObservableList<TeacherInfo> allTeachers = FXCollections.observableArrayList();
    private ObservableList<TeacherInfo> filteredTeachers = FXCollections.observableArrayList();

    // Pagination Parameters
    private final int ROWS_PER_PAGE = 10;
    private int totalPages = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initFilters();
        loadData(); // Load mock data
        initTeacherTable();
        initPagination();
    }

    private void initFilters() {
        departmentFilter.setItems(FXCollections.observableArrayList(
                "全部院系", "计算机学院", "数学学院", "物理学院", "外语学院", "经济管理学院"
        ));
        departmentFilter.getSelectionModel().selectFirst();

        titleFilter.setItems(FXCollections.observableArrayList(
                "全部职称", "助教", "讲师", "副教授", "教授"
        ));
        titleFilter.getSelectionModel().selectFirst();

        statusFilter.setItems(FXCollections.observableArrayList(
                "全部状态", "在职", "休假", "离职"
        ));
        statusFilter.getSelectionModel().selectFirst();

        // Add listeners
        departmentFilter.setOnAction(e -> applyFilters());
        titleFilter.setOnAction(e -> applyFilters());
        statusFilter.setOnAction(e -> applyFilters());
    }

    private void initTeacherTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));

        // Status column styling
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<TeacherInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Label statusLabel = new Label(item);
                    statusLabel.getStyleClass().add("status-badge");
                    switch (item) {
                        case "在职":
                            statusLabel.getStyleClass().add("status-active");
                            break;
                        case "休假":
                            statusLabel.getStyleClass().add("status-pending"); // Use pending style for on leave
                            break;
                        case "离职":
                            statusLabel.getStyleClass().add("status-inactive");
                            break;
                    }
                    setGraphic(statusLabel);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        actionColumn.setCellFactory(createActionCellFactory());
        teacherTable.setItems(filteredTeachers);
    }

    private Callback<TableColumn<TeacherInfo, Void>, TableCell<TeacherInfo, Void>> createActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<TeacherInfo, Void> call(TableColumn<TeacherInfo, Void> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button();
                    private final Button editBtn = new Button();
                    private final Button resetPassBtn = new Button(); // Reset Password specific button
                    private final Button deleteBtn = new Button();

                    // Using HBox for layout flexibility
                    private final HBox actionBox = new HBox(5);

                    {
                        // Add icons and tooltips
                        Region viewIcon = new Region(); viewIcon.getStyleClass().add("view-icon"); viewBtn.setGraphic(viewIcon);
                        viewBtn.setTooltip(new Tooltip("查看详情"));
                        viewBtn.getStyleClass().addAll("table-button", "default-btn");

                        Region editIcon = new Region(); editIcon.getStyleClass().add("edit-icon"); editBtn.setGraphic(editIcon);
                        editBtn.setTooltip(new Tooltip("编辑信息"));
                        editBtn.getStyleClass().addAll("table-button", "warning-btn");

                        Region resetIcon = new Region(); resetIcon.getStyleClass().add("approval-icon"); resetPassBtn.setGraphic(resetIcon); // Reuse approval icon
                        resetPassBtn.setTooltip(new Tooltip("重置密码"));
                        resetPassBtn.getStyleClass().addAll("table-button", "success-btn");

                        Region deleteIcon = new Region(); deleteIcon.getStyleClass().add("delete-icon"); deleteBtn.setGraphic(deleteIcon);
                        deleteBtn.setTooltip(new Tooltip("删除教师"));
                        deleteBtn.getStyleClass().addAll("table-button", "danger-btn");

                        // Adjust buttons based on teacher status if needed in updateItem
                        actionBox.getChildren().addAll(viewBtn, editBtn, resetPassBtn, deleteBtn);
                        actionBox.setAlignment(Pos.CENTER);

                        // Set actions
                        viewBtn.setOnAction(event -> viewTeacher(getIndex()));
                        editBtn.setOnAction(event -> editTeacher(getIndex()));
                        resetPassBtn.setOnAction(event -> resetPassword(getIndex()));
                        deleteBtn.setOnAction(event -> deleteTeacher(getIndex()));
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // Example: Disable delete/edit for inactive teachers
                            // TeacherInfo teacher = getTableView().getItems().get(getIndex());
                            // boolean isInactive = "离职".equals(teacher.getStatus());
                            // editBtn.setDisable(isInactive);
                            // deleteBtn.setDisable(isInactive);
                            // resetPassBtn.setDisable(isInactive);
                            setGraphic(actionBox);
                        }
                    }
                };
            }
        };
    }

    private void initPagination() {
        totalPages = (int) Math.ceil((double) filteredTeachers.size() / ROWS_PER_PAGE);
        teacherPagination.setPageCount(totalPages > 0 ? totalPages : 1);
        teacherPagination.setCurrentPageIndex(0);
        teacherPagination.setPageFactory(this::createTeacherPage);
        updatePageInfo();
    }

    private Node createTeacherPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredTeachers.size());
        teacherTable.setItems(FXCollections.observableArrayList(
                filteredTeachers.subList(fromIndex, toIndex)));
        return new VBox(teacherTable); // Or just return null if table updates are sufficient
    }

    private void updatePageInfo() {
        int currentPage = teacherPagination.getCurrentPageIndex();
        int fromIndex = currentPage * ROWS_PER_PAGE + 1;
        int toIndex = Math.min((currentPage + 1) * ROWS_PER_PAGE, filteredTeachers.size());

        if (filteredTeachers.isEmpty()) {
            pageInfo.setText("共 0 条记录");
        } else {
            pageInfo.setText(String.format("共 %d 条记录，当前显示 %d-%d 条",
                    filteredTeachers.size(), fromIndex, toIndex));
        }
    }

    private void loadData() {
        // Mock data - Replace with actual API call
        allTeachers.addAll(
                new TeacherInfo("T001", "张三", "计算机学院", "教授", "zhangsan@example.com", "在职"),
                new TeacherInfo("T002", "李四", "计算机学院", "副教授", "lisi@example.com", "在职"),
                new TeacherInfo("T003", "王五", "数学学院", "讲师", "wangwu@example.com", "在职"),
                new TeacherInfo("T004", "赵六", "物理学院", "教授", "zhaoliu@example.com", "在职"),
                new TeacherInfo("T005", "孙七", "外语学院", "讲师", "sunqi@example.com", "休假"),
                new TeacherInfo("T006", "周八", "经济管理学院", "副教授", "zhouba@example.com", "在职"),
                new TeacherInfo("T007", "吴九", "计算机学院", "讲师", "wujiu@example.com", "在职"),
                new TeacherInfo("T008", "郑十", "数学学院", "助教", "zhengshi@example.com", "在职"),
                new TeacherInfo("T009", "冯十一", "物理学院", "副教授", "fengshiyi@example.com", "离职"),
                new TeacherInfo("T010", "陈十二", "计算机学院", "教授", "chenshier@example.com", "在职"),
                new TeacherInfo("T011", "卫十三", "经济管理学院", "讲师", "weishisan@example.com", "在职")
        );
        filteredTeachers.addAll(allTeachers);
    }

    // Event Handlers
    @FXML
    private void searchTeachers() {
        applyFiltersAndSearch(searchField.getText().toLowerCase().trim());
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        departmentFilter.getSelectionModel().selectFirst();
        titleFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();
        applyFiltersAndSearch("");
    }

    private void applyFilters() {
        applyFiltersAndSearch(searchField.getText().toLowerCase().trim());
    }

    private void applyFiltersAndSearch(String searchTerm) {
        filteredTeachers.clear();

        String department = departmentFilter.getValue().equals("全部院系") ? "" : departmentFilter.getValue();
        String title = titleFilter.getValue().equals("全部职称") ? "" : titleFilter.getValue();
        String status = statusFilter.getValue().equals("全部状态") ? "" : statusFilter.getValue();

        for (TeacherInfo teacher : allTeachers) {
            boolean departmentMatch = department.isEmpty() || teacher.getDepartment().equals(department);
            boolean titleMatch = title.isEmpty() || teacher.getTitle().equals(title);
            boolean statusMatch = status.isEmpty() || teacher.getStatus().equals(status);

            boolean searchMatch = searchTerm.isEmpty() ||
                    teacher.getId().toLowerCase().contains(searchTerm) ||
                    teacher.getName().toLowerCase().contains(searchTerm) ||
                    teacher.getDepartment().toLowerCase().contains(searchTerm);

            if (departmentMatch && titleMatch && statusMatch && searchMatch) {
                filteredTeachers.add(teacher);
            }
        }

        // Update pagination and table view
        totalPages = (int) Math.ceil((double) filteredTeachers.size() / ROWS_PER_PAGE);
        teacherPagination.setPageCount(totalPages > 0 ? totalPages : 1);
        teacherPagination.setCurrentPageIndex(0);
        updatePageInfo();
        createTeacherPage(0); // Load the first page data into the table
    }

    // Quick Actions
    @FXML
    private void showAddTeacherView(javafx.scene.input.MouseEvent event) {
        System.out.println("Add Teacher Clicked");
        showInfoDialog("功能开发中", "新增教师功能待实现。");
    }

    @FXML
    private void importTeachers(javafx.scene.input.MouseEvent event) {
        System.out.println("Import Teachers Clicked");
        // Implement file chooser and import logic
        showInfoDialog("功能开发中", "批量导入功能待实现。");
    }

    @FXML
    private void exportTeachers(javafx.scene.input.MouseEvent event) {
        System.out.println("Export Teachers Clicked");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出教师数据");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel文件", "*.xlsx"));
        fileChooser.setInitialFileName("教师数据.xlsx");

        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file != null) {
            // Implement export logic
            System.out.println("Exporting to: " + file.getAbsolutePath());
            showInfoDialog("导出成功", "教师数据已成功导出到: " + file.getAbsolutePath());
        }
    }

    // Batch Actions
    @FXML
    private void batchResetPassword() {
        System.out.println("Batch Reset Password Clicked");
        // Get selected teachers and implement reset logic
        showInfoDialog("功能开发中", "批量重置密码功能待实现。");
    }

    @FXML
    private void batchDeleteTeachers() {
        System.out.println("Batch Delete Clicked");
        // Get selected teachers and implement deletion logic
        showInfoDialog("功能开发中", "批量删除功能待实现。");
    }

    // Table Actions
    private void viewTeacher(int index) {
        if (index < 0 || index >= teacherTable.getItems().size()) return;
        TeacherInfo selectedTeacher = teacherTable.getItems().get(index);
        System.out.println("View Teacher: " + selectedTeacher.getName());
        showInfoDialog("查看教师", "查看教师详情: " + selectedTeacher.getName() + "\n(详情页面待实现)");
    }

    private void editTeacher(int index) {
        if (index < 0 || index >= teacherTable.getItems().size()) return;
        TeacherInfo selectedTeacher = teacherTable.getItems().get(index);
        System.out.println("Edit Teacher: " + selectedTeacher.getName());
        showInfoDialog("编辑教师", "编辑教师信息: " + selectedTeacher.getName() + "\n(编辑功能待实现)");
    }

    private void resetPassword(int index) {
        if (index < 0 || index >= teacherTable.getItems().size()) return;
        TeacherInfo selectedTeacher = teacherTable.getItems().get(index);
        System.out.println("Reset Password for: " + selectedTeacher.getName());
        if (showConfirmDialog("确认操作", "确定要重置教师 " + selectedTeacher.getName() + " 的密码吗？")) {
            // Implement password reset logic (e.g., call API)
            showInfoDialog("操作成功", "已重置教师 " + selectedTeacher.getName() + " 的密码。");
        }
    }

    private void deleteTeacher(int index) {
        if (index < 0 || index >= teacherTable.getItems().size()) return;
        TeacherInfo selectedTeacher = teacherTable.getItems().get(index);
        System.out.println("Delete Teacher: " + selectedTeacher.getName());
        if (showConfirmDialog("确认删除", "确定要删除教师 " + selectedTeacher.getName() + " 吗？\n此操作将同时删除关联的账户信息，且可能无法恢复。")) {
            // Implement deletion logic (API call, update lists)
            allTeachers.remove(selectedTeacher);
            applyFiltersAndSearch(searchField.getText().toLowerCase().trim()); // Refresh view
            showInfoDialog("操作成功", "已删除教师: " + selectedTeacher.getName());
        }
    }

    // Helper Dialog Methods
    private void showInfoDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean showConfirmDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    // Inner class for Teacher data model
    public static class TeacherInfo {
        private final SimpleStringProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty department;
        private final SimpleStringProperty title;
        private final SimpleStringProperty contact;
        private final SimpleStringProperty status;
        // private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

        public TeacherInfo(String id, String name, String department, String title, String contact, String status) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.department = new SimpleStringProperty(department);
            this.title = new SimpleStringProperty(title);
            this.contact = new SimpleStringProperty(contact);
            this.status = new SimpleStringProperty(status);
        }

        // Getters
        public String getId() { return id.get(); }
        public SimpleStringProperty idProperty() { return id; }
        public String getName() { return name.get(); }
        public SimpleStringProperty nameProperty() { return name; }
        public String getDepartment() { return department.get(); }
        public SimpleStringProperty departmentProperty() { return department; }
        public String getTitle() { return title.get(); }
        public SimpleStringProperty titleProperty() { return title; }
        public String getContact() { return contact.get(); }
        public SimpleStringProperty contactProperty() { return contact; }
        public String getStatus() { return status.get(); }
        public SimpleStringProperty statusProperty() { return status; }
        // public boolean isSelected() { return selected.get(); }
        // public SimpleBooleanProperty selectedProperty() { return selected; }
        // public void setSelected(boolean selected) { this.selected.set(selected); }
    }
}
