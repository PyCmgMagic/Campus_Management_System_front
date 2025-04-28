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

    // FXML 绑定
    @FXML private ScrollPane rootPane;
    @FXML private VBox mainClassesView;
    @FXML private Label mainPageTitle;
    @FXML private HBox mainTitleContainer;

    // 快速操作
    @FXML private BorderPane addClassCard;
    @FXML private BorderPane batchManageCard;
    @FXML private BorderPane exportClassCard;

    // 搜索和筛选
    @FXML private TextField searchField;
    @FXML private ComboBox<String> gradeFilter;

    // 批量操作
    @FXML private Button batchDeleteBtn; // 批量删除按钮示例

    // 班级表格
    @FXML private TableView<ClassInfo> classTable;
    @FXML private TableColumn<ClassInfo, String> idColumn;
    @FXML private TableColumn<ClassInfo, String> nameColumn;
    @FXML private TableColumn<ClassInfo, String> departmentColumn;
    @FXML private TableColumn<ClassInfo, String> gradeColumn;
    @FXML private TableColumn<ClassInfo, String> counselorColumn;
    @FXML private TableColumn<ClassInfo, Integer> studentCountColumn;
    @FXML private TableColumn<ClassInfo, String> statusColumn;
    @FXML private TableColumn<ClassInfo, Void> actionColumn;

    // 分页
    @FXML private Pagination classPagination;
    @FXML private Label pageInfo;

    // 数据存储
    private ObservableList<ClassInfo> filteredClassInfo = FXCollections.observableArrayList();

    // 分页参数
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

        // 添加监听器，当选择变化时应用筛选
        gradeFilter.setOnAction(e -> applyFilters());
    }

    private void initClassTable() {
        // 为每列设置单元格值工厂
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
        counselorColumn.setCellValueFactory(new PropertyValueFactory<>("counselor"));
        studentCountColumn.setCellValueFactory(new PropertyValueFactory<>("studentCount"));

        // 状态列的自定义单元格工厂
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
                    // 根据状态应用不同样式
                    switch (item) {
                        case "正常":
                            statusLabel.getStyleClass().add("status-active"); // 重用活跃状态样式
                            break;
                        case "已毕业":
                        case "已撤销":
                            statusLabel.getStyleClass().add("status-inactive"); // 重用非活跃状态样式
                            break;
                        default:
                            statusLabel.getStyleClass().add("status-pending"); // 对其他情况重用待处理样式
                            break;
                    }
                    setGraphic(statusLabel);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // 添加操作列单元格工厂
        actionColumn.setCellFactory(createActionCellFactory());

        // 设置初始项绑定（对更新至关重要）
        classTable.setItems(filteredClassInfo);
    }

    private Callback<TableColumn<ClassInfo, Void>, TableCell<ClassInfo, Void>> createActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<ClassInfo, Void> call(TableColumn<ClassInfo, Void> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button();
                    private final Button editBtn = new Button();
                    private final Button deleteBtn = new Button(); // 或存档/毕业
                    private final HBox actionBox = new HBox(5, viewBtn, editBtn, deleteBtn);

                    {
                        viewBtn.getStyleClass().addAll("table-button", "default-btn");
                        editBtn.getStyleClass().addAll("table-button", "warning-btn");
                        deleteBtn.getStyleClass().addAll("table-button", "danger-btn");

                        // 添加图标（重用课程管理的CSS）
                        Region viewIcon = new Region(); viewIcon.getStyleClass().add("view-icon"); viewBtn.setGraphic(viewIcon);
                        Region editIcon = new Region(); editIcon.getStyleClass().add("edit-icon"); editBtn.setGraphic(editIcon);
                        Region deleteIcon = new Region(); deleteIcon.getStyleClass().add("delete-icon"); deleteBtn.setGraphic(deleteIcon);

                        // 设置工具提示
                        viewBtn.setTooltip(new Tooltip("查看详情"));
                        editBtn.setTooltip(new Tooltip("编辑班级"));
                        deleteBtn.setTooltip(new Tooltip("删除班级"));

                        // 设置操作
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
                            // 获取底层过滤列表中的实际项索引
                            int currentIndex = getTableRow() != null ? getTableRow().getIndex() : -1;
                            // 在启用按钮之前确保索引有效
                            if (currentIndex >= 0 && currentIndex < filteredClassInfo.size()) {
                                setGraphic(actionBox);
                            } else {
                                setGraphic(null); // 处理更新期间可能出现的索引越界问题
                            }
                        }
                    }
                };
            }
        };
    }

    private void initPagination() {
        // 页数将在数据加载后设置
        classPagination.setCurrentPageIndex(0);
        classPagination.setPageFactory(this::createClassPage);
    }

    private Node createClassPage(int pageIndex) {
        loadTableData(pageIndex);
        // 返回表格。当loadTableData通过filteredClassInfo列表绑定完成时，表格内容将异步更新。
        return classTable; // 返回表格本身
    }

    private void updatePageInfo() {
        int currentPage = classPagination.getCurrentPageIndex();
        int fromIndex = currentPage * ROWS_PER_PAGE + 1;
        // 根据当前页实际加载的项目数计算toIndex
        int toIndex = fromIndex + filteredClassInfo.size() - 1;

        if (filteredClassInfo.isEmpty() && currentPage == 0) { // 第一页空结果的特殊情况
            pageInfo.setText("共 0 条记录");
        } else if (filteredClassInfo.isEmpty()) { // 空页，但不一定是第一页
            pageInfo.setText("无更多记录"); // 或根据需要调整消息
        } else {
            pageInfo.setText(String.format("当前显示 %d-%d 条", fromIndex, toIndex));
        }
    }

    private void loadTableData(int pageIndex) {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(pageIndex + 1));
        params.put("size", String.valueOf(ROWS_PER_PAGE));
        // TODO: 实现服务器端筛选时在此处添加筛选参数
        // String department = departmentFilter.getValue(); if (!department.equals("全部院系")) params.put("department", department);
        // String grade = gradeFilter.getValue(); if (!grade.equals("全部年级")) params.put("grade", grade);
        // String status = statusFilter.getValue(); if (!status.equals("全部状态")) params.put("status", status);
        // String searchTerm = searchField.getText(); if (!searchTerm.isEmpty()) params.put("search", searchTerm);

        NetworkUtils.get("/section/getSectionListAll", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                Platform.runLater(() -> {
                    try {
                        Gson gson = new Gson();
                        JsonObject response = gson.fromJson(result, JsonObject.class);

                        if (response.has("code") && response.get("code").getAsInt() == 200) {
                            int totalItems = 0; // API未提供总项目数
                            boolean isLastPage = false; // 估计这是否是最后一页的标志

                            JsonArray dataArray = null;
                            if (response.has("data") && response.get("data").isJsonArray()) {
                                dataArray = response.getAsJsonArray("data");
                            } else {
                                showInfoDialog("数据错误", "响应中未找到班级列表数据 (data array)。");
                                filteredClassInfo.clear();
                                classPagination.setPageCount(1);
                                updatePageInfo(); // 在没有totalItems的情况下更新
                                return; // 停止处理
                            }

                            if (dataArray != null) {
                                filteredClassInfo.clear(); // 清除上一页数据
                                for (int i = 0; i < dataArray.size(); i++) {
                                    JsonObject classJson = dataArray.get(i).getAsJsonObject();
                                    // 根据提供的示例映射JSON字段
                                    String id = classJson.has("id") ? classJson.get("id").getAsString() : "N/A";
                                    String major = classJson.has("major") ? classJson.get("major").getAsString() : "未知专业";
                                    String number = classJson.has("number") ? classJson.get("number").getAsString() : "未知班号";
                                    String name = major + number; // 将专业和班号组合为名称
                                    String department = "软件学院"; // 示例中未提供系部
                                    String grade = classJson.has("grade") ? classJson.get("grade").getAsString() + "级" : "未知年级";
                                    String counselor = classJson.has("advisor") ? classJson.get("advisor").getAsString() : "N/A";
                                    int studentCount = 0;
                                   try{
                                       studentCount = classJson.has("studentCount") ? classJson.get("studentCount").getAsInt() : -1;
                                   }catch (Exception e){
                                       studentCount = -1;
                                   }
                                    String status = "正常";

                                    filteredClassInfo.add(new ClassInfo(id, name, department, grade, counselor, studentCount, status));
                                }

                                // 估计总页数，因为API不提供总计数
                                if (dataArray.size() < ROWS_PER_PAGE) {
                                    isLastPage = true;
                                }

                                int currentPageIndex = classPagination.getCurrentPageIndex();
                                int estimatedTotalPages;
                                if (isLastPage) {
                                    estimatedTotalPages = currentPageIndex + 1;
                                } else {
                                    // 假设至少存在一个更多页面
                                    estimatedTotalPages = currentPageIndex + 2;
                                }
                                // 如果存在结果，防止将页数设置为低于当前页 + 1
                                if (estimatedTotalPages <= currentPageIndex && !filteredClassInfo.isEmpty()) {
                                     estimatedTotalPages = currentPageIndex + 1;
                                }
                                // 确保至少有1页，即使为空
                                if (estimatedTotalPages == 0) {
                                    estimatedTotalPages = 1;
                                }

                                classPagination.setPageCount(estimatedTotalPages);
                                System.out.println("警告：API未提供总项目数。估计总页数：" + estimatedTotalPages);

                                // 更新分页信息标签（无总计数）
                                updatePageInfo();

                            } // 否则情况已在上面处理

                        } else {
                            String errorMsg = response.has("msg") ? response.get("msg").getAsString() : "未知错误";
                            showInfoDialog("加载失败", "无法加载班级列表: " + errorMsg);
                            filteredClassInfo.clear();
                            classPagination.setPageCount(1);
                            updatePageInfo(); // 在没有totalItems的情况下更新
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // 记录完整堆栈跟踪以进行调试
                        showInfoDialog("处理失败", "处理班级列表响应时出错: " + e.getMessage());
                        filteredClassInfo.clear();
                        classPagination.setPageCount(1);
                        updatePageInfo(); // 在没有totalItems的情况下更新
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> { // 确保UI更新发生在JavaFX应用程序线程上
                     e.printStackTrace(); // 记录完整堆栈跟踪以进行调试
                     showInfoDialog("网络错误", "无法连接到服务器或请求失败: " + e.getMessage());
                     filteredClassInfo.clear();
                     classPagination.setPageCount(1);
                     updatePageInfo(); // 在没有totalItems的情况下更新
                 });
            }
        });
    }

    // 事件处理程序

    @FXML
    private void searchClasses() {
        System.out.println("搜索按钮已点击。需要实现服务器端搜索。");
        // 暂时，只需在没有搜索词的情况下重新加载第一页
        loadTableData(0);
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        gradeFilter.getSelectionModel().selectFirst();
        System.out.println("重置按钮已点击。重新加载第一页。");
        loadTableData(0); // 重新加载第一页数据
    }

    private void applyFilters() {
        System.out.println("筛选条件已更改。需要实现服务器端筛选。");
        // 暂时，只要筛选条件更改，就重新加载第一页
        loadTableData(0);
    }

    // 此方法现在与逐页加载不兼容，需要服务器端筛选。
    // 保留方法签名，但注释掉本地筛选逻辑。
    private void applyFiltersAndSearch(String searchTerm) {
        System.out.println("已调用applyFiltersAndSearch。这需要触发带有筛选参数的API调用。");
        // 为第0页触发带有当前筛选条件和搜索词的API调用
        loadTableData(0); // 使用潜在的服务器端筛选条件（如果添加到loadTableData中）重新加载第0页
    }

    @FXML
    private void showAddClassView(javafx.scene.input.MouseEvent event) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/addNewClass.fxml"));
            Parent root = loader.load();
            
            // Create a new stage for the dialog
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL); // Block input to other windows
            dialog.initOwner(rootPane.getScene().getWindow()); // Set the parent window
            dialog.setTitle("添加新班级");
            dialog.setResizable(false);
            
            // Create scene with root element
            Scene scene = new Scene(root);
            dialog.setScene(scene);
            
            // Show the dialog and wait for it to be closed
            dialog.showAndWait();
            
            // After dialog is closed, refresh table data (in case a new class was added)
            loadTableData(classPagination.getCurrentPageIndex());
        } catch (Exception e) {
            e.printStackTrace();
            showInfoDialog("加载错误", "无法加载添加班级窗口: " + e.getMessage());
        }
    }

    @FXML
    private void batchManageClasses(javafx.scene.input.MouseEvent event) {
        System.out.println("批量管理已点击");
        // 实现批量管理的逻辑
        showInfoDialog("功能开发中", "批量管理功能将在后续版本开放。");
    }

    @FXML
    private void exportClasses(javafx.scene.input.MouseEvent event) {
        System.out.println("导出班级已点击");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出班级数据");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel文件", "*.xlsx"));
        fileChooser.setInitialFileName("班级数据.xlsx");

        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file != null) {
            // 在此处实现实际的导出逻辑（例如，使用Apache POI）
            System.out.println("导出到: " + file.getAbsolutePath());
            showInfoDialog("导出成功", "班级数据已成功导出到: " + file.getAbsolutePath());
        }
    }

    @FXML
    private void batchDeleteClasses() {
        System.out.println("批量删除已点击");
        // 实现获取选定项目并删除它们的逻辑
        // List<ClassInfo> selected = classTable.getItems().stream().filter(ClassInfo::isSelected).collect(Collectors.toList());
        // if (selected.isEmpty()) { ... }
        showInfoDialog("功能开发中", "批量删除功能将在后续版本开放。");
    }

    // 操作列方法
    private void viewClass(int index) {
        int actualIndex = index;
        if (actualIndex < 0 || actualIndex >= filteredClassInfo.size()) return;
        ClassInfo selectedClass = filteredClassInfo.get(actualIndex);
        System.out.println("查看班级: " + selectedClass.getName());
        // 实现显示班级详情视图的逻辑
        showInfoDialog("查看班级", "查看班级: " + selectedClass.getName() + "\n(详情页面待实现)");
    }

    private void editClass(int index) {
        int actualIndex = index;
        if (actualIndex < 0 || actualIndex >= filteredClassInfo.size()) return;
        ClassInfo selectedClass = filteredClassInfo.get(actualIndex);
        System.out.println("编辑班级: " + selectedClass.getName());
        // 实现显示编辑班级对话框/视图的逻辑
        showInfoDialog("编辑班级", "编辑班级: " + selectedClass.getName() + "\n(编辑功能待实现)");
    }

    private void deleteClass(int index) {
        int actualIndex = index;
        if (actualIndex < 0 || actualIndex >= filteredClassInfo.size()) return;
        ClassInfo selectedClass = filteredClassInfo.get(actualIndex);
        System.out.println("删除班级: " + selectedClass.getName());
        if (showConfirmDialog("确认删除", "确定要删除班级 " + selectedClass.getName() + " 吗？\n此操作可能无法恢复。")) {
            // TODO: 实现在服务器上删除班级的实际API调用
            System.out.println("需要在此处调用API删除班级 " + selectedClass.getId() + "。");

            // 暂时模拟成功删除：
            showInfoDialog("操作成功", "已删除班级: " + selectedClass.getName() + "\n(模拟)");

            // 从服务器成功删除后，重新加载当前页数据
            int currentPage = classPagination.getCurrentPageIndex();
            loadTableData(currentPage);
        }
    }

    // 辅助对话框方法
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

    // 班级数据模型的内部类（如果存在，请替换为您的实际模型）
    public static class ClassInfo {
        private final SimpleStringProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty department;
        private final SimpleStringProperty grade;
        private final SimpleStringProperty counselor;
        private final SimpleIntegerProperty studentCount;
        private final SimpleStringProperty status;
        // 如果需要批量操作，添加选定属性
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

        // Getters（PropertyValueFactory需要）
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