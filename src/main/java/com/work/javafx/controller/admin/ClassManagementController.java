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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.application.Platform;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ClassManagementController implements Initializable {

    @FXML private ScrollPane rootPane;
    @FXML private VBox mainClassesView;
    @FXML private Label mainPageTitle;
    @FXML private HBox mainTitleContainer;

    @FXML private BorderPane addClassCard;
    @FXML private BorderPane batchManageCard;
    @FXML private BorderPane exportClassCard;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> gradeFilter;

    @FXML private Button batchDeleteBtn;

    @FXML private TableView<ClassInfo> classTable;
    @FXML private TableColumn<ClassInfo, String> idColumn;
    @FXML private TableColumn<ClassInfo, String> nameColumn;
    @FXML private TableColumn<ClassInfo, String> departmentColumn;
    @FXML private TableColumn<ClassInfo, String> gradeColumn;
    @FXML private TableColumn<ClassInfo, String> counselorColumn;
    @FXML private TableColumn<ClassInfo, Integer> studentCountColumn;
    @FXML private TableColumn<ClassInfo, String> statusColumn;
    @FXML private TableColumn<ClassInfo, Void> actionColumn;

    @FXML private Pagination classPagination;
    @FXML private Label pageInfo;

    private ObservableList<ClassInfo> filteredClassInfo = FXCollections.observableArrayList();

    private final int ROWS_PER_PAGE = 10;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initFilters();
        initClassTable();
        initPagination();
        loadTableData(0);
    }

    private void initFilters() {

        gradeFilter.setItems(FXCollections.observableArrayList(
                "全部年级", "2021级", "2022级", "2023级", "2024级"
        ));
        gradeFilter.getSelectionModel().selectFirst();

        gradeFilter.setOnAction(e -> applyFilters());
    }

    private void initClassTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
        counselorColumn.setCellValueFactory(new PropertyValueFactory<>("counselor"));
        studentCountColumn.setCellValueFactory(new PropertyValueFactory<>("studentCount"));

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
                    switch (item) {
                        case "正常":
                            statusLabel.getStyleClass().add("status-active");
                            break;
                        case "已毕业":
                        case "已撤销":
                            statusLabel.getStyleClass().add("status-inactive");
                            break;
                        default:
                            statusLabel.getStyleClass().add("status-pending");
                            break;
                    }
                    setGraphic(statusLabel);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        actionColumn.setCellFactory(createActionCellFactory());

        classTable.setItems(filteredClassInfo);
    }

    private Callback<TableColumn<ClassInfo, Void>, TableCell<ClassInfo, Void>> createActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<ClassInfo, Void> call(TableColumn<ClassInfo, Void> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button();
                    private final Button editBtn = new Button();
                    private final Button deleteBtn = new Button();
                    private final HBox actionBox = new HBox(5, viewBtn, editBtn, deleteBtn);

                    {
                        viewBtn.getStyleClass().addAll("table-button", "default-btn");
                        editBtn.getStyleClass().addAll("table-button", "warning-btn");
                        deleteBtn.getStyleClass().addAll("table-button", "danger-btn");

                        Region viewIcon = new Region(); viewIcon.getStyleClass().add("view-icon"); viewBtn.setGraphic(viewIcon);
                        Region editIcon = new Region(); editIcon.getStyleClass().add("edit-icon"); editBtn.setGraphic(editIcon);
                        Region deleteIcon = new Region(); deleteIcon.getStyleClass().add("delete-icon"); deleteBtn.setGraphic(deleteIcon);

                        viewBtn.setTooltip(new Tooltip("查看详情"));
                        editBtn.setTooltip(new Tooltip("编辑班级"));
                        deleteBtn.setTooltip(new Tooltip("删除班级"));

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
                            int currentIndex = getTableRow() != null ? getTableRow().getIndex() : -1;
                            if (currentIndex >= 0 && currentIndex < filteredClassInfo.size()) {
                                setGraphic(actionBox);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        };
    }

    private void initPagination() {
        classPagination.setCurrentPageIndex(0);
        classPagination.setPageFactory(this::createClassPage);
        
        classPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            if (newIndex.intValue() != oldIndex.intValue()) {
                loadTableData(newIndex.intValue());
            }
        });
    }

    private Node createClassPage(int pageIndex) {
        return new VBox();
    }

    private void updatePageInfo() {
        int currentPage = classPagination.getCurrentPageIndex();
        int itemsOnCurrentPage = filteredClassInfo.size();
        int totalPages = classPagination.getPageCount();

        if (itemsOnCurrentPage == 0) {
            if (currentPage == 0 && totalPages <= 1) {
                 pageInfo.setText("共 0 条记录");
            } else {
                pageInfo.setText("当前页无记录");
            }
        } else {
            int fromIndex = currentPage * ROWS_PER_PAGE + 1;
            int toIndex = fromIndex + itemsOnCurrentPage - 1;
            pageInfo.setText(String.format("当前显示 %d-%d 条 (共 %d 页)", fromIndex, toIndex, totalPages));
        }
    }

    private void loadTableData(int pageIndex) {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(pageIndex + 1));
        params.put("size", String.valueOf(ROWS_PER_PAGE));

        String selectedGrade = gradeFilter.getValue();
        if (selectedGrade != null && !selectedGrade.equals("全部年级")) {
            String gradeValue = selectedGrade.replace("级", "").trim();
            if (!gradeValue.isEmpty()) {
                params.put("grade", gradeValue);
            }
            NetworkUtils.get("/section/getSectionList", params, new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    Platform.runLater(() -> {
                        try {
                            Gson gson = new Gson();
                            JsonObject response = gson.fromJson(result, JsonObject.class);

                            if (response.has("code") && response.get("code").getAsInt() == 200) {
                                int totalPages = 1;
                                int totalRecords = 0;

                                JsonArray dataArray = null;
                                if (response.has("data") && response.get("data").isJsonObject()) {
                                    JsonObject dataObject = response.getAsJsonObject("data");

                                    if (dataObject.has("pages")) {
                                        totalPages = dataObject.get("pages").getAsInt();
                                    } else if (dataObject.has("page")) {
                                        totalPages = dataObject.get("page").getAsInt();
                                    }

                                    if (dataObject.has("total")) {
                                        try{
                                            if (!dataObject.get("total").isJsonNull()) {
                                                totalRecords = dataObject.get("total").getAsInt();
                                            }
                                        } catch (Exception e) {
                                        }
                                    }

                                    if (dataObject.has("section") && dataObject.get("section").isJsonArray()) {
                                        dataArray = dataObject.getAsJsonArray("section");
                                    } else {
                                        filteredClassInfo.clear();
                                        classPagination.setPageCount(1);
                                        classTable.refresh();
                                        updatePageInfo();
                                        return;
                                    }
                                } else {
                                    filteredClassInfo.clear();
                                    classPagination.setPageCount(1);
                                    classTable.refresh();
                                    updatePageInfo();
                                    return;
                                }

                                if (dataArray != null) {
                                    ObservableList<ClassInfo> newData = FXCollections.observableArrayList();
                                    for (int i = 0; i < dataArray.size(); i++) {
                                        JsonObject classJson = dataArray.get(i).getAsJsonObject();
                                        String id = classJson.has("id") ? classJson.get("id").getAsString() : "N/A";
                                        String major = classJson.has("major") ? classJson.get("major").getAsString() : "未知专业";
                                        String number = classJson.has("number") ? classJson.get("number").getAsString() : "未知班号";
                                        String name = major + number;
                                        String department = "软件学院";
                                        String grade = classJson.has("grade") ? classJson.get("grade").getAsString() + "级" : "未知年级";
                                        String counselor = classJson.has("advisor") ? classJson.get("advisor").getAsString() : "N/A";
                                        int studentCount = 0;
                                        try{
                                            if (classJson.has("studentCount") && !classJson.get("studentCount").isJsonNull()) {
                                                studentCount = classJson.get("studentCount").getAsInt();
                                            } else {
                                                studentCount = 0;
                                            }
                                        }catch (Exception e){
                                            studentCount = 0;
                                        }
                                        String status = "正常";

                                        newData.add(new ClassInfo(id, name, department, grade, counselor, studentCount, status));
                                    }

                                    filteredClassInfo.setAll(newData);

                                    if (classTable.getItems() != filteredClassInfo) {
                                        classTable.setItems(filteredClassInfo);
                                    }

                                    classTable.refresh();

                                    classPagination.setPageCount(totalPages > 0 ? totalPages : 1);

                                    updatePageInfo();
                                }
                            } else {
                                String errorMsg = response.has("msg") ? response.get("msg").getAsString() : "未知错误";
                                filteredClassInfo.clear();
                                classPagination.setPageCount(1);
                                classTable.refresh();
                                updatePageInfo();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            filteredClassInfo.clear();
                            classPagination.setPageCount(1);
                            classTable.refresh();
                            updatePageInfo();
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Platform.runLater(() -> {
                        e.printStackTrace();
                        filteredClassInfo.clear();
                        classPagination.setPageCount(1);
                        classTable.refresh();
                        updatePageInfo();
                    });
                }
            });
        }else {
            NetworkUtils.get("/section/getSectionListAll", params, new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    Platform.runLater(() -> {
                        try {
                            Gson gson = new Gson();
                            JsonObject response = gson.fromJson(result, JsonObject.class);

                            if (response.has("code") && response.get("code").getAsInt() == 200) {
                                int totalPages = 1;
                                int totalRecords = 0;

                                JsonArray dataArray = null;
                                if (response.has("data") && response.get("data").isJsonObject()) {
                                    JsonObject dataObject = response.getAsJsonObject("data");

                                     if (dataObject.has("page")) {
                                        totalPages = dataObject.get("page").getAsInt();
                                    }

                                    if (dataObject.has("total")) {
                                        try {
                                            if (!dataObject.get("total").isJsonNull()) {
                                                totalRecords = dataObject.get("total").getAsInt();
                                            }
                                        } catch (Exception e) {
                                        }
                                    }

                                    if (dataObject.has("section") && dataObject.get("section").isJsonArray()) {
                                        dataArray = dataObject.getAsJsonArray("section");
                                    } else {
                                        showInfoDialog("数据错误", "响应中缺少班级列表数据 (section 数组)。");
                                        filteredClassInfo.clear();
                                        classPagination.setPageCount(1);
                                        classTable.refresh();
                                        updatePageInfo();
                                        return;
                                    }
                                } else {
                                    showInfoDialog("数据错误", "响应中缺少数据对象。");
                                    filteredClassInfo.clear();
                                    classPagination.setPageCount(1);
                                    classTable.refresh();
                                    updatePageInfo();
                                    return;
                                }

                                if (dataArray != null) {
                                    ObservableList<ClassInfo> newData = FXCollections.observableArrayList();
                                    for (int i = 0; i < dataArray.size(); i++) {
                                        JsonObject classJson = dataArray.get(i).getAsJsonObject();
                                        String id = classJson.has("id") ? classJson.get("id").getAsString() : "N/A";
                                        String major = classJson.has("major") ? classJson.get("major").getAsString() : "未知专业";
                                        String number = classJson.has("number") ? classJson.get("number").getAsString() : "未知班号";
                                        String name = major + number;
                                        String department = "软件学院";
                                        String grade = classJson.has("grade") ? classJson.get("grade").getAsString() + "级" : "未知年级";
                                        String counselor = classJson.has("advisor") ? classJson.get("advisor").getAsString() : "N/A";
                                        int studentCount = 0;
                                        try{
                                            if (classJson.has("studentCount") && !classJson.get("studentCount").isJsonNull()) {
                                                studentCount = classJson.get("studentCount").getAsInt();
                                            } else {
                                                studentCount = 0;
                                            }
                                        }catch (Exception e){
                                            studentCount = 0;
                                        }
                                        String status = "正常";

                                        newData.add(new ClassInfo(id, name, department, grade, counselor, studentCount, status));
                                    }

                                    filteredClassInfo.setAll(newData);

                                    if (classTable.getItems() != filteredClassInfo) {
                                        classTable.setItems(filteredClassInfo);
                                    }

                                    classTable.refresh();

                                    classPagination.setPageCount(totalPages > 0 ? totalPages : 1);

                                    updatePageInfo();
                                }
                            } else {
                                String errorMsg = response.has("msg") ? response.get("msg").getAsString() : "未知错误";
                                showInfoDialog("加载失败", "加载班级列表失败: " + errorMsg);
                                filteredClassInfo.clear();
                                classPagination.setPageCount(1);
                                classTable.refresh();
                                updatePageInfo();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            showInfoDialog("处理失败", "处理班级列表响应时出错: " + e.getMessage());
                            filteredClassInfo.clear();
                            classPagination.setPageCount(1);
                            classTable.refresh();
                            updatePageInfo();
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Platform.runLater(() -> {
                        e.printStackTrace();
                        showInfoDialog("网络错误", "无法连接到服务器或请求失败: " + e.getMessage());
                        filteredClassInfo.clear();
                        classPagination.setPageCount(1);
                        classTable.refresh();
                        updatePageInfo();
                    });
                }
            });
        }
    }

    @FXML
    private void searchClasses() {
        loadTableData(0);
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        gradeFilter.getSelectionModel().selectFirst();
        loadTableData(0);
    }

    private void applyFilters() {
        loadTableData(0);
    }

    private void applyFiltersAndSearch(String searchTerm) {
        loadTableData(0);
    }

    @FXML
    private void showAddClassView(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/addNewClass.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(rootPane.getScene().getWindow());
            dialog.setTitle("添加新班级");
            dialog.setResizable(false);

            Scene scene = new Scene(root);
            dialog.setScene(scene);

            dialog.showAndWait();

            loadTableData(classPagination.getCurrentPageIndex());
        } catch (Exception e) {
            e.printStackTrace();
            showInfoDialog("加载错误", "无法加载添加班级窗口: " + e.getMessage());
        }
    }

    @FXML
    private void batchManageClasses(javafx.scene.input.MouseEvent event) {
        showInfoDialog("功能开发中", "批量管理功能将在后续版本开放。");
    }

    @FXML
    private void exportClasses(javafx.scene.input.MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出班级数据");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel文件", "*.xlsx"));
        fileChooser.setInitialFileName("班级数据.xlsx");

        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file != null) {
            System.out.println("导出到: " + file.getAbsolutePath());
            showInfoDialog("导出成功", "班级数据已成功导出到: " + file.getAbsolutePath());
        }
    }

    @FXML
    private void batchDeleteClasses() {
        showInfoDialog("功能开发中", "批量删除功能将在后续版本开放。");
    }

    private void viewClass(int index) {
        int actualIndex = index;
        if (actualIndex < 0 || actualIndex >= filteredClassInfo.size()) return;
        ClassInfo selectedClass = filteredClassInfo.get(actualIndex);
        showInfoDialog("查看班级", "查看班级: " + selectedClass.getName() + "\n(详情页面待实现)");
    }

    private void editClass(int index) {
        int actualIndex = index;
        if (actualIndex < 0 || actualIndex >= filteredClassInfo.size()) return;
        ClassInfo selectedClass = filteredClassInfo.get(actualIndex);
        showInfoDialog("编辑班级", "编辑班级: " + selectedClass.getName() + "\n(编辑功能待实现)");
    }

    private void deleteClass(int index) {
        int actualIndex = index;
        if (actualIndex < 0 || actualIndex >= filteredClassInfo.size()) return;
        ClassInfo selectedClass = filteredClassInfo.get(actualIndex);
        if (showConfirmDialog("确认删除", "确定要删除班级 " + selectedClass.getName() + " 吗？\n此操作可能无法恢复。")) {
            showInfoDialog("操作成功", "已删除班级: " + selectedClass.getName() + "\n(模拟)");

            int currentPage = classPagination.getCurrentPageIndex();
            loadTableData(currentPage);
        }
    }

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

    public static class ClassInfo {
        private final SimpleStringProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty department;
        private final SimpleStringProperty grade;
        private final SimpleStringProperty counselor;
        private final SimpleIntegerProperty studentCount;
        private final SimpleStringProperty status;

        public ClassInfo(String id, String name, String department, String grade, String counselor, int studentCount, String status) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.department = new SimpleStringProperty(department);
            this.grade = new SimpleStringProperty(grade);
            this.counselor = new SimpleStringProperty(counselor);
            this.studentCount = new SimpleIntegerProperty(studentCount);
            this.status = new SimpleStringProperty(status);
        }

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
    }
}