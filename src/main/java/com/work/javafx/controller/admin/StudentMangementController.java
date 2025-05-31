package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.work.javafx.model.Student;
import com.work.javafx.util.ResUtil;
import com.work.javafx.util.ShowMessage;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.work.javafx.util.NetworkUtils;

import java.io.IOException;
import java.net.URL;
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

    private Stage progressDialog;

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
                "软件工程",
                "数字媒体技术",
                "大数据",
                "AI"
        );
        majorComboBox.setValue("全部专业");

        // 添加年级选项
        gradeComboBox.getItems().addAll(
                "全部年级",
                "2024",
                "2023",
                "2022",
                "2021",
                "2020"
        );
        gradeComboBox.setValue("全部年级");

        // 添加筛选监听器
        majorComboBox.setOnAction(e -> handleFilterChange());
        gradeComboBox.setOnAction(e -> handleFilterChange());
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
            private final Button resetBtn = new Button();
            private final HBox actionsBox = new HBox(10, viewBtn, resetBtn,deleteBtn);

            {
                // 初始化按钮样式和提示
                viewBtn.getStyleClass().addAll("table-button", "default-btn");
                resetBtn.getStyleClass().addAll("table-button", "default-btn");
                deleteBtn.getStyleClass().addAll("table-button", "danger-btn");
                Tooltip tooltip = new Tooltip("重置密码");
                resetBtn.setTooltip(tooltip);
                // 添加图标
                Region viewIcon = new Region();
                viewIcon.getStyleClass().add("view-icon");
                viewBtn.setGraphic(viewIcon);

                Region editIcon = new Region();
                editIcon.getStyleClass().add("reset-icon");
                resetBtn.setGraphic(editIcon);
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
                    resetBtn.setOnAction(e -> resetPassword(student));

                    setGraphic(actionsBox);
                }
            }
        });

        // 设置表格可编辑
        studentTable.setEditable(true);

        // 设置表格选择模式
        studentTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }


    // 初始化分页控件
    private void initializePagination() {
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);

        // 添加页码变化监听器
        pageChangeListener = (obs, oldIndex, newIndex) -> {
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
        updatePageInfo();
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
                if(majorFilter.equals("软件工程")){
                    params.put("major","0");
                }if(majorFilter.equals("数字媒体技术")){
                    params.put("major","1");
                }if(majorFilter.equals("大数据")){
                    params.put("major","2");
                }if(majorFilter.equals("AI")){
                    params.put("major","3");
                }
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

                                    String studentApiStatus = "STUDYING";
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
                            } else {
                                String errorMsg = jsonResponse.has("msg")
                                        ? jsonResponse.get("msg").getAsString()
                                        : "加载学生数据失败";
                                showAlert(Alert.AlertType.ERROR, "错误", errorMsg);
                                masterData.clear();
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "解析API响应失败", e);
                            showAlert(Alert.AlertType.ERROR, "错误", "解析API响应失败: " + e.getMessage());
                            masterData.clear();
                        } finally {
                            // 无论如何都要解除加载锁定，启用UI
                            isPageLoadingLocked = false;
                            studentTable.setDisable(false);
                            updatePageInfo();
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
                                    } else {
                                    }

                                    int gradeYear = 0;
                                    if (studentJson.has("grade") && !studentJson.get("grade").isJsonNull()) {
                                        try {
                                            gradeYear = studentJson.get("grade").getAsInt();
                                        } catch (NumberFormatException e) {
                                        }
                                    }

                                    String sectionStr = "未分班";
                                    if (studentJson.has("section") && !studentJson.get("section").isJsonNull()) {
                                        try {
                                            sectionStr = studentJson.get("section").getAsInt() + "";
                                        } catch (NumberFormatException | UnsupportedOperationException e) {
                                            try {
                                                sectionStr = studentJson.get("section").getAsString();
                                            } catch (UnsupportedOperationException e2){
                                                // sectionStr 保持默认值 "未分班"
                                            }
                                        }
                                    }
                                    String fullsection = major + sectionStr + "班";

                                    String studentApiStatus = "STUDYING";
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

                                // 保存API返回的数据到模型
                                totalItems = apiTotalItems;
                                totalPages = apiTotalPages;
                                currentPage = apiCurrentPage;

                                if (requestedPage == apiCurrentPage) {
                                    pagination.currentPageIndexProperty().removeListener(pageChangeListener);
                                    pagination.setPageCount(Math.max(1, totalPages));
                                    pagination.setCurrentPageIndex(currentPage - 1);
                                    pagination.currentPageIndexProperty().addListener(pageChangeListener);
                                    masterData.setAll(newData);
                                    if (studentTable.getItems() != masterData) {
                                        studentTable.setItems(masterData);
                                    }
                                }
                            } else {
                                String errorMsg = jsonResponse.has("msg")
                                        ? jsonResponse.get("msg").getAsString()
                                        : "加载学生数据失败";
                                showAlert(Alert.AlertType.ERROR, "错误", errorMsg);
                                masterData.clear();
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "解析API响应失败", e);
                            showAlert(Alert.AlertType.ERROR, "错误", "解析API响应失败: " + e.getMessage());
                            masterData.clear();
                        } finally {
                            isPageLoadingLocked = false;
                            studentTable.setDisable(false);
                            updatePageInfo();
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

    // 获取选中的年级值
    private String getSelectedGradeValue() {
        String gradeText = gradeComboBox.getValue();
        if (gradeText == null || gradeText.equals("全部年级")) {
            return "";
        }
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
        loadStudentsFromApi(1);
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
            loadStudentsFromApi(currentPage);
        }catch (Exception e){
            LOGGER.log(Level.SEVERE, "Error opening add student window", e);
            showAlert(Alert.AlertType.ERROR, "错误", "无法打开添加学生窗口: " + e.getMessage());
        }
    }

    private void showProgressDialog(String title, String message) {
        progressDialog = new Stage();
        progressDialog.initModality(Modality.APPLICATION_MODAL);
        if (importStudentCard != null && importStudentCard.getScene() != null && importStudentCard.getScene().getWindow() != null) {
            progressDialog.initOwner(importStudentCard.getScene().getWindow());
        }
        progressDialog.initStyle(StageStyle.UNDECORATED);

        VBox vbox = new VBox();
        vbox.setSpacing(15);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(25));
        vbox.setStyle("-fx-background-color: white; -fx-border-color: #B0B0B0; -fx-border-width: 1px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");

        ProgressIndicator pIndicator = new ProgressIndicator();
        pIndicator.setPrefSize(60, 60);

        Label lblMessage = new Label(message);
        lblMessage.setStyle("-fx-font-size: 14px;");

        vbox.getChildren().addAll(pIndicator, lblMessage);

        Scene scene = new Scene(vbox);
        progressDialog.setScene(scene);
        progressDialog.setTitle(title);
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.close();
        }
        progressDialog = null;
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
            showProgressDialog("文件上传", "正在上传 " + selectedFile.getName() + "，请稍候...");
            try {
                System.out.println("开始上传: " + selectedFile.getName());
                NetworkUtils.postMultipartFileAsync("/admin/upload", selectedFile)
                        .thenAcceptAsync(response -> {
                            closeProgressDialog();
                            try {
                                JsonObject res = gson.fromJson(response, JsonObject.class);
                                if (res.has("code") && res.get("code").getAsInt() == 200) {
                                    System.out.println("上传成功响应: " + response);
                                    showAlert(Alert.AlertType.INFORMATION, "成功", "文件上传成功！");
                                    loadStudentsFromApi(1);
                                } else {
                                    String errorMsg = res.has("msg") ? res.get("msg").getAsString() : "文件上传失败，服务器返回错误。";
                                    LOGGER.warning("文件上传响应错误: " + response);
                                    showAlert(Alert.AlertType.ERROR, "上传响应错误", errorMsg);
                                }
                            } catch (Exception jsonEx) {
                                LOGGER.log(Level.SEVERE, "解析上传响应失败: " + response, jsonEx);
                                showAlert(Alert.AlertType.ERROR, "错误", "解析服务器响应失败: " + jsonEx.getMessage());
                            }
                        }, Platform::runLater)
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                closeProgressDialog();
                                LOGGER.log(Level.SEVERE, "文件上传彻底失败", ex);
                                String errorMessage = "文件上传失败: ";
                                if (ex.getCause() != null && ex.getCause().getMessage() != null) {
                                    errorMessage += ex.getCause().getMessage();
                                } else if (ex.getMessage() != null) {
                                    errorMessage += ex.getMessage();
                                } else {
                                    errorMessage += "未知网络或服务器错误。";
                                }
                                showAlert(Alert.AlertType.ERROR, "错误", errorMessage);
                            });
                            return null;
                        });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    closeProgressDialog();
                    LOGGER.log(Level.SEVERE, "启动文件上传时出错", e);
                    String msg = ResUtil.getMsgFromException(e);
                    showAlert(Alert.AlertType.ERROR, "错误", "启动文件上传时出错: " + msg);
                });
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
            UserDetailsController_student controller = loader.getController();
            Stage stage = new Stage();
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("用户详情——"+student.getName());
            stage.setScene(new Scene(root));
            controller.setStage(stage);
            controller.loadUserData(student.getId());
            stage.showAndWait();

        }catch (Exception e){
            LOGGER.log(Level.SEVERE, "Error opening student details window", e);
            showAlert(Alert.AlertType.ERROR, "错误", "无法打开学生详情窗口: " + e.getMessage());
        }
    }

    // 处理删除学生按钮点击
    private void handleDeleteStudent(Student student) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除学生 " + student.getName() + "（学号：" + student.getSduid() + "）吗？此操作不可撤销。");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String endpoint = "/admin/deleteUser";
            String urlWithParams = endpoint + "?userId=" + student.getId();

            NetworkUtils.postAsync(urlWithParams, null)
                    .thenAcceptAsync(response -> {
                        try {
                            JsonObject res = gson.fromJson(response, JsonObject.class);
                            if (res.has("code") && res.get("code").getAsInt() == 200) {
                                ShowMessage.showInfoMessage("操作成功", "已删除学生: " + student.getName());
                                loadStudentsFromApi(masterData.size() == 1 && currentPage > 1 ? currentPage -1 : currentPage);
                            } else {
                                String errorMsg = res.has("msg") ? res.get("msg").getAsString() : "未知错误";
                                ShowMessage.showErrorMessage("删除失败", "删除学生失败: " + errorMsg);
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "处理删除响应失败", e);
                            String errorMsg = "处理删除响应时出错";
                            try {
                                JsonObject resEx = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")), JsonObject.class);
                                if (resEx.has("msg")) errorMsg = resEx.get("msg").getAsString();
                            } catch (Exception ignored) { }
                            ShowMessage.showErrorMessage("处理错误", errorMsg);
                        }
                    }, Platform::runLater)
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "删除学生请求失败", ex);
                            String errorMsg = "删除学生请求失败";
                            try {
                                JsonObject resEx = gson.fromJson(ex.getMessage().substring(ex.getMessage().indexOf("{")), JsonObject.class);
                                if (resEx.has("msg")) errorMsg = resEx.get("msg").getAsString();
                            } catch (Exception parseEx) {}
                            ShowMessage.showErrorMessage("请求错误", errorMsg);
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

    private void resetPassword(Student student) {
        if (ShowMessage.showConfirmMessage("确认操作", "确定要重置学生 " + student.getName() + " (学号: "+ student.getSduid() +") 的密码为默认密码 (123456) 吗？")) {
            Map<String,String> params = new HashMap<>();
            params.put("userId",student.getId());
            NetworkUtils.post("/user/resetPassword", params, "", new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) throws IOException {
                    Platform.runLater(() -> {
                        JsonObject res = gson.fromJson(result,JsonObject.class);
                        if(res.get("code").getAsInt() == 200){
                            ShowMessage.showInfoMessage("操作成功", "已重置学生 " + student.getName() + " 的密码为 (123456)");
                        }else{
                            ShowMessage.showErrorMessage("操作失败",res.get("msg").getAsString());
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "重置密码失败", e);
                        String errorMsg = "重置密码失败";
                        try {
                            JsonObject res = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")),JsonObject.class);
                            if(res.has("msg")) errorMsg = res.get("msg").getAsString();
                        } catch(Exception ignore) {  }
                        ShowMessage.showErrorMessage("重置失败", errorMsg);
                    });
                }
            });
        }
    }
}