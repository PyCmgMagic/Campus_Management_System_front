package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.work.javafx.model.Student;
import com.work.javafx.util.ShowMessage;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.work.javafx.util.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

public class StudentMangementController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(StudentMangementController.class.getName());

    // FXML UI元素
    @FXML private StackPane addStudentCard;
    @FXML private StackPane importStudentCard;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> majorComboBox;
    @FXML private ComboBox<String> gradeComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> idColumn;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> genderColumn;
    @FXML private TableColumn<Student, String> departmentColumn;
    @FXML private TableColumn<Student, String> majorColumn;
    @FXML private TableColumn<Student, String> gradeColumn;
    @FXML private TableColumn<Student, String> classColumn;
    @FXML private TableColumn<Student, String> statusColumn;
    @FXML private TableColumn<Student, Student> actionColumn;
    @FXML private Pagination pagination;
    @FXML private Label pageInfoLabel;

    // 数据模型
    private ObservableList<Student> masterData = FXCollections.observableArrayList();
    private int itemsPerPage = 10;
    private int totalPages = 1;
    private int totalItems = 0;
    private int currentPage = 1;
    private final SimpleBooleanProperty selectAll = new SimpleBooleanProperty(false);
    static Gson gson = new Gson();
    
    // 页码变化监听器
    private javafx.beans.value.ChangeListener<Number> pageChangeListener;
    
    // 添加一个页面加载锁定标志
    private volatile boolean isPageLoadingLocked = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化筛选下拉框
        initializeComboBoxes();
        
        // 初始化表格列
        initializeTable();
        
        // 初始化分页控件
        initializePagination();
        
        // 加载数据
        loadStudentsFromApi(1);
    }

    // 初始化筛选下拉框选项
    private void initializeComboBoxes() {

        // 添加专业选项
        majorComboBox.getItems().addAll(
            "全部专业",
            "计算机科学与技术",
            "软件工程",
            "电子信息工程",
            "通信工程",
            "数学与应用数学",
            "统计学",
            "英语"
        );
        majorComboBox.setValue("全部专业");
        
        // 添加年级选项
        gradeComboBox.getItems().addAll(
            "全部年级",
            "2024级",
            "2023级",
            "2022级",
            "2021级",
            "2020级"
        );
        gradeComboBox.setValue("全部年级");
        
        // 添加状态选项
        statusComboBox.getItems().addAll(
            "全部状态",
            "在读",
            "休学",
            "毕业"
        );
        statusComboBox.setValue("全部状态");
        
        // 添加筛选监听器
        majorComboBox.setOnAction(e -> handleFilterChange());
        gradeComboBox.setOnAction(e -> handleFilterChange());
        statusComboBox.setOnAction(e -> handleFilterChange());
    }

    // 初始化表格列和单元格工厂
    private void initializeTable() {

        // 设置数据列
        idColumn.setCellValueFactory(cellData -> cellData.getValue().sduidProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        genderColumn.setCellValueFactory(cellData -> cellData.getValue().genderProperty());
        departmentColumn.setCellValueFactory(cellData -> cellData.getValue().departmentProperty());
        majorColumn.setCellValueFactory(cellData -> cellData.getValue().majorProperty());
        gradeColumn.setCellValueFactory(cellData -> cellData.getValue().gradeProperty());
        classColumn.setCellValueFactory(cellData -> cellData.getValue().classNameProperty());
        
        // 设置状态列自定义渲染
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        statusColumn.setCellFactory(column -> new TableCell<Student, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(item);
                    statusLabel.getStyleClass().add("status-badge");
                    
                    switch (item) {
                        case "在读":
                            statusLabel.getStyleClass().add("status-active");
                            break;
                        case "休学":
                            statusLabel.getStyleClass().add("status-suspended");
                            break;
                        case "毕业":
                            statusLabel.getStyleClass().add("status-graduated");
                            break;
                    }
                    
                    setGraphic(statusLabel);
                    setText(null);
                }
            }
        });
        
        // 设置操作列
        actionColumn.setCellValueFactory(param -> new javafx.beans.property.ReadOnlyObjectWrapper<>(param.getValue()));
        actionColumn.setCellFactory(column -> new TableCell<Student, Student>() {
            private final Button viewBtn = new Button();
            private final Button deleteBtn = new Button();
            private final HBox actionsBox = new HBox(10, viewBtn, deleteBtn);
            
            {
                // 初始化按钮样式和提示
                viewBtn.getStyleClass().addAll("table-button", "default-btn");
                deleteBtn.getStyleClass().addAll("table-button", "danger-btn");

                // 添加图标
                Region viewIcon = new Region();
                viewIcon.getStyleClass().add("view-icon");
                viewBtn.setGraphic(viewIcon);


                Region deleteIcon = new Region();
                deleteIcon.getStyleClass().add("delete-icon");
                deleteBtn.setGraphic(deleteIcon);



                deleteBtn.setTooltip(new Tooltip("删除"));
                viewBtn.setTooltip(new Tooltip("查看详情"));

                actionsBox.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Student student, boolean empty) {
                super.updateItem(student, empty);
                
                if (empty || student == null) {
                    setGraphic(null);
                } else {
                    viewBtn.setOnAction(e -> handleViewStudent(student));
                    deleteBtn.setOnAction(e -> handleDeleteStudent(student));
                    setGraphic(actionsBox);
                }
            }
        });
        
        // 设置表格可编辑
        studentTable.setEditable(true);
        
        // 设置表格选择模式
        studentTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    
    // 创建全选复选框
    private CheckBox createSelectAllCheckbox() {
        CheckBox selectAllCheckbox = new CheckBox();
        selectAllCheckbox.getStyleClass().add("select-all-checkbox");
        selectAllCheckbox.selectedProperty().bindBidirectional(selectAll);
        
        // 当全选框状态变化时更新所有行的选择状态
        selectAll.addListener((obs, oldVal, newVal) -> {
            // 更新当前页的所有数据
            studentTable.getItems().forEach(student -> student.setSelected(newVal));
            // 如果取消全选，需要将所有数据重置
            if (!newVal) {
                masterData.forEach(student -> student.setSelected(false));
            }
        });
        
        return selectAllCheckbox;
    }

    // 初始化分页控件
    private void initializePagination() {
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
        
        // 添加页码变化监听器
        pageChangeListener = (obs, oldIndex, newIndex) -> {
             // 添加额外的判断，确保新旧索引确实不同，且当前没有页面加载锁定
             if (newIndex.intValue() != oldIndex.intValue() && !isPageLoadingLocked) {
                 isPageLoadingLocked = true;
                 loadStudentsFromApi(newIndex.intValue() + 1);
             } else if (isPageLoadingLocked) {
                 Platform.runLater(() -> {
                     pagination.currentPageIndexProperty().removeListener(pageChangeListener);
                     pagination.setCurrentPageIndex(oldIndex.intValue());
                     pagination.currentPageIndexProperty().addListener(pageChangeListener);
                 });
             }
        };
        
        pagination.currentPageIndexProperty().addListener(pageChangeListener);
        updatePageInfo(); // Initial call
    }

    // 创建分页页面内容
    private Node createPage(int pageIndex) {
        return new VBox();
    }
    
    private void updatePageInfo() {
        if (pageInfoLabel == null) return;

        int itemsOnCurrentPage = masterData.size(); 

        if (totalItems == 0) {
            pageInfoLabel.setText("共 0 条记录");
        } else {
            int fromIndex = (currentPage - 1) * itemsPerPage + 1;
            int toIndex = Math.min(fromIndex + itemsOnCurrentPage - 1, totalItems);
             if (itemsOnCurrentPage == 0 && currentPage > 1) {
                 pageInfoLabel.setText(String.format("第 %d 页 (共 %d 页) 无记录", currentPage, totalPages));
             } else if (itemsOnCurrentPage == 0 && totalItems > 0) {
                 pageInfoLabel.setText(String.format("共 %d 条记录 (0 显示)", totalItems));
             } else {
                 pageInfoLabel.setText(String.format("显示 %d-%d 条，共 %d 条 (第 %d/%d 页)", 
                                                 fromIndex, toIndex, totalItems, currentPage, totalPages));
             }
        }
    }

    private void loadStudentsFromApi(int pageNum) {
        String gradeQueryParam = getSelectedGradeValue();
        Map<String, String> params = new HashMap<>();

        params.put("pageNum", String.valueOf(pageNum));
        params.put("pageSize", String.valueOf(itemsPerPage));

        // 用户请求的页码，记录下来以便后续比较
        final int requestedPage = pageNum;

        if(searchField.getText().trim().isEmpty()){
        if (gradeQueryParam != null && !gradeQueryParam.isEmpty()) {
            params.put("grade", gradeQueryParam);
        }
        String majorFilter = majorComboBox.getValue();
        if (majorFilter != null && !majorFilter.equals("全部专业")) {
            params.put("major", majorFilter);
        }

        // 确保UI被禁用，显示加载状态
        Platform.runLater(() -> {
            studentTable.setDisable(true);
            studentTable.getItems().clear(); 
        });

        NetworkUtils.getAsync("/admin/student/list", params)
            .thenAcceptAsync(response -> {
                try {
                    JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
                    
                    if (jsonResponse.has("code") && jsonResponse.get("code").getAsInt() == 200) {
                        JsonObject data = jsonResponse.getAsJsonObject("data");
                        int apiTotalItems = data.get("total").getAsInt();
                        int apiTotalPages = data.get("pages").getAsInt();
                        int apiCurrentPage = data.get("pageNum").getAsInt();
                        
                        // 转换学生数据
                        ObservableList<Student> newData = FXCollections.observableArrayList();
                        JsonArray studentList = data.getAsJsonArray("list");
                        
                        for (JsonElement element : studentList) {
                            JsonObject studentJson = element.getAsJsonObject();
                            String username = studentJson.get("username").getAsString();
                            String sex = studentJson.get("sex").getAsString();
                            String sduid = studentJson.get("sduid").getAsString();

                            String major = "未分配";
                            if (studentJson.has("major") && !studentJson.get("major").isJsonNull()) {
                                major = studentJson.get("major").getAsString();
                            }
                            int id = -1;
                             if (studentJson.has("id") && !studentJson.get("id").isJsonNull()) {
                                id = studentJson.get("id").getAsInt();
                            }
                            if(major.equals("0")){
                                major = "软件工程";
                            } else if (major.equals("1")) {
                                major = "数媒";
                            } else if (major.equals("2")) {
                                major = "大数据";
                            } else if (major.equals("3")) {
                                major = "AI";
                            } else if (major.equals("未分配")) {
                                // Keep as "未分配"
                            } else {
                            }

                            int gradeYear = 0;
                            if (studentJson.has("grade") && !studentJson.get("grade").isJsonNull()) {
                                try {
                                    gradeYear = studentJson.get("grade").getAsInt();
                                } catch (NumberFormatException e) {
                                    // gradeYear 保持默认值
                                }
                            }

                            String sectionStr = "未分班";
                            if (studentJson.has("section") && !studentJson.get("section").isJsonNull()) {
                                try {
                                    // 尝试将其作为数字获取，然后转换为字符串
                                    sectionStr = studentJson.get("section").getAsInt() + ""; 
                                } catch (NumberFormatException | UnsupportedOperationException e) {
                                    // 如果不是数字，尝试直接作为字符串获取
                                    try {
                                        sectionStr = studentJson.get("section").getAsString();
                                    } catch (UnsupportedOperationException e2){
                                        // sectionStr 保持默认值 "未分班"
                                    }
                                }
                            }
                            String fullsection = major + sectionStr + "班";

                            String studentApiStatus = "UNKNOWN_STATUS";
                            if (studentJson.has("status") && !studentJson.get("status").isJsonNull()) {
                                studentApiStatus = studentJson.get("status").getAsString();
                            }
                            String displayStatus = mapStatusValue(studentApiStatus);
                            
                            Student student = new Student(
                                    id+"",
                                sduid, 
                                username, 
                                sex, 
                                "软件学院",
                                major, 
                                gradeYear + "级",
                                fullsection,
                                displayStatus
                            );
                            
                            newData.add(student);
                        }
                        
                        // 最后在UI线程中更新界面
                        Platform.runLater(() -> {
                            try {
                                // 保存API返回的数据到模型
                                totalItems = apiTotalItems;
                                totalPages = apiTotalPages;
                                currentPage = apiCurrentPage;
                                
                                // 只有当当前页面的数据确实是我们请求的页面的数据时，才更新UI
                                if (requestedPage == apiCurrentPage) {
                                    // 临时禁用监听器
                                    pagination.currentPageIndexProperty().removeListener(pageChangeListener);
                                    
                                    // 设置总页数
                                    pagination.setPageCount(Math.max(1, totalPages));
                                    
                                    // 更新页码
                                    pagination.setCurrentPageIndex(currentPage - 1);
                                    
                                    // 重新添加监听器
                                    pagination.currentPageIndexProperty().addListener(pageChangeListener);
                                    
                                    // 更新表格数据
                                    masterData.setAll(newData);
                                    if (studentTable.getItems() != masterData) {
                                        studentTable.setItems(masterData);
                                    }
                                }
                            } finally {
                                // 无论如何都要解除加载锁定，启用UI
                                isPageLoadingLocked = false;
                                studentTable.setDisable(false);
                                updatePageInfo();
                            }
                        });
                    } else {
                        Platform.runLater(() -> {
                            try {
                                String errorMsg = jsonResponse.has("msg") 
                                    ? jsonResponse.get("msg").getAsString() 
                                    : "加载学生数据失败";
                                showAlert(Alert.AlertType.ERROR, "错误", errorMsg);
                                masterData.clear();
                            } finally {
                                isPageLoadingLocked = false;
                                studentTable.setDisable(false);
                                updatePageInfo();
                            }
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        try {
                            LOGGER.log(Level.SEVERE, "解析API响应失败", e);
                            showAlert(Alert.AlertType.ERROR, "错误", "解析API响应失败: " + e.getMessage());
                            masterData.clear();
                        } finally {
                            isPageLoadingLocked = false;
                            studentTable.setDisable(false);
                            updatePageInfo();
                        }
                    });
                }
            }, Platform::runLater)
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    try {
                        LOGGER.log(Level.SEVERE, "从API加载学生数据失败", ex);
                        showAlert(Alert.AlertType.ERROR, "错误", "从API加载学生数据失败: " + ex.getMessage());
                        masterData.clear();
                    } finally {
                        isPageLoadingLocked = false;
                        studentTable.setDisable(false);
                        updatePageInfo();
                    }
                });
                return null;
            });
        }else{
            params.put("permission","2");
            params.put("keyword",searchField.getText());
            NetworkUtils.getAsync("/admin/searchSdu", params)
                    .thenAcceptAsync(response -> {
                        try {
                            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

                            if (jsonResponse.has("code") && jsonResponse.get("code").getAsInt() == 200) {
                                JsonObject data = jsonResponse.getAsJsonObject("data");
                                int apiTotalItems = data.get("total").getAsInt();
                                int apiCurrentPage = data.get("pageNum").getAsInt();
                                int pageSize = data.get("pageSize").getAsInt();
                                int apiTotalPages = apiTotalItems % pageSize == 0 ? apiTotalItems/pageSize : apiTotalItems /pageSize +1;

                                        // 转换学生数据
                                ObservableList<Student> newData = FXCollections.observableArrayList();
                                JsonArray studentList = data.getAsJsonArray("list");

                                for (JsonElement element : studentList) {
                                    JsonObject studentJson = element.getAsJsonObject();
                                    String username = studentJson.get("username").getAsString();
                                    String sex = studentJson.get("sex").getAsString();
                                    String sduid = studentJson.get("sduid").getAsString();

                                    String major = "未分配";
                                    if (studentJson.has("major") && !studentJson.get("major").isJsonNull()) {
                                        major = studentJson.get("major").getAsString();
                                    }
                                    int id = -1;
                                    if (studentJson.has("id") && !studentJson.get("id").isJsonNull()) {
                                        id = studentJson.get("id").getAsInt();
                                    }
                                    if(major.equals("0")){
                                        major = "软件工程";
                                    } else if (major.equals("1")) {
                                        major = "数媒";
                                    } else if (major.equals("2")) {
                                        major = "大数据";
                                    } else if (major.equals("3")) {
                                        major = "AI";
                                    } else if (major.equals("未分配")) {
                                        // Keep as "未分配"
                                    } else {
                                    }

                                    int gradeYear = 0;
                                    if (studentJson.has("grade") && !studentJson.get("grade").isJsonNull()) {
                                        try {
                                            gradeYear = studentJson.get("grade").getAsInt();
                                        } catch (NumberFormatException e) {
                                            // gradeYear 保持默认值
                                        }
                                    }

                                    String sectionStr = "未分班";
                                    if (studentJson.has("section") && !studentJson.get("section").isJsonNull()) {
                                        try {
                                            // 尝试将其作为数字获取，然后转换为字符串
                                            sectionStr = studentJson.get("section").getAsInt() + "";
                                        } catch (NumberFormatException | UnsupportedOperationException e) {
                                            // 如果不是数字，尝试直接作为字符串获取
                                            try {
                                                sectionStr = studentJson.get("section").getAsString();
                                            } catch (UnsupportedOperationException e2){
                                                // sectionStr 保持默认值 "未分班"
                                            }
                                        }
                                    }
                                    String fullsection = major + sectionStr + "班";

                                    String studentApiStatus = "UNKNOWN_STATUS";
                                    if (studentJson.has("status") && !studentJson.get("status").isJsonNull()) {
                                        studentApiStatus = studentJson.get("status").getAsString();
                                    }
                                    String displayStatus = mapStatusValue(studentApiStatus);

                                    Student student = new Student(
                                            id+"",
                                            sduid,
                                            username,
                                            sex,
                                            "软件学院",
                                            major,
                                            gradeYear + "级",
                                            fullsection,
                                            displayStatus
                                    );

                                    newData.add(student);
                                }

                                // 最后在UI线程中更新界面
                                Platform.runLater(() -> {
                                    try {
                                        // 保存API返回的数据到模型
                                        totalItems = apiTotalItems;
                                        totalPages = apiTotalPages;
                                        currentPage = apiCurrentPage;

                                        // 只有当当前页面的数据确实是我们请求的页面的数据时，才更新UI
                                        if (requestedPage == apiCurrentPage) {
                                            // 临时禁用监听器
                                            pagination.currentPageIndexProperty().removeListener(pageChangeListener);

                                            // 设置总页数
                                            pagination.setPageCount(Math.max(1, totalPages));

                                            // 更新页码
                                            pagination.setCurrentPageIndex(currentPage - 1);

                                            // 重新添加监听器
                                            pagination.currentPageIndexProperty().addListener(pageChangeListener);

                                            // 更新表格数据
                                            masterData.setAll(newData);
                                            if (studentTable.getItems() != masterData) {
                                                studentTable.setItems(masterData);
                                            }
                                        }
                                    } finally {
                                        // 无论如何都要解除加载锁定，启用UI
                                        isPageLoadingLocked = false;
                                        studentTable.setDisable(false);
                                        updatePageInfo();
                                    }
                                });
                            } else {
                                Platform.runLater(() -> {
                                    try {
                                        String errorMsg = jsonResponse.has("msg")
                                                ? jsonResponse.get("msg").getAsString()
                                                : "加载学生数据失败";
                                        showAlert(Alert.AlertType.ERROR, "错误", errorMsg);
                                        masterData.clear();
                                    } finally {
                                        isPageLoadingLocked = false;
                                        studentTable.setDisable(false);
                                        updatePageInfo();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Platform.runLater(() -> {
                                try {
                                    LOGGER.log(Level.SEVERE, "解析API响应失败", e);
                                    showAlert(Alert.AlertType.ERROR, "错误", "解析API响应失败: " + e.getMessage());
                                    masterData.clear();
                                } finally {
                                    isPageLoadingLocked = false;
                                    studentTable.setDisable(false);
                                    updatePageInfo();
                                }
                            });
                        }
                    }, Platform::runLater)
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            try {
                                LOGGER.log(Level.SEVERE, "从API加载学生数据失败", ex);
                                showAlert(Alert.AlertType.ERROR, "错误", "从API加载学生数据失败: " + ex.getMessage());
                                masterData.clear();
                            } finally {
                                isPageLoadingLocked = false;
                                studentTable.setDisable(false);
                                updatePageInfo();
                            }
                        });
                        return null;
                    });
        }
    }
    
    // 将API状态值映射为显示值
    private String mapStatusValue(String apiStatus) {
        switch (apiStatus) {
            case "STUDYING":
                return "在读";
            case "SUSPENDED":
                return "休学";
            case "GRADUATED":
                return "毕业";
            default:
                return apiStatus;
        }
    }
    
    // Map display status back to API value if needed for filtering
    private String mapStatusToApiValue(String displayStatus) {
        switch (displayStatus) {
            case "在读":
                return "STUDYING";
            case "休学":
                return "SUSPENDED";
            case "毕业":
                return "GRADUATED";
            default:
                return "";
        }
    }

    // 获取选中的年级值
    private String getSelectedGradeValue() {
        String gradeText = gradeComboBox.getValue();
        if (gradeText == null || gradeText.equals("全部年级")) {
            return "";
        }
        
        // 提取年级数字（例如：从 "2024级" 提取 "2024"）
        return gradeText.replaceAll("[^0-9]", "");
    }
    
    // 处理筛选条件变化
    private void handleFilterChange() {
        loadStudentsFromApi(1);
    }

    // 处理搜索按钮点击
    @FXML
    private void handleSearch() {
        loadStudentsFromApi(1);
    }

    // 处理重置按钮点击
    @FXML
    private void handleReset() {
        searchField.clear();
        majorComboBox.setValue("全部专业");
        gradeComboBox.setValue("全部年级");
        statusComboBox.setValue("全部状态");
        loadStudentsFromApi(1);
    }

    // 处理批量删除按钮点击
    @FXML
    private void handleBatchDelete() {
        List<Student> selectedStudents = masterData.stream()
                .filter(Student::isSelected)
                .collect(Collectors.toList());
                
        if (selectedStudents.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "提示", "请先选择要删除的学生");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除选中的 " + selectedStudents.size() + " 个学生吗？此操作不可撤销。");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            showAlert(Alert.AlertType.INFORMATION, "成功", "已成功删除 " + selectedStudents.size() + " 个学生（模拟）。请调用后端API实现真正删除。");
            
            loadStudentsFromApi(Math.max(1, currentPage));
        }
    }

    // 处理添加学生卡片点击
    @FXML
    private void handleAddStudent(MouseEvent event) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/addNewStudent.fxml"));
            Parent root = loader.load();
            AddNewStudentController controller = loader.getController();
            Stage stage = new Stage();
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("添加学生");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root,800,600));
            stage.setMinHeight(700);
            stage.setMinWidth(550);
            controller.setStage(stage);
            stage.showAndWait();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @FXML
    private void importStudents(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择学生信息文件");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel 文件", "*.xlsx", "*.xls")
        );

        Stage stage = (Stage) importStudentCard.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                showAlert(Alert.AlertType.INFORMATION, "提示", "正在上传文件: " + selectedFile.getName() + "...");

                NetworkUtils.postMultipartFileAsync("/admin/upload", selectedFile)
                    .thenAcceptAsync(response -> {
                        Platform.runLater(() -> {

                            JsonObject res = gson.fromJson(response,JsonObject.class);
                            if(res.has("code") && res.get("code").getAsInt()==200){
                                System.out.println( response);
                                showAlert(Alert.AlertType.INFORMATION, "成功", "文件上传成功！");
                                loadStudentsFromApi(1);
                            }

                        });
                    }, Platform::runLater)
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "文件上传失败", ex);
                            showAlert(Alert.AlertType.ERROR, "错误", "文件上传失败: " + ex.getMessage());
                        });
                        return null;
                    });

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "启动文件上传时出错", e);
                showAlert(Alert.AlertType.ERROR, "错误", "启动文件上传时出错: " + e.getMessage());
            }
        } else {
            System.out.println("未选择文件。");
             showAlert(Alert.AlertType.INFORMATION, "提示", "未选择任何文件。");
        }
    }

    // 处理查看学生详情按钮点击
    private void handleViewStudent(Student student) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/UserDetails_student.fxml"));
            Parent root = loader.load();
            //获取控制器
            UserDetailsController_student controller = loader.getController();
            //创建新窗口
            Stage stage = new Stage();
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("用户详情——"+student.getName());
            stage.setScene(new Scene(root));
            //窗口引用传递给控制器
            controller.setStage(stage);
            // 传递用户ID并加载数据
            controller.loadUserData(student.getId());
            //显示窗口
            stage.showAndWait();
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    // 处理删除学生按钮点击
    private void handleDeleteStudent(Student student) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除学生 " + student.getName() + "（" + student.getId() + "）吗？此操作不可撤销。");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String endpoint = "/admin/deleteUser";
            String urlWithParams = endpoint + "?userId=" + student.getId();

            NetworkUtils.postAsync(urlWithParams, null) // 使用 POST 方法，body 为 null
                    .thenAcceptAsync(response -> Platform.runLater(() -> {
                        try {
                            JsonObject res = gson.fromJson(response, JsonObject.class);
                            if (res.has("code") && res.get("code").getAsInt() == 200) {
                                ShowMessage.showInfoMessage("操作成功", "已删除学生: " + student.getName());
                                loadStudentsFromApi(Math.max(1, currentPage));
                            } else {
                                String errorMsg = res.has("msg") ? res.get("msg").getAsString() : "未知错误";
                                ShowMessage.showInfoMessage("删除失败", "删除学生失败: " + errorMsg);
                            }
                        } catch (Exception e) {
                            JsonObject res  = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")), JsonObject.class);
                            ShowMessage.showInfoMessage("处理错误", res.get("msg").getAsString());
                        }

                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            JsonObject res  = gson.fromJson(ex.getMessage().substring(ex.getMessage().indexOf("{")), JsonObject.class);
                            ShowMessage.showInfoMessage("处理错误", res.get("msg").getAsString());

                        });
                        return null;
                    });
            
        }
    }

    // 显示提示对话框
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    

}
