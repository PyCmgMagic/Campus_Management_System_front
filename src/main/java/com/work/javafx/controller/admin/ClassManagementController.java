package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.work.javafx.util.NetworkUtils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ClassManagementController implements Initializable {

    // FXML Bindings
    @FXML private ScrollPane rootPane;
    @FXML private VBox mainClassesView;
    @FXML private Label mainPageTitle;
    @FXML private HBox mainTitleContainer;

    // Quick Actions
    @FXML private BorderPane addClassCard;
    @FXML private BorderPane batchManageCard;
    @FXML private BorderPane exportClassCard;

    // Search and Filter
    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private ComboBox<String> gradeFilter;
    @FXML private ComboBox<String> statusFilter;

    // Batch Actions
    @FXML private Button batchDeleteBtn; // Example batch button

    // Class Table
    @FXML private TableView<ClassInfo> classTable;
    @FXML private TableColumn<ClassInfo, String> idColumn;
    @FXML private TableColumn<ClassInfo, String> nameColumn;
    @FXML private TableColumn<ClassInfo, String> departmentColumn;
    @FXML private TableColumn<ClassInfo, String> gradeColumn;
    @FXML private TableColumn<ClassInfo, String> counselorColumn;
    @FXML private TableColumn<ClassInfo, Integer> studentCountColumn;
    @FXML private TableColumn<ClassInfo, String> statusColumn;
    @FXML private TableColumn<ClassInfo, Void> actionColumn;

    // Pagination
    @FXML private Pagination classPagination;
    @FXML private Label pageInfo;

    // Data Storage
    private ObservableList<ClassInfo> allClassInfo = FXCollections.observableArrayList();
    private ObservableList<ClassInfo> filteredClassInfo = FXCollections.observableArrayList();

    // Pagination Parameters
    private final int ROWS_PER_PAGE = 10;
    private int totalPages = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initFilters();
        loadData(); // Load mock data for now
        initClassTable();
        initPagination();
    }

    private void initFilters() {
        departmentFilter.setItems(FXCollections.observableArrayList(
                "全部院系", "计算机学院", "数学学院", "物理学院", "外语学院", "经济管理学院"
        ));
        departmentFilter.getSelectionModel().selectFirst();

        gradeFilter.setItems(FXCollections.observableArrayList(
                "全部年级", "2021级", "2022级", "2023级", "2024级"
        ));
        gradeFilter.getSelectionModel().selectFirst();

        statusFilter.setItems(FXCollections.observableArrayList(
                "全部状态", "正常", "已毕业", "已撤销"
        ));
        statusFilter.getSelectionModel().selectFirst();

        // Add listeners to apply filters when selection changes
        departmentFilter.setOnAction(e -> applyFilters());
        gradeFilter.setOnAction(e -> applyFilters());
        statusFilter.setOnAction(e -> applyFilters());
    }

    private void initClassTable() {
        // Set cell value factories for each column
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
        counselorColumn.setCellValueFactory(new PropertyValueFactory<>("counselor"));
        studentCountColumn.setCellValueFactory(new PropertyValueFactory<>("studentCount"));

        // Custom cell factory for status column
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<ClassInfo, String>() {
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
                    // Apply different styles based on status
                    switch (item) {
                        case "正常":
                            statusLabel.getStyleClass().add("status-active"); // Reuse active style
                            break;
                        case "已毕业":
                        case "已撤销":
                            statusLabel.getStyleClass().add("status-inactive"); // Reuse inactive style
                            break;
                        default:
                            statusLabel.getStyleClass().add("status-pending"); // Reuse pending style for other cases
                            break;
                    }
                    setGraphic(statusLabel);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Add action column cell factory
        actionColumn.setCellFactory(createActionCellFactory());

        // Set initial data
        classTable.setItems(filteredClassInfo);
    }

    private Callback<TableColumn<ClassInfo, Void>, TableCell<ClassInfo, Void>> createActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<ClassInfo, Void> call(TableColumn<ClassInfo, Void> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button();
                    private final Button editBtn = new Button();
                    private final Button deleteBtn = new Button(); // Or archive/graduate
                    private final HBox actionBox = new HBox(5, viewBtn, editBtn, deleteBtn);

                    {
                        viewBtn.getStyleClass().addAll("table-button", "default-btn");
                        editBtn.getStyleClass().addAll("table-button", "warning-btn");
                        deleteBtn.getStyleClass().addAll("table-button", "danger-btn");

                        // Add icons (reuse from course management css)
                        Region viewIcon = new Region(); viewIcon.getStyleClass().add("view-icon"); viewBtn.setGraphic(viewIcon);
                        Region editIcon = new Region(); editIcon.getStyleClass().add("edit-icon"); editBtn.setGraphic(editIcon);
                        Region deleteIcon = new Region(); deleteIcon.getStyleClass().add("delete-icon"); deleteBtn.setGraphic(deleteIcon);

                        // Set tooltips
                        viewBtn.setTooltip(new Tooltip("查看详情"));
                        editBtn.setTooltip(new Tooltip("编辑班级"));
                        deleteBtn.setTooltip(new Tooltip("删除班级"));

                        // Set actions
                        viewBtn.setOnAction(event -> viewClass(getIndex()));
                        editBtn.setOnAction(event -> editClass(getIndex()));
                        deleteBtn.setOnAction(event -> deleteClass(getIndex()));

                        actionBox.setAlignment(Pos.CENTER);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(actionBox);
                        }
                    }
                };
            }
        };
    }

    private void initPagination() {
        totalPages = (int) Math.ceil((double) filteredClassInfo.size() / ROWS_PER_PAGE);
        classPagination.setPageCount(totalPages > 0 ? totalPages : 1);
        classPagination.setCurrentPageIndex(0);
        classPagination.setPageFactory(this::createClassPage);
        updatePageInfo();
    }

    private Node createClassPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredClassInfo.size());
        classTable.setItems(FXCollections.observableArrayList(
                filteredClassInfo.subList(fromIndex, toIndex)));
        return new VBox(classTable); // Return the table itself or a container
    }

    private void updatePageInfo() {
        int currentPage = classPagination.getCurrentPageIndex();
        int fromIndex = currentPage * ROWS_PER_PAGE + 1;
        int toIndex = Math.min((currentPage + 1) * ROWS_PER_PAGE, filteredClassInfo.size());

        if (filteredClassInfo.isEmpty()) {
            pageInfo.setText("共 0 条记录");
        } else {
            pageInfo.setText(String.format("共 %d 条记录，当前显示 %d-%d 条",
                    filteredClassInfo.size(), fromIndex, toIndex));
        }
    }

    private void loadData() {
        Map<String,String> param = new HashMap<>();
        param.put("page",classPagination.getCurrentPageIndex()+"");
        param.put("size","10");
        NetworkUtils.get("/section/getSectionListAll", param, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                Gson gson = new Gson();
                JsonObject res = gson.fromJson(result,JsonObject.class);
                if(res.has("code") && res.get("code").getAsInt() == 200){
                    JsonArray data = res.getAsJsonArray("data");

                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
        allClassInfo.addAll(
                new ClassInfo("BJ001", "软件工程2101", "计算机学院", "2021级", "张老师", 55, "正常"),
                new ClassInfo("BJ002", "网络工程2101", "计算机学院", "2021级", "李老师", 50, "正常"),
                new ClassInfo("BJ003", "数学与应用数学2201", "数学学院", "2022级", "王老师", 60, "正常"),
                new ClassInfo("BJ004", "物理学2201", "物理学院", "2022级", "赵老师", 45, "正常"),
                new ClassInfo("BJ005", "英语2301", "外语学院", "2023级", "钱老师", 65, "正常"),
                new ClassInfo("BJ006", "工商管理2001", "经济管理学院", "2020级", "孙老师", 58, "已毕业"),
                new ClassInfo("BJ007", "计算机科学与技术2102", "计算机学院", "2021级", "周老师", 52, "正常"),
                new ClassInfo("BJ008", "数据科学与大数据技术2201", "计算机学院", "2022级", "吴老师", 48, "正常"),
                new ClassInfo("BJ009", "金融学2301", "经济管理学院", "2023级", "郑老师", 62, "正常"),
                new ClassInfo("BJ010", "软件工程1901", "计算机学院", "2019级", "冯老师", 53, "已毕业"),
                new ClassInfo("BJ011", "网络工程2001", "计算机学院", "2020级", "陈老师", 51, "已毕业")
        );
        filteredClassInfo.addAll(allClassInfo);
    }

    // Event Handlers

    @FXML
    private void searchClasses() {
        String searchTerm = searchField.getText().toLowerCase().trim();
        applyFiltersAndSearch(searchTerm);
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        departmentFilter.getSelectionModel().selectFirst();
        gradeFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();
        applyFiltersAndSearch("");
    }

    private void applyFilters() {
        applyFiltersAndSearch(searchField.getText().toLowerCase().trim());
    }

    private void applyFiltersAndSearch(String searchTerm) {
        filteredClassInfo.clear();

        String department = departmentFilter.getValue().equals("全部院系") ? "" : departmentFilter.getValue();
        String grade = gradeFilter.getValue().equals("全部年级") ? "" : gradeFilter.getValue();
        String status = statusFilter.getValue().equals("全部状态") ? "" : statusFilter.getValue();

        for (ClassInfo classInfo : allClassInfo) {
            boolean departmentMatch = department.isEmpty() || classInfo.getDepartment().equals(department);
            boolean gradeMatch = grade.isEmpty() || classInfo.getGrade().equals(grade);
            boolean statusMatch = status.isEmpty() || classInfo.getStatus().equals(status);

            boolean searchMatch = searchTerm.isEmpty() ||
                    classInfo.getId().toLowerCase().contains(searchTerm) ||
                    classInfo.getName().toLowerCase().contains(searchTerm) ||
                    classInfo.getCounselor().toLowerCase().contains(searchTerm);

            if (departmentMatch && gradeMatch && statusMatch && searchMatch) {
                filteredClassInfo.add(classInfo);
            }
        }

        // Update pagination
        totalPages = (int) Math.ceil((double) filteredClassInfo.size() / ROWS_PER_PAGE);
        classPagination.setPageCount(totalPages > 0 ? totalPages : 1);
        classPagination.setCurrentPageIndex(0); // Go back to first page
        updatePageInfo();
        // Ensure the table updates with the first page data
        createClassPage(0);
    }

    @FXML
    private void showAddClassView(javafx.scene.input.MouseEvent event) {
        System.out.println("Add Class Clicked");
        // Implement logic to show an add class dialog or view
        showInfoDialog("功能开发中", "新增班级功能将在后续版本开放。");
    }

    @FXML
    private void batchManageClasses(javafx.scene.input.MouseEvent event) {
        System.out.println("Batch Manage Clicked");
        // Implement logic for batch management
        showInfoDialog("功能开发中", "批量管理功能将在后续版本开放。");
    }

    @FXML
    private void exportClasses(javafx.scene.input.MouseEvent event) {
        System.out.println("Export Classes Clicked");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出班级数据");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel文件", "*.xlsx"));
        fileChooser.setInitialFileName("班级数据.xlsx");

        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file != null) {
            // Implement actual export logic here (e.g., using Apache POI)
            System.out.println("Exporting to: " + file.getAbsolutePath());
            showInfoDialog("导出成功", "班级数据已成功导出到: " + file.getAbsolutePath());
        }
    }

    @FXML
    private void batchDeleteClasses() {
        System.out.println("Batch Delete Clicked");
        // Implement logic to get selected items and delete them
        // List<ClassInfo> selected = classTable.getItems().stream().filter(ClassInfo::isSelected).collect(Collectors.toList());
        // if (selected.isEmpty()) { ... }
        showInfoDialog("功能开发中", "批量删除功能将在后续版本开放。");
    }

    // Action Column Methods
    private void viewClass(int index) {
        if (index < 0 || index >= classTable.getItems().size()) return;
        ClassInfo selectedClass = classTable.getItems().get(index);
        System.out.println("View Class: " + selectedClass.getName());
        // Implement logic to show class details view
        showInfoDialog("查看班级", "查看班级: " + selectedClass.getName() + "\n(详情页面待实现)");
    }

    private void editClass(int index) {
        if (index < 0 || index >= classTable.getItems().size()) return;
        ClassInfo selectedClass = classTable.getItems().get(index);
        System.out.println("Edit Class: " + selectedClass.getName());
        // Implement logic to show edit class dialog/view
        showInfoDialog("编辑班级", "编辑班级: " + selectedClass.getName() + "\n(编辑功能待实现)");
    }

    private void deleteClass(int index) {
        if (index < 0 || index >= classTable.getItems().size()) return;
        ClassInfo selectedClass = classTable.getItems().get(index);
        System.out.println("Delete Class: " + selectedClass.getName());
        if (showConfirmDialog("确认删除", "确定要删除班级 " + selectedClass.getName() + " 吗？\n此操作可能无法恢复。")) {
            // Implement actual deletion logic (remove from allClassInfo and filteredClassInfo)
            allClassInfo.remove(selectedClass);
            // Re-apply filters and search to update the view correctly
            applyFiltersAndSearch(searchField.getText().toLowerCase().trim());
            showInfoDialog("操作成功", "已删除班级: " + selectedClass.getName());
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

    // Inner class for Class data model (replace with your actual model if exists)
    public static class ClassInfo {
        private final SimpleStringProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty department;
        private final SimpleStringProperty grade;
        private final SimpleStringProperty counselor;
        private final SimpleIntegerProperty studentCount;
        private final SimpleStringProperty status;
        // Add a selected property if needed for batch actions
        // private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

        public ClassInfo(String id, String name, String department, String grade, String counselor, int studentCount, String status) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.department = new SimpleStringProperty(department);
            this.grade = new SimpleStringProperty(grade);
            this.counselor = new SimpleStringProperty(counselor);
            this.studentCount = new SimpleIntegerProperty(studentCount);
            this.status = new SimpleStringProperty(status);
        }

        // Getters (required for PropertyValueFactory)
        public String getId() { return id.get(); }
        public SimpleStringProperty idProperty() { return id; }
        public String getName() { return name.get(); }
        public SimpleStringProperty nameProperty() { return name; }
        public String getDepartment() { return department.get(); }
        public SimpleStringProperty departmentProperty() { return department; }
        public String getGrade() { return grade.get(); }
        public SimpleStringProperty gradeProperty() { return grade; }
        public String getCounselor() { return counselor.get(); }
        public SimpleStringProperty counselorProperty() { return counselor; }
        public int getStudentCount() { return studentCount.get(); }
        public SimpleIntegerProperty studentCountProperty() { return studentCount; }
        public String getStatus() { return status.get(); }
        public SimpleStringProperty statusProperty() { return status; }
        // public boolean isSelected() { return selected.get(); }
        // public SimpleBooleanProperty selectedProperty() { return selected; }
        // public void setSelected(boolean selected) { this.selected.set(selected); }
    }
}