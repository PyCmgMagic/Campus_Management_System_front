package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.work.javafx.util.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
    @FXML private ComboBox<String> departmentComboBox;
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

    // 数据模型
    private ObservableList<Student> masterData = FXCollections.observableArrayList();
    private ObservableList<Student> filteredData = FXCollections.observableArrayList();
    private int itemsPerPage = 10;
    private final SimpleBooleanProperty selectAll = new SimpleBooleanProperty(false);
    static Gson gson =new Gson();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化筛选下拉框
        initializeComboBoxes();
        
        // 初始化表格列
        initializeTable();
        
        // 加载测试数据
        loadMockData();
        
        // 初始化分页控件
        initializePagination();
        
        // 更新表格和分页信息
        updateFilteredData();
        
        // 确保表格直接显示数据
        if (!filteredData.isEmpty()) {
            studentTable.setItems(FXCollections.observableArrayList(
                    filteredData.subList(0, Math.min(itemsPerPage, filteredData.size()))));
        }
    }

    // 初始化筛选下拉框选项
    private void initializeComboBoxes() {
        // 添加院系选项
        departmentComboBox.getItems().addAll(
            "全部院系",
            "计算机科学与技术学院",
            "电子信息学院",
            "数学科学学院",
            "外国语学院"
        );
        departmentComboBox.setValue("全部院系");
        
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
        departmentComboBox.setOnAction(e -> updateFilteredData());
        majorComboBox.setOnAction(e -> updateFilteredData());
        gradeComboBox.setOnAction(e -> updateFilteredData());
        statusComboBox.setOnAction(e -> updateFilteredData());
    }

    // 初始化表格列和单元格工厂
    private void initializeTable() {

        // 设置数据列
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
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
            private final Button editBtn = new Button();
            private final Button deleteBtn = new Button();
            private final HBox actionsBox = new HBox(10, viewBtn, editBtn, deleteBtn);
            
            {
                // 初始化按钮样式和提示
                viewBtn.getStyleClass().addAll("action-btn", "view-btn");
                viewBtn.setTooltip(new Tooltip("查看详情"));
                
                editBtn.getStyleClass().addAll("action-btn", "edit-btn");
                editBtn.setTooltip(new Tooltip("编辑"));
                
                deleteBtn.getStyleClass().addAll("action-btn", "delete-btn");
                deleteBtn.setTooltip(new Tooltip("删除"));
                
                actionsBox.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Student student, boolean empty) {
                super.updateItem(student, empty);
                
                if (empty || student == null) {
                    setGraphic(null);
                } else {
                    viewBtn.setOnAction(e -> handleViewStudent(student));
                    editBtn.setOnAction(e -> handleEditStudent(student));
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
        pagination.setPageCount(1);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
        
        // 设置分页样式
        pagination.getStyleClass().add("pagination");
    }

    // 创建分页页面内容
    private TableView<Student> createPage(int pageIndex) {
        int fromIndex = pageIndex * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, filteredData.size());
        
        // 设置表格数据为当前页的数据
        if (fromIndex <= toIndex && !filteredData.isEmpty()) {
            ObservableList<Student> pageData = FXCollections.observableArrayList(
                    filteredData.subList(fromIndex, toIndex));
            studentTable.setItems(pageData);
        } else {
            studentTable.setItems(FXCollections.observableArrayList());
        }
        
        // 重置全选状态
        selectAll.set(false);
        
        return studentTable;
    }


    // 加载模拟测试数据
    private void loadMockData() {
        masterData.addAll(
            new Student("202301001", "张三", "男", "计算机学院", "计算机科学与技术", "2023级", "计科2301", "在读"),
            new Student("202202015", "李四", "女", "电子信息学院", "电子信息工程", "2022级", "电信2202", "在读"),
            new Student("202103088", "王五", "男", "数学科学学院", "数学与应用数学", "2021级", "数学2101", "休学"),
            new Student("202001032", "赵六", "女", "计算机学院", "软件工程", "2020级", "软工2002", "毕业"),
            new Student("202301005", "孙七", "男", "计算机学院", "计算机科学与技术", "2023级", "计科2302", "在读"),
            new Student("202204033", "周八", "女", "外国语学院", "英语", "2022级", "英语2201", "在读"),
            new Student("202302011", "吴九", "男", "电子信息学院", "通信工程", "2023级", "通信2301", "在读"),
            new Student("202305022", "郑十", "女", "计算机学院", "软件工程", "2023级", "软工2301", "在读"),
            new Student("202102042", "钱十一", "男", "数学科学学院", "统计学", "2021级", "统计2102", "在读"),
            new Student("202001015", "孙十二", "女", "计算机学院", "计算机科学与技术", "2020级", "计科2003", "毕业"),
            new Student("202201023", "周十三", "男", "电子信息学院", "电子信息工程", "2022级", "电信2201", "在读"),
            new Student("202304007", "吴十四", "女", "外国语学院", "英语", "2023级", "英语2302", "在读")
        );
    }

    // 应用筛选条件并更新表格数据
    private void updateFilteredData() {
        String searchText = searchField.getText().toLowerCase().trim();
        String department = departmentComboBox.getValue();
        String major = majorComboBox.getValue();
        String grade = gradeComboBox.getValue();
        String status = statusComboBox.getValue();
        
        // 筛选数据
        filteredData.clear();
        for (Student student : masterData) {
            boolean matchesSearch = searchText.isEmpty() || 
                    student.getId().toLowerCase().contains(searchText) || 
                    student.getName().toLowerCase().contains(searchText);
                    
            boolean matchesDepartment = "全部院系".equals(department) || 
                    student.getDepartment().equals(department);
                    
            boolean matchesMajor = "全部专业".equals(major) || 
                    student.getMajor().equals(major);
                    
            boolean matchesGrade = "全部年级".equals(grade) || 
                    student.getGrade().equals(grade);
                    
            boolean matchesStatus = "全部状态".equals(status) || 
                    student.getStatus().equals(status);
                    
            if (matchesSearch && matchesDepartment && matchesMajor && matchesGrade && matchesStatus) {
                filteredData.add(student);
            }
        }
        
        // 更新分页控件
        int pageCount = (int) Math.ceil((double) filteredData.size() / itemsPerPage);
        pagination.setPageCount(Math.max(1, pageCount));
        pagination.setCurrentPageIndex(0);
        
        // 创建第一页内容
        createPage(0);
    }

    // 处理搜索按钮点击
    @FXML
    private void handleSearch() {

        updateFilteredData();
    }

    // 处理重置按钮点击
    @FXML
    private void handleReset() {
        searchField.clear();
        departmentComboBox.setValue("全部院系");
        majorComboBox.setValue("全部专业");
        gradeComboBox.setValue("全部年级");
        statusComboBox.setValue("全部状态");
        updateFilteredData();
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
        
        // 显示确认对话框
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除选中的 " + selectedStudents.size() + " 个学生吗？此操作不可撤销。");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 执行删除操作
            masterData.removeAll(selectedStudents);
            updateFilteredData();
            showAlert(Alert.AlertType.INFORMATION, "成功", "已成功删除 " + selectedStudents.size() + " 个学生");
        }
    }

    // 处理添加学生卡片点击
    @FXML
    private void handleAddStudent(MouseEvent event) {
        // 这里实现添加学生的逻辑
        showAlert(Alert.AlertType.INFORMATION, "功能提示", "添加学生功能尚未实现");
    }


    @FXML
    private void importStudents(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择学生信息文件");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel 文件", "*.xlsx", "*.xls")
        );

        // 获取当前窗口Stage，用于显示文件选择器
        Stage stage = (Stage) importStudentCard.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                 // 显示一个加载提示或禁用UI元素
                showAlert(Alert.AlertType.INFORMATION, "提示", "正在上传文件: " + selectedFile.getName() + "...");

                NetworkUtils.postMultipartFileAsync("/admin/upload", selectedFile)
                    .thenAcceptAsync(response -> {
                        // 在 UI 线程上更新
                        Platform.runLater(() -> {

                            JsonObject res = gson.fromJson(response,JsonObject.class);
                            if(res.has("code") && res.get("code").getAsInt()==200){
                                System.out.println( response); // 打印响应以供调试
                                showAlert(Alert.AlertType.INFORMATION, "成功", "文件上传成功！");
                                //刷新列表
                                 loadMockData();
                                 updateFilteredData();
                            }

                        });
                    }, Platform::runLater) // 确保 thenAcceptAsync 的回调也在UI线程执行
                    .exceptionally(ex -> {
                        // 在 UI 线程上更新
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "文件上传失败", ex);
                            showAlert(Alert.AlertType.ERROR, "错误", "文件上传失败: " + ex.getMessage());
                        });
                        return null;
                    });

            } catch (Exception e) {
                 // 处理调用 NetworkUtils 可能出现的直接异常（虽然异步调用本身不太可能在这里抛出）
                LOGGER.log(Level.SEVERE, "启动文件上传时出错", e);
                showAlert(Alert.AlertType.ERROR, "错误", "启动文件上传时出错: " + e.getMessage());
            }
        } else {
            System.out.println("未选择文件。");
            // 如果用户取消选择，显示提示
             showAlert(Alert.AlertType.INFORMATION, "提示", "未选择任何文件。");
        }
    }

    // 处理查看学生详情按钮点击
    private void handleViewStudent(Student student) {
        // 这里实现查看学生详情的逻辑
        showAlert(Alert.AlertType.INFORMATION, "学生详情", 
                "学号: " + student.getId() + "\n" +
                "姓名: " + student.getName() + "\n" +
                "性别: " + student.getGender() + "\n" +
                "院系: " + student.getDepartment() + "\n" +
                "专业: " + student.getMajor() + "\n" +
                "年级: " + student.getGrade() + "\n" +
                "班级: " + student.getClassName() + "\n" +
                "状态: " + student.getStatus());
    }

    // 处理编辑学生按钮点击
    private void handleEditStudent(Student student) {
        // 这里实现编辑学生的逻辑
        showAlert(Alert.AlertType.INFORMATION, "功能提示", "编辑学生功能尚未实现");
    }

    // 处理删除学生按钮点击
    private void handleDeleteStudent(Student student) {
        // 显示确认对话框
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除学生 " + student.getName() + "（" + student.getId() + "）吗？此操作不可撤销。");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 执行删除操作
            masterData.remove(student);
            updateFilteredData();
            showAlert(Alert.AlertType.INFORMATION, "成功", "已成功删除学生");
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

    // 学生数据模型类
    public static class Student {
        private final SimpleStringProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty gender;
        private final SimpleStringProperty department;
        private final SimpleStringProperty major;
        private final SimpleStringProperty grade;
        private final SimpleStringProperty className;
        private final SimpleStringProperty status;
        private final SimpleBooleanProperty selected;

        public Student(String id, String name, String gender, String department, 
                       String major, String grade, String className, String status) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.gender = new SimpleStringProperty(gender);
            this.department = new SimpleStringProperty(department);
            this.major = new SimpleStringProperty(major);
            this.grade = new SimpleStringProperty(grade);
            this.className = new SimpleStringProperty(className);
            this.status = new SimpleStringProperty(status);
            this.selected = new SimpleBooleanProperty(false);
        }

        // Getters and setters
        public String getId() { return id.get(); }
        public SimpleStringProperty idProperty() { return id; }
        public void setId(String id) { this.id.set(id); }

        public String getName() { return name.get(); }
        public SimpleStringProperty nameProperty() { return name; }
        public void setName(String name) { this.name.set(name); }

        public String getGender() { return gender.get(); }
        public SimpleStringProperty genderProperty() { return gender; }
        public void setGender(String gender) { this.gender.set(gender); }

        public String getDepartment() { return department.get(); }
        public SimpleStringProperty departmentProperty() { return department; }
        public void setDepartment(String department) { this.department.set(department); }

        public String getMajor() { return major.get(); }
        public SimpleStringProperty majorProperty() { return major; }
        public void setMajor(String major) { this.major.set(major); }

        public String getGrade() { return grade.get(); }
        public SimpleStringProperty gradeProperty() { return grade; }
        public void setGrade(String grade) { this.grade.set(grade); }

        public String getClassName() { return className.get(); }
        public SimpleStringProperty classNameProperty() { return className; }
        public void setClassName(String className) { this.className.set(className); }

        public String getStatus() { return status.get(); }
        public SimpleStringProperty statusProperty() { return status; }
        public void setStatus(String status) { this.status.set(status); }

        public boolean isSelected() { return selected.get(); }
        public SimpleBooleanProperty selectedProperty() { return selected; }
        public void setSelected(boolean selected) { this.selected.set(selected); }
    }
}
