package com.work.javafx.controller.admin;

import com.work.javafx.model.Course;
import com.work.javafx.model.CourseApplication;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CourseManagementController implements Initializable {

    // FXML主容器组件
    @FXML private ScrollPane rootPane;
    @FXML private VBox mainCoursesView;
    @FXML private VBox pendingCoursesView;
    @FXML private Label mainPageTitle;
    @FXML private HBox mainTitleContainer;
    @FXML private HBox pendingTitleContainer;
    @FXML private Hyperlink backToMainLink;
    
    // 快捷操作卡片
    @FXML private BorderPane reviewPendingCard;
    @FXML private BorderPane addCourseCard;
    @FXML private BorderPane exportCourseCard;
    @FXML private Label pendingBadge;
    
    // 搜索和筛选组件
    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private ComboBox<String> courseTypeFilter;
    @FXML private ComboBox<String> creditFilter;
    @FXML private ComboBox<String> statusFilter;
    
    // 批量操作按钮
    @FXML private Button batchStopBtn;
    @FXML private Button batchEditBtn;
    
    // 主课程表格组件
    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, String> codeColumn;
    @FXML private TableColumn<Course, String> nameColumn;
    @FXML private TableColumn<Course, String> departmentColumn;
    @FXML private TableColumn<Course, Integer> creditColumn;
    @FXML private TableColumn<Course, String> typeColumn;
    @FXML private TableColumn<Course, String> teacherColumn;
    @FXML private TableColumn<Course, String> statusColumn;
    @FXML private TableColumn<Course, Void> actionColumn;
    
    // 主课程分页组件
    @FXML private Pagination coursePagination;
    @FXML private Label pageInfo;
    
    // 待审核课程相关组件
    @FXML private TextField pendingSearchField;
    @FXML private TableView<CourseApplication> pendingCourseTable;
    @FXML private TableColumn<CourseApplication, String> pendingCodeColumn;
    @FXML private TableColumn<CourseApplication, String> pendingNameColumn;
    @FXML private TableColumn<CourseApplication, String> pendingDepartmentColumn;
    @FXML private TableColumn<CourseApplication, String> pendingApplicantColumn;
    @FXML private TableColumn<CourseApplication, String> pendingDateColumn;
    @FXML private TableColumn<CourseApplication, Integer> pendingCreditColumn;
    @FXML private TableColumn<CourseApplication, String> pendingTypeColumn;
    @FXML private TableColumn<CourseApplication, Void> pendingActionColumn;
    
    @FXML private Pagination pendingPagination;
    @FXML private Label pendingPageInfo;
    
    // 数据存储
    private ObservableList<Course> allCourses = FXCollections.observableArrayList();
    private ObservableList<Course> filteredCourses = FXCollections.observableArrayList();
    private ObservableList<CourseApplication> pendingCourses = FXCollections.observableArrayList();
    
    // 分页参数
    private final int ROWS_PER_PAGE = 10;
    private int totalPages = 0;
    //

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化筛选器
        initFilters();
        
        // 初始化表格
        initCourseTable();
        initPendingCourseTable();
        
        // 加载模拟数据
        loadMockData();
        
        // 初始化分页
        initPagination();
        
        // 更新待审批徽章
        updatePendingBadge();
    }
    
    // 初始化筛选器
    private void initFilters() {
        departmentFilter.setItems(FXCollections.observableArrayList(
                "全部院系", "计算机学院", "数学学院", "物理学院", "外语学院", "经济管理学院"
        ));
        departmentFilter.getSelectionModel().selectFirst();
        
        courseTypeFilter.setItems(FXCollections.observableArrayList(
                "全部类型", "必修课", "选修课", "公共课", "专业课"
        ));
        courseTypeFilter.getSelectionModel().selectFirst();
        
        creditFilter.setItems(FXCollections.observableArrayList(
                "全部学分", "1学分", "2学分", "3学分", "4学分", "5学分"
        ));
        creditFilter.getSelectionModel().selectFirst();
        
        statusFilter.setItems(FXCollections.observableArrayList(
                "全部状态", "开设中", "已停开"
        ));
        statusFilter.getSelectionModel().selectFirst();
        
        // 添加筛选器监听器
        departmentFilter.setOnAction(e -> applyFilters());
        courseTypeFilter.setOnAction(e -> applyFilters());
        creditFilter.setOnAction(e -> applyFilters());
        statusFilter.setOnAction(e -> applyFilters());
    }
    
    // 初始化主课程表格
    private void initCourseTable() {
//        // 设置选择列
//        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
//        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
//        selectColumn.setEditable(true);
        
        // 设置其他列
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        creditColumn.setCellValueFactory(new PropertyValueFactory<>("credit"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        teacherColumn.setCellValueFactory(new PropertyValueFactory<>("teacher"));
        
        // 状态列自定义显示
        statusColumn.setCellValueFactory(cellData -> {
            Boolean isActive = cellData.getValue().getIsActive();
            return new SimpleStringProperty(isActive ? "开设中" : "已停开");
        });
        statusColumn.setCellFactory(column -> new TableCell<Course, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    // Create a more compact display
                    Label statusLabel = new Label(item);
                    statusLabel.getStyleClass().add("status-badge");
                    if ("开设中".equals(item)) {
                        statusLabel.getStyleClass().add("status-active");
                    } else {
                        statusLabel.getStyleClass().add("status-inactive");
                    }
                    
                    setGraphic(statusLabel);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        
        // 操作列
        actionColumn.setCellFactory(createActionCellFactory());
        
        // 设置表格允许编辑
        courseTable.setEditable(true);
    }
    
    // 创建操作列工厂
    private Callback<TableColumn<Course, Void>, TableCell<Course, Void>> createActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Course, Void> call(TableColumn<Course, Void> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button();
                    private final Button editBtn = new Button();
                    private final Button deleteBtn = new Button();
                    private final HBox actionBox = new HBox(5, viewBtn, editBtn, deleteBtn);
                    
                    {
                        viewBtn.getStyleClass().addAll("table-button", "default-btn");
                        editBtn.getStyleClass().addAll("table-button", "warning-btn");
                        deleteBtn.getStyleClass().addAll("table-button", "danger-btn");
                        
                        // 添加图标
                        Region viewIcon = new Region();
                        viewIcon.getStyleClass().add("view-icon");
                        viewBtn.setGraphic(viewIcon);
                        
                        Region editIcon = new Region();
                        editIcon.getStyleClass().add("edit-icon");
                        editBtn.setGraphic(editIcon);
                        
                        Region deleteIcon = new Region();
                        deleteIcon.getStyleClass().add("delete-icon");
                        deleteBtn.setGraphic(deleteIcon);
                        
                        // 设置按钮点击事件
                        viewBtn.setOnAction(event -> viewCourse(getIndex()));
                        editBtn.setOnAction(event -> editCourse(getIndex()));
                        deleteBtn.setOnAction(event -> stopCourse(getIndex()));
                        
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
    
    // 初始化待审批课程表格
    private void initPendingCourseTable() {
        // 设置列数据绑定
        pendingCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        pendingNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        pendingDepartmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        pendingApplicantColumn.setCellValueFactory(new PropertyValueFactory<>("applicantName"));
        pendingDateColumn.setCellValueFactory(new PropertyValueFactory<>("applicationDate"));
        pendingCreditColumn.setCellValueFactory(new PropertyValueFactory<>("credit"));
        pendingTypeColumn.setCellValueFactory(new PropertyValueFactory<>("courseType"));
        
        // 操作列
        pendingActionColumn.setCellFactory(createPendingActionCellFactory());
    }
    
    // 创建待审批操作列工厂
    private Callback<TableColumn<CourseApplication, Void>, TableCell<CourseApplication, Void>> createPendingActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<CourseApplication, Void> call(TableColumn<CourseApplication, Void> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button();
                    private final Button approveBtn = new Button();
                    private final Button rejectBtn = new Button();
                    private final HBox actionBox = new HBox(5, viewBtn, approveBtn, rejectBtn);
                    
                    {
                        viewBtn.getStyleClass().addAll("table-button", "default-btn");
                        approveBtn.getStyleClass().addAll("table-button", "success-btn");
                        rejectBtn.getStyleClass().addAll("table-button", "warning-btn");
                        
                        // 添加图标
                        Region viewIcon = new Region();
                        viewIcon.getStyleClass().add("view-icon");
                        viewBtn.setGraphic(viewIcon);
                        
                        Region approveIcon = new Region();
                        approveIcon.getStyleClass().add("approve-icon");
                        approveBtn.setGraphic(approveIcon);
                        
                        Region rejectIcon = new Region();
                        rejectIcon.getStyleClass().add("reject-icon");
                        rejectBtn.setGraphic(rejectIcon);
                        
                        // 设置按钮点击事件
                        viewBtn.setOnAction(event -> viewPendingCourse(getIndex()));
                        approveBtn.setOnAction(event -> approveCourse(getIndex()));
                        rejectBtn.setOnAction(event -> rejectCourse(getIndex()));
                        
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
    
    // 加载模拟数据
    private void loadMockData() {
        // 添加课程数据
        allCourses.addAll(
            new Course("CS101", "计算机导论", "计算机学院", 3, "必修课", "张教授", true),
            new Course("CS102", "程序设计基础", "计算机学院", 4, "必修课", "李教授", false),
            new Course("CS201", "数据结构", "计算机学院", 4, "必修课", "王教授", true),
            new Course("CS202", "算法设计与分析", "计算机学院", 3, "专业课", "赵教授", true),
            new Course("CS301", "操作系统", "计算机学院", 4, "专业课", "钱教授", true),
            new Course("CS302", "编译原理", "计算机学院", 3, "专业课", "孙教授", true),
            new Course("CS401", "人工智能", "计算机学院", 3, "选修课", "周教授", true),
            new Course("CS402", "机器学习", "计算机学院", 3, "选修课", "吴教授", true),
            new Course("MA101", "高等数学", "数学学院", 5, "公共课", "郑教授", true),
            new Course("MA201", "线性代数", "数学学院", 3, "公共课", "冯教授", true),
            new Course("PH101", "大学物理", "物理学院", 4, "公共课", "陈教授", true),
            new Course("EN101", "大学英语", "外语学院", 3, "公共课", "楚教授", true),
            new Course("EC101", "微观经济学", "经济管理学院", 3, "选修课", "魏教授", false),
            new Course("EC102", "宏观经济学", "经济管理学院", 3, "选修课", "蒋教授", false),
            new Course("CS501", "云计算", "计算机学院", 2, "选修课", "沈教授", false)
        );
        
        // 添加待审批课程申请
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        pendingCourses.addAll(
            new CourseApplication("CS601", "区块链技术", "计算机学院", "刘教授", LocalDate.now().format(formatter), 3, "选修课"),
            new CourseApplication("CS602", "量子计算", "计算机学院", "何教授", LocalDate.now().minusDays(1).format(formatter), 4, "选修课"),
            new CourseApplication("EC301", "金融科技", "经济管理学院", "梁教授", LocalDate.now().minusDays(2).format(formatter), 2, "选修课")
        );
        
        // 初始显示所有课程
        filteredCourses.addAll(allCourses);
        courseTable.setItems(filteredCourses);
        
        // 设置待审批课程
        pendingCourseTable.setItems(pendingCourses);
    }
    
    // 初始化分页
    private void initPagination() {
        totalPages = (int) Math.ceil((double) filteredCourses.size() / ROWS_PER_PAGE);
        coursePagination.setPageCount(totalPages);
        coursePagination.setCurrentPageIndex(0);
        
        coursePagination.setPageFactory(this::createCoursePage);
        
        updatePageInfo();
        
        // 初始化待审批分页
        pendingPagination.setPageCount((int) Math.ceil((double) pendingCourses.size() / ROWS_PER_PAGE));
        pendingPagination.setCurrentPageIndex(0);
        pendingPagination.setPageFactory(this::createPendingPage);
        
        updatePendingPageInfo();
    }
    
    // 创建课程分页
    private Node createCoursePage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredCourses.size());
        
        courseTable.setItems(FXCollections.observableArrayList(
                filteredCourses.subList(fromIndex, toIndex)));
        
        return new VBox(courseTable);
    }
    
    // 创建待审批分页
    private Node createPendingPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, pendingCourses.size());
        
        pendingCourseTable.setItems(FXCollections.observableArrayList(
                pendingCourses.subList(fromIndex, toIndex)));
        
        return new VBox(pendingCourseTable);
    }
    
    // 更新页面信息
    private void updatePageInfo() {
        int currentPage = coursePagination.getCurrentPageIndex();
        int fromIndex = currentPage * ROWS_PER_PAGE + 1;
        int toIndex = Math.min((currentPage + 1) * ROWS_PER_PAGE, filteredCourses.size());
        
        if (filteredCourses.isEmpty()) {
            pageInfo.setText("共 0 条记录");
        } else {
            pageInfo.setText(String.format("共 %d 条记录，当前显示 %d-%d 条", 
                    filteredCourses.size(), fromIndex, toIndex));
        }
    }
    
    // 更新待审批页面信息
    private void updatePendingPageInfo() {
        pendingPageInfo.setText(String.format("共 %d 条待审批记录", pendingCourses.size()));
    }
    
    // 更新待审批徽章
    private void updatePendingBadge() {
        pendingBadge.setText(String.valueOf(pendingCourses.size()));
        pendingBadge.setVisible(pendingCourses.size() > 0);
    }
    
    // 应用筛选器
    private void applyFilters() {
        filteredCourses.clear();
        
        String department = departmentFilter.getValue().equals("全部院系") ? "" : departmentFilter.getValue();
        String type = courseTypeFilter.getValue().equals("全部类型") ? "" : courseTypeFilter.getValue();
        String creditStr = creditFilter.getValue().equals("全部学分") ? "" : creditFilter.getValue();
        String status = statusFilter.getValue().equals("全部状态") ? "" : statusFilter.getValue();
        
        Integer credit = null;
        if (!creditStr.isEmpty()) {
            credit = Integer.parseInt(creditStr.replace("学分", ""));
        }
        
        Boolean isActive = null;
        if (!status.isEmpty()) {
            isActive = status.equals("开设中");
        }
        
        for (Course course : allCourses) {
            if ((department.isEmpty() || course.getDepartment().equals(department))
                    && (type.isEmpty() || course.getType().equals(type))
                    && (credit == null || course.getCredit() == credit)
                    && (isActive == null || course.getIsActive() == isActive)) {
                filteredCourses.add(course);
            }
        }
        
        // 更新分页
        totalPages = (int) Math.ceil((double) filteredCourses.size() / ROWS_PER_PAGE);
        coursePagination.setPageCount(totalPages > 0 ? totalPages : 1);
        coursePagination.setCurrentPageIndex(0);
        
        updatePageInfo();
    }
    
    // FXML事件处理方法
    
    @FXML
    private void searchCourses() {
        String searchTerm = searchField.getText().toLowerCase().trim();
        
        if (searchTerm.isEmpty()) {
            applyFilters();
            return;
        }
        
        // 先应用其他筛选器
        applyFilters();
        
        // 再应用搜索条件
        List<Course> searchResults = filteredCourses.stream()
                .filter(course -> 
                    course.getCode().toLowerCase().contains(searchTerm) ||
                    course.getName().toLowerCase().contains(searchTerm) ||
                    course.getTeacher().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
        
        filteredCourses.clear();
        filteredCourses.addAll(searchResults);
        
        // 更新分页
        totalPages = (int) Math.ceil((double) filteredCourses.size() / ROWS_PER_PAGE);
        coursePagination.setPageCount(totalPages > 0 ? totalPages : 1);
        coursePagination.setCurrentPageIndex(0);
        
        updatePageInfo();
    }
    
    @FXML
    private void resetFilters() {
        searchField.clear();
        departmentFilter.getSelectionModel().selectFirst();
        courseTypeFilter.getSelectionModel().selectFirst();
        creditFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();
        
        filteredCourses.clear();
        filteredCourses.addAll(allCourses);
        
        // 更新分页
        totalPages = (int) Math.ceil((double) filteredCourses.size() / ROWS_PER_PAGE);
        coursePagination.setPageCount(totalPages);
        coursePagination.setCurrentPageIndex(0);
        
        updatePageInfo();
    }
    
    @FXML
    private void searchPendingCourses() {
        String searchTerm = pendingSearchField.getText().toLowerCase().trim();
        
        if (searchTerm.isEmpty()) {
            pendingCourseTable.setItems(pendingCourses);
            return;
        }
        
        ObservableList<CourseApplication> searchResults = pendingCourses.filtered(app -> 
                app.getCourseName().toLowerCase().contains(searchTerm) ||
                app.getApplicantName().toLowerCase().contains(searchTerm) ||
                app.getCourseCode().toLowerCase().contains(searchTerm));
        
        pendingCourseTable.setItems(searchResults);
        
        // 更新分页
        pendingPagination.setPageCount((int) Math.ceil((double) searchResults.size() / ROWS_PER_PAGE));
        pendingPagination.setCurrentPageIndex(0);
        
        updatePendingPageInfo();
    }
    
    @FXML
    private void showMainView() {
        mainCoursesView.setVisible(true);
        pendingCoursesView.setVisible(false);
        mainTitleContainer.setVisible(true);
        pendingTitleContainer.setVisible(false);
    }
    
    @FXML
    private void showPendingView() {
        mainCoursesView.setVisible(false);
        pendingCoursesView.setVisible(true);
        mainTitleContainer.setVisible(false);
        pendingTitleContainer.setVisible(true);
    }
    
    @FXML
    private void showAddCourseView() {
        // 显示添加课程对话框
        showInfoDialog("功能提示", "添加课程功能将在后续版本开放");
    }
    
    @FXML
    private void exportCourses() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出课程数据");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel文件", "*.xlsx"));
        fileChooser.setInitialFileName("课程数据.xlsx");
        
        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file != null) {
            // 导出课程数据到Excel
            showInfoDialog("导出成功", "课程数据已成功导出到: " + file.getAbsolutePath());
        }
    }
    
    @FXML
    private void batchStopCourses() {
        List<Course> selectedCourses = getSelectedCourses();
        if (selectedCourses.isEmpty()) {
            showErrorDialog("操作失败", "请先选择要停开的课程");
            return;
        }
        
        if (showConfirmDialog("确认操作", "确定要停开已选择的 " + selectedCourses.size() + " 门课程吗？")) {
            selectedCourses.forEach(course -> course.setIsActive(false));
            courseTable.refresh();
            showInfoDialog("操作成功", "已停开 " + selectedCourses.size() + " 门课程");
        }
    }
    
    @FXML
    private void batchEditCourses() {
        List<Course> selectedCourses = getSelectedCourses();
        if (selectedCourses.isEmpty()) {
            showErrorDialog("操作失败", "请先选择要修改的课程");
            return;
        }
        
        showInfoDialog("功能提示", "批量修改功能将在后续版本开放");
    }
    
    // 获取选中的课程
    private List<Course> getSelectedCourses() {
        return allCourses.stream()
                .filter(Course::getSelected)
                .collect(Collectors.toList());
    }
    
    // 课程操作方法
    private void viewCourse(int index) {
        Course course = courseTable.getItems().get(index);
        showInfoDialog("查看课程", "课程详情: " + course.getName());
    }
    
    private void editCourse(int index) {
        Course course = courseTable.getItems().get(index);
        showInfoDialog("编辑课程", "编辑课程: " + course.getName());
    }
    
    private void stopCourse(int index) {
        Course course = courseTable.getItems().get(index);
        
        if (!course.getIsActive()) {
            showInfoDialog("操作提示", "该课程已经处于停开状态");
            return;
        }
        
        if (showConfirmDialog("确认操作", "确定要停开课程 " + course.getName() + " 吗？")) {
            course.setIsActive(false);
            courseTable.refresh();
            showInfoDialog("操作成功", "已停开课程: " + course.getName());
        }
    }
    
    // 待审批课程操作方法
    private void viewPendingCourse(int index) {
        CourseApplication application = pendingCourseTable.getItems().get(index);
        showInfoDialog("查看申请", "申请详情: " + application.getCourseName());
    }
    
    private void approveCourse(int index) {
        CourseApplication application = pendingCourseTable.getItems().get(index);
        
        if (showConfirmDialog("确认操作", "确定要通过课程 " + application.getCourseName() + " 的申请吗？")) {
            // 添加到课程表
            Course newCourse = new Course(
                    application.getCourseCode(),
                    application.getCourseName(),
                    application.getDepartment(),
                    application.getCredit(),
                    application.getCourseType(),
                    application.getApplicantName(),
                    true
            );
            
            allCourses.add(newCourse);
            
            // 从待审批列表中移除
            pendingCourses.remove(application);
            
            // 刷新数据
            applyFilters();
            updatePendingBadge();
            updatePendingPageInfo();
            
            showInfoDialog("操作成功", "已批准课程申请: " + application.getCourseName());
        }
    }
    
    private void rejectCourse(int index) {
        CourseApplication application = pendingCourseTable.getItems().get(index);
        
        if (showConfirmDialog("确认操作", "确定要拒绝课程 " + application.getCourseName() + " 的申请吗？")) {
            // 从待审批列表中移除
            pendingCourses.remove(application);
            
            // 更新页面
            updatePendingBadge();
            updatePendingPageInfo();
            
            showInfoDialog("操作成功", "已拒绝课程申请: " + application.getCourseName());
        }
    }
    
    // 对话框辅助方法
    private void showInfoDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showErrorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
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
}
