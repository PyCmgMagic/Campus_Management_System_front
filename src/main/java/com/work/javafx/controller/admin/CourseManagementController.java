package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.work.javafx.controller.teacher.CourseManagementContent;
import com.work.javafx.model.Course;
import com.work.javafx.model.CourseApplication;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ShowMessage;
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
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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
    static Gson gson = new Gson();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化筛选器
        initFilters();
        // 加载数据
        loadData();

        // 初始化表格
        initCourseTable();
        initPendingCourseTable();
        

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

        // 设置其他列
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
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
        pendingCodeColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        pendingNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        pendingDepartmentColumn.setCellValueFactory(new PropertyValueFactory<>("college"));
        pendingApplicantColumn.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
        pendingCreditColumn.setCellValueFactory(new PropertyValueFactory<>("point"));
        pendingTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        
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
    private void loadData() {
        // 获取课程列表
        fetchCourseList(1, ROWS_PER_PAGE);
        
        // 添加待审批课程申请
        //获取数据
        NetworkUtils.get("/class/pending", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject res = gson.fromJson(result, JsonObject.class);

                if(res.has("code") && res.get("code").getAsInt()==200){
                    JsonArray dataArray = res.getAsJsonArray("data");
                    Type courseListType = new TypeToken<List<CourseApplication>>() {}.getType();
                    List<CourseApplication> loadedApplications = gson.fromJson(dataArray, courseListType);
                    pendingCourses.clear();
                    pendingCourses.addAll(loadedApplications);
                    updatePendingPageInfo();
                    updatePendingBadge();
                    pendingCourseTable.setItems(pendingCourses);
                    pendingCourseTable.refresh();
                    pendingPagination.setPageCount((int) Math.ceil((double) pendingCourses.size() / ROWS_PER_PAGE));
                    pendingPagination.setCurrentPageIndex(0);

                    System.out.println("成功加载 " + pendingCourses.size() + " 条待审批课程。");

                }
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("加载待审批课程失败: " + e.getMessage());
            }
        });
    }
    
    // 从后端获取课程列表
    private void fetchCourseList(int pageNum, int pageSize) {
        // 设置请求参数
        String term = "2024-2025-1";
        String url = "/class/list?term=" + term + "&pageNum=" + pageNum + "&pageSize=" + pageSize;
        
        NetworkUtils.get(url, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                
                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    JsonObject data = res.getAsJsonObject("data");
                    JsonArray courseList = data.getAsJsonArray("list");
                    int totalItems = data.get("total").getAsInt();
                    int totalPages = data.get("pages").getAsInt();
                    
                    // 清空现有课程列表
                    allCourses.clear();
                    
                    // 解析课程数据
                    for (int i = 0; i < courseList.size(); i++) {
                        JsonObject courseItem = courseList.get(i).getAsJsonObject();
                        
                        // 假设从API返回的数据中的status字段表示课程状态
                        boolean isActive = "已通过".equals(courseItem.get("status").getAsString());
                        
                        Course course = new Course(
                            String.valueOf(courseItem.get("id").getAsInt()),
                            courseItem.get("name").getAsString(),
                            "", // department不在API返回中，默认为空
                            courseItem.get("point").getAsInt(),
                            "", // type不在API返回中，默认为空
                            courseItem.get("teacherName").getAsString(),
                            isActive
                        );
                        
                        // 添加额外信息到Course对象
                        course.setClassNum(courseItem.get("classNum").getAsString());
                        course.setPeopleNum(courseItem.get("peopleNum").getAsInt());
                        course.setTerm(courseItem.get("term").getAsString());
                        course.setStatus(courseItem.get("status").getAsString());
                        
                        allCourses.add(course);
                    }
                    
                    // 更新表格内容
                    filteredCourses.clear();
                    filteredCourses.addAll(allCourses);
                    courseTable.setItems(filteredCourses);
                    
                    // 更新分页
                    coursePagination.setPageCount(totalPages > 0 ? totalPages : 1);
                    
                    updatePageInfo();
                    
                    System.out.println("成功加载 " + allCourses.size() + " 门课程，总计 " + totalItems + " 门。");
                } else {
                    System.err.println("加载课程失败: " + res.get("msg").getAsString());
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                System.err.println("加载课程失败: " + e.getMessage());
            }
        });
    }
    
    // 初始化分页
    private void initPagination() {
        totalPages = (int) Math.ceil((double) filteredCourses.size() / ROWS_PER_PAGE);
        coursePagination.setPageCount(totalPages);
        coursePagination.setCurrentPageIndex(0);
        
        // 修改分页工厂，使其在页面改变时从API获取数据
        coursePagination.setPageFactory(pageIndex -> {
            // 当页面改变时从API获取数据
            fetchCourseList(pageIndex + 1, ROWS_PER_PAGE);
            return new VBox(courseTable);
        });
        
        updatePageInfo();
        
        // 初始化待审批分页
        pendingPagination.setPageCount((int) Math.ceil((double) pendingCourses.size() / ROWS_PER_PAGE));
        pendingPagination.setCurrentPageIndex(0);
        pendingPagination.setPageFactory(this::createPendingPage);
        
        updatePendingPageInfo();
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
        int toIndex = Math.min((currentPage + 1) * ROWS_PER_PAGE, filteredCourses.size() + fromIndex - 1);
        
        if (filteredCourses.isEmpty()) {
            pageInfo.setText("共 0 条记录");
        } else {
            pageInfo.setText(String.format("第 %d 页，显示 %d-%d 条", 
                    currentPage + 1, fromIndex, toIndex));
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
        // 目前仅在本地进行筛选，后续可修改为API筛选
        final String department = departmentFilter.getValue().equals("全部院系") ? "" : departmentFilter.getValue();
        final String type = courseTypeFilter.getValue().equals("全部类型") ? "" : courseTypeFilter.getValue();
        String creditStr = creditFilter.getValue().equals("全部学分") ? "" : creditFilter.getValue();
        String status = statusFilter.getValue().equals("全部状态") ? "" : statusFilter.getValue();
        
        final Integer credit;
        if (!creditStr.isEmpty()) {
            credit = Integer.parseInt(creditStr.replace("学分", ""));
        } else {
            credit = null;
        }
        
        final Boolean isActive;
        if (!status.isEmpty()) {
            isActive = status.equals("开设中");
        } else {
            isActive = null;
        }
        
        // 先从API获取所有数据
        fetchCourseList(1, ROWS_PER_PAGE);
        
        // 然后在本地进行筛选
        List<Course> filteredList = allCourses.stream()
                .filter(course -> (department.isEmpty() || course.getDepartment().equals(department))
                        && (type.isEmpty() || course.getType().equals(type))
                        && (credit == null || course.getCredit() == credit)
                        && (isActive == null || course.getIsActive() == isActive))
                .collect(Collectors.toList());
        
        filteredCourses.clear();
        filteredCourses.addAll(filteredList);
        
        // 更新表格
        courseTable.setItems(filteredCourses);
        
        // 更新分页
        coursePagination.setPageCount(1);
        coursePagination.setCurrentPageIndex(0);
        
        updatePageInfo();
    }
    
    // FXML事件处理方法
    
    @FXML
    private void searchCourses() {
        String searchTerm = searchField.getText().toLowerCase().trim();
        
        // 重置筛选器
        departmentFilter.getSelectionModel().selectFirst();
        courseTypeFilter.getSelectionModel().selectFirst();
        creditFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();
        
        // 使用 pageNum = 1 重新从API获取数据
        fetchCourseList(1, ROWS_PER_PAGE);
        
        // 如果搜索词不为空，则在本地筛选结果
        if (!searchTerm.isEmpty()) {
            List<Course> searchResults = filteredCourses.stream()
                    .filter(course -> 
                        course.getCode().toLowerCase().contains(searchTerm) ||
                        course.getName().toLowerCase().contains(searchTerm) ||
                        course.getTeacher().toLowerCase().contains(searchTerm))
                    .toList();

            filteredCourses.clear();
            filteredCourses.addAll(searchResults);

            // 更新表格和分页
            courseTable.setItems(filteredCourses);
            coursePagination.setPageCount(1);
            coursePagination.setCurrentPageIndex(0);

            updatePageInfo();
        }
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        departmentFilter.getSelectionModel().selectFirst();
        courseTypeFilter.getSelectionModel().selectFirst();
        creditFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();

        // 重置后重新获取第一页数据
        fetchCourseList(1, ROWS_PER_PAGE);
    }

    @FXML
    private void searchPendingCourses() {
        String searchTerm = pendingSearchField.getText().toLowerCase().trim();

        if (searchTerm.isEmpty()) {
            pendingCourseTable.setItems(pendingCourses);
            return;
        }

        ObservableList<CourseApplication> searchResults = pendingCourses.filtered(app ->
                app.getName().toLowerCase().contains(searchTerm) ||
                String.valueOf(app.getTeacherId()).contains(searchTerm) ||
                String.valueOf(app.getId()).contains(searchTerm));

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
        CourseManagementContent courseManagementContent = new CourseManagementContent();
        courseManagementContent.ApplyForNewCourse( new ActionEvent());
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
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/CourseDetails.fxml"));
            Parent root = loader.load();
            //获取控制器
            CourseDetailsController controller = loader.getController();
            //创建新窗口
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.NONE);//非模态
            popupStage.initStyle(StageStyle.UNIFIED);
            popupStage.setTitle("查看课程详情");
            popupStage.setScene(new Scene(root,800,600));
            // 设置最小窗口大小
            popupStage.setMinWidth(700);
            popupStage.setMinHeight(550);
            controller.setStage(popupStage);

            // 设置课程ID并加载数据
            controller.loadCourseDetails(Integer.parseInt(course.getCode()));
            // 设置为非审批页面
            controller.setApplicable(false);

            //显示窗口
            popupStage.showAndWait();
        }catch (Exception e){
            e.printStackTrace();
        }
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
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/CourseDetails.fxml"));
            Parent root = loader.load();
            //获取控制器
            CourseDetailsController controller  = loader.getController();
            //创建新窗口
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.NONE);//非模态
            popupStage.initStyle(StageStyle.UNIFIED);
            popupStage.setTitle("查看课程详情");
            popupStage.setScene(new Scene(root,800,600));
            // 设置最小窗口大小
            popupStage.setMinWidth(700);
            popupStage.setMinHeight(550);
            controller.setStage(popupStage);

            // 设置课程ID并加载数据
            controller.loadCourseDetails(application.getId());
            // 设置为审批页面
            controller.setApplicable(true);

            //显示窗口
            popupStage.showAndWait();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void approveCourse(int index) {
        CourseApplication application = pendingCourseTable.getItems().get(index);

        if (showConfirmDialog("确认操作", "确定要通过课程 " + application.getName() + " 的申请吗？")) {
            // 发送批准请求到后端
            String url = "/class/approve/"+application.getId()+"?status=1&classNum="+application.getClassNum();
            NetworkUtils.post(url, "", new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    JsonObject res = gson.fromJson(result, JsonObject.class);

                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        // 添加到课程表
                        Course newCourse = new Course(
                                String.valueOf(application.getId()),
                                application.getName(),
                                application.getCollege(),
                                application.getPoint(),
                                application.getType(),
                                String.valueOf(application.getTeacherId()),
                                true
                        );

                        allCourses.add(newCourse);

                        // 从待审批列表中移除
                        pendingCourses.remove(application);

                        // 刷新数据
                        applyFilters();
                        updatePendingBadge();
                        updatePendingPageInfo();
                        pendingCourseTable.setItems(pendingCourses);
                        pendingCourseTable.refresh();

                        showInfoDialog("操作成功", "已批准课程申请: " + application.getName());
                    } else {
                        showErrorDialog("操作失败", "批准课程失败: " + res.get("msg").getAsString());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    showErrorDialog("操作失败",   e.getMessage());
                }
            });
        }
    }

    private void rejectCourse(int index) {
        CourseApplication application = pendingCourseTable.getItems().get(index);

        if (showConfirmDialog("确认操作", "确定要拒绝课程 " + application.getName() + " 的申请吗？")) {
            // 创建文本输入对话框
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("拒绝课程申请");
            dialog.setHeaderText("请输入拒绝原因");
            dialog.setContentText("拒绝原因:");
            // 获取用户输入的拒绝原因
            Optional<String> result = dialog.showAndWait();
            // 只有当用户提供了拒绝原因时才继续
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                String rejectReason = result.get().trim();
            // 发送拒绝请求到后端
            String url = "/class/approve/"+application.getId()+"?status=2&classNum="+application.getClassNum()+"&reason="+rejectReason;

            NetworkUtils.post(url,"", new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        // 从待审批列表中移除
                        pendingCourses.remove(application);
                        // 更新页面
                        updatePendingBadge();
                        updatePendingPageInfo();
                        pendingCourseTable.setItems(pendingCourses);
                        pendingCourseTable.refresh();

                        showInfoDialog("操作成功", "已拒绝课程申请: " + application.getName());
                    } else {
                        showErrorDialog("操作失败", "拒绝课程失败: " + res.get("msg").getAsString());
                    }
                }
                
                @Override
                public void onFailure(Exception e) {
                    showErrorDialog("操作失败", "网络错误: " + e.getMessage());
                }
            });
            }else {
                // 如果用户没有提供拒绝原因，显示提示
                ShowMessage.showErrorMessage("失败！","必须提供拒绝原因");
            }
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
