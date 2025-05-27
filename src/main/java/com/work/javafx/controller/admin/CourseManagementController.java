package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.work.javafx.controller.teacher.CourseManagementContent;
import com.work.javafx.controller.teacher.editCourseController;
import com.work.javafx.entity.Data;
import com.work.javafx.model.ClassSimpleInfo;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

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
    @FXML private Label pendingBadge;

    // 搜索和筛选组件
    @FXML private TextField searchField;
    @FXML private ComboBox<String> termFilter;

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

    // --- 新增/修改的状态变量 ---
    private ObservableList<Course> completeSearchResults = FXCollections.observableArrayList();
    private boolean isSearchModeActive = false;
    private int totalItemsForCurrentView = 0;

    private final int ROWS_PER_PAGE = 10;
    static Gson gson = new Gson();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        courseTable.setItems(filteredCourses);
        initFilters();
        loadData();
        initCourseTable();
        initPendingCourseTable();
        initPagination();
        updatePendingBadge();
    }

    private void initFilters() {
        termFilter.setItems(Data.getInstance().getSemesterList());
        termFilter.getSelectionModel().selectFirst();
        termFilter.setOnAction(this::handleTermChange);
    }

    private void initCourseTable() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        creditColumn.setCellValueFactory(new PropertyValueFactory<>("credit"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        teacherColumn.setCellValueFactory(new PropertyValueFactory<>("teacher"));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIsActive() ? "开设中" : "已拒绝"));
        statusColumn.setCellFactory(column -> new TableCell<Course, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null); setGraphic(null); setStyle("");
                } else {
                    Label statusLabel = new Label(item);
                    statusLabel.getStyleClass().add("status-badge");
                    if ("开设中".equals(item)) statusLabel.getStyleClass().add("status-active");
                    else statusLabel.getStyleClass().add("status-inactive");
                    setGraphic(statusLabel); setText(null); setAlignment(Pos.CENTER);
                }
            }
        });
        actionColumn.setCellFactory(createActionCellFactory());
    }

    private Callback<TableColumn<Course, Void>, TableCell<Course, Void>> createActionCellFactory() {
        return param -> new TableCell<>() {
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
                viewBtn.setOnAction(event -> viewCourse(getIndex()));
                editBtn.setOnAction(event -> editCourse(getIndex()));
                deleteBtn.setOnAction(event -> stopCourse(getIndex()));
                actionBox.setAlignment(Pos.CENTER);
            }
            @Override protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : actionBox); }
        };
    }

    private void initPendingCourseTable() {
        pendingCodeColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        pendingNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        pendingDepartmentColumn.setCellValueFactory(new PropertyValueFactory<>("college"));
        pendingApplicantColumn.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
        pendingCreditColumn.setCellValueFactory(new PropertyValueFactory<>("point"));
        pendingTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        pendingActionColumn.setCellFactory(createPendingActionCellFactory());
    }

    private Callback<TableColumn<CourseApplication, Void>, TableCell<CourseApplication, Void>> createPendingActionCellFactory() {
        return param -> new TableCell<>() {
            private final Button viewBtn = new Button();
            private final Button approveBtn = new Button();
            private final Button rejectBtn = new Button();
            private final HBox actionBox = new HBox(5, viewBtn, approveBtn, rejectBtn);
            {
                viewBtn.getStyleClass().addAll("table-button", "default-btn");
                approveBtn.getStyleClass().addAll("table-button", "success-btn");
                rejectBtn.getStyleClass().addAll("table-button", "warning-btn");
                Region viewIcon = new Region(); viewIcon.getStyleClass().add("view-icon"); viewBtn.setGraphic(viewIcon);
                Region approveIcon = new Region(); approveIcon.getStyleClass().add("approve-icon"); approveBtn.setGraphic(approveIcon);
                Region rejectIcon = new Region(); rejectIcon.getStyleClass().add("reject-icon"); rejectBtn.setGraphic(rejectIcon);
                viewBtn.setOnAction(event -> viewPendingCourse(getIndex()));
                approveBtn.setOnAction(event -> approveCourse(getIndex()));
                rejectBtn.setOnAction(event -> rejectCourse(getIndex()));
                actionBox.setAlignment(Pos.CENTER);
            }
            @Override protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : actionBox); }
        };
    }

    private void loadData() {
        fetchCourseList(1, ROWS_PER_PAGE);
        NetworkUtils.get("/class/pending", new NetworkUtils.Callback<String>() {
            @Override public void onSuccess(String result) {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    JsonArray dataArray = res.getAsJsonArray("data");
                    Type courseListType = new TypeToken<List<CourseApplication>>() {}.getType();
                    List<CourseApplication> loadedApplications = gson.fromJson(dataArray, courseListType);
                    pendingCourses.clear();
                    pendingCourses.addAll(loadedApplications);
                    pendingCourseTable.setItems(pendingCourses);
                    updatePendingPageInfo();
                    updatePendingBadge();
                    int pendingPageCount = (int) Math.ceil((double) pendingCourses.size() / ROWS_PER_PAGE);
                    pendingPagination.setPageCount(Math.max(1, pendingPageCount));
                    pendingPagination.setCurrentPageIndex(0);
                }
            }
            @Override public void onFailure(Exception e) { System.err.println("加载待审批课程失败: " + e.getMessage()); }
        });
    }

    private void fetchCourseList(int pageNum, int pageSize) {
        isSearchModeActive = false;
        String term = termFilter.getValue();
        String url = "/class/list?term=" + term + "&pageNum=" + pageNum + "&pageSize=" + pageSize;

        NetworkUtils.get(url, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    JsonObject data = res.getAsJsonObject("data");
                    JsonArray courseListJson = data.getAsJsonArray("list");
                    totalItemsForCurrentView = data.get("total").getAsInt();
                    int apiTotalPages = data.get("pages").getAsInt();
                    allCourses.clear();
                    for (int i = 0; i < courseListJson.size(); i++) {
                        JsonObject item = courseListJson.get(i).getAsJsonObject();
                        Course course = new Course(String.valueOf(item.get("id").getAsInt()), item.get("name").getAsString(),"", item.get("point").getAsDouble(), item.get("type").getAsString(), item.get("teacherName").getAsString(), "已通过".equals(item.get("status").getAsString()));
                        course.setClassNum(item.get("classNum").getAsString()); course.setPeopleNum(item.get("peopleNum").getAsInt()); course.setTerm(item.get("term").getAsString()); course.setStatus(item.get("status").getAsString());
                        allCourses.add(course);
                    }
                    filteredCourses.setAll(allCourses);
                    courseTable.refresh();
                    coursePagination.setPageCount(Math.max(1, apiTotalPages));
                    updatePageInfo();
                } else {
                    ShowMessage.showErrorMessage("加载课程失败", res.has("msg") ? res.get("msg").getAsString() : "未知错误");
                    totalItemsForCurrentView = 0; filteredCourses.clear(); courseTable.refresh(); coursePagination.setPageCount(1); updatePageInfo();
                }
            }
            @Override
            public void onFailure(Exception e) {
                ShowMessage.showErrorMessage("加载课程异常", "网络请求失败: " + e.getMessage());
                totalItemsForCurrentView = 0; filteredCourses.clear(); courseTable.refresh(); coursePagination.setPageCount(1); updatePageInfo();
            }
        });
    }

    private void initPagination() {
        coursePagination.setCurrentPageIndex(0);
        coursePagination.setPageFactory(pageIndex -> {
            if (isSearchModeActive) {
                int from = pageIndex * ROWS_PER_PAGE;
                int to = Math.min(from + ROWS_PER_PAGE, completeSearchResults.size());
                List<Course> pageData = (from < to) ? completeSearchResults.subList(from, to) : new ArrayList<>();
                filteredCourses.setAll(pageData);
                courseTable.refresh();
            } else {
                fetchCourseList(pageIndex + 1, ROWS_PER_PAGE);
            }
            updatePageInfo();
            return new VBox();
        });

        int initialPendingPages = (int) Math.ceil((double) pendingCourses.size() / ROWS_PER_PAGE);
        pendingPagination.setPageCount(Math.max(1, initialPendingPages));
        pendingPagination.setCurrentPageIndex(0);
        pendingPagination.setPageFactory(this::createPendingPage);
    }

    private Node createPendingPage(int pageIndex) {
        int from = pageIndex * ROWS_PER_PAGE;
        int to = Math.min(from + ROWS_PER_PAGE, pendingCourses.size());
        pendingCourseTable.setItems((from < to) ? FXCollections.observableArrayList(pendingCourses.subList(from, to)) : FXCollections.emptyObservableList());
        return new VBox(pendingCourseTable);
    }

    private void updatePageInfo() {
        int current = coursePagination.getCurrentPageIndex();
        if (totalItemsForCurrentView == 0) pageInfo.setText("共 0 条记录");
        else pageInfo.setText(String.format("第 %d 页，显示 %d-%d 条 (共 %d 条)", current + 1, current * ROWS_PER_PAGE + 1, Math.min((current + 1) * ROWS_PER_PAGE, totalItemsForCurrentView), totalItemsForCurrentView));
    }

    private void updatePendingPageInfo() {
        int count = (int) Math.ceil((double) pendingCourses.size() / ROWS_PER_PAGE);
        pendingPagination.setPageCount(Math.max(1, count));
        if (pendingPagination.getCurrentPageIndex() >= pendingPagination.getPageCount() && pendingPagination.getPageCount() > 0) pendingPagination.setCurrentPageIndex(pendingPagination.getPageCount() - 1);
        else if (pendingPagination.getPageCount() == 0 || pendingPagination.getCurrentPageIndex() < 0) pendingPagination.setCurrentPageIndex(0);
        if (pendingCourses.isEmpty()) pendingCourseTable.setItems(FXCollections.emptyObservableList());
        else createPendingPage(pendingPagination.getCurrentPageIndex());
        pendingPageInfo.setText(String.format("共 %d 条待审批记录", pendingCourses.size()));
    }

    private void updatePendingBadge() { pendingBadge.setText(String.valueOf(pendingCourses.size())); pendingBadge.setVisible(pendingCourses.size() > 0); }

    @FXML
    private void searchCourses() {
        String term = searchField.getText().trim();
        if (term.isEmpty()) { resetFilters(); return; }
        isSearchModeActive = true;
        Map<String, String> params = new HashMap<>(); params.put("keyword", term);

        NetworkUtils.get("/course-selection/search", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    JsonArray listJson = res.getAsJsonArray("data");
                    completeSearchResults.clear();
                    for (int i = 0; i < listJson.size(); i++) {
                        JsonObject item = listJson.get(i).getAsJsonObject();
                        Course course = new Course(String.valueOf(item.get("id").getAsInt()), item.get("name").getAsString(),"", item.get("point").getAsDouble(), item.get("type").getAsString(), item.has("teacherName") ? item.get("teacherName").getAsString() : "", "已通过".equals(item.get("status").getAsString()));
                        course.setClassNum(item.get("classNum").getAsString()); course.setTerm(item.get("term").getAsString()); course.setStatus(item.get("status").getAsString());
                        completeSearchResults.add(course);
                    }
                    totalItemsForCurrentView = completeSearchResults.size();
                    int numPages = (int) Math.ceil((double) totalItemsForCurrentView / ROWS_PER_PAGE);
                    coursePagination.setPageCount(Math.max(1, numPages));
                    Callback<Integer, Node> factory = coursePagination.getPageFactory();

                } else {
                    ShowMessage.showErrorMessage("搜索失败", res.has("msg") ? res.get("msg").getAsString() : "API错误");
                    isSearchModeActive = false; totalItemsForCurrentView = 0; filteredCourses.clear(); courseTable.refresh(); coursePagination.setPageCount(1); updatePageInfo();
                }
            }
            @Override
            public void onFailure(Exception e) { ShowMessage.showErrorMessage("搜索异常", "网络请求失败: " + e.getMessage()); isSearchModeActive = false; }
        });
    }

    @FXML
    private void resetFilters() {
        searchField.clear(); isSearchModeActive = false; completeSearchResults.clear();
        String currentTerm = termFilter.getValue();
        termFilter.getSelectionModel().selectFirst();
        if (Objects.equals(currentTerm, termFilter.getValue())) {
            if (coursePagination.getCurrentPageIndex() == 0) fetchCourseList(1, ROWS_PER_PAGE);
            else coursePagination.setCurrentPageIndex(0);
        }
    }

    @FXML
    public void handleTermChange(ActionEvent actionEvent) {
        isSearchModeActive = false; completeSearchResults.clear();
        if (coursePagination.getCurrentPageIndex() == 0) fetchCourseList(1, ROWS_PER_PAGE);
        else coursePagination.setCurrentPageIndex(0);
    }

    @FXML private void showMainView() { mainCoursesView.setVisible(true); pendingCoursesView.setVisible(false); mainTitleContainer.setVisible(true); pendingTitleContainer.setVisible(false); }
    @FXML private void showPendingView() { mainCoursesView.setVisible(false); pendingCoursesView.setVisible(true); mainTitleContainer.setVisible(false); pendingTitleContainer.setVisible(true); }
    @FXML private void showAddCourseView() { new CourseManagementContent().ApplyForNewCourse(new ActionEvent());}
    @FXML private void exportCourses() { ShowMessage.showInfoMessage("提示", "导出功能未实现"); }
    @FXML private void batchStopCourses() { ShowMessage.showInfoMessage("提示", "批量停开未实现"); }

    private List<Course> getSelectedCourses() { return courseTable.getSelectionModel().getSelectedItems(); }

    private void viewCourse(int index) {
        if (index < 0 || index >= filteredCourses.size()) return; Course c = filteredCourses.get(index);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/CourseDetails.fxml")); Parent root = loader.load();
            CourseDetailsController ctrl = loader.getController(); Stage stage = new Stage();
            stage.initModality(Modality.NONE); stage.initStyle(StageStyle.UNIFIED); stage.setTitle("查看课程详情");
            stage.setScene(new Scene(root, 800, 600)); stage.setMinWidth(700); stage.setMinHeight(550);
            ctrl.setStage(stage); ctrl.loadCourseDetails(Integer.parseInt(c.getCode())); ctrl.setApplicable(false);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); ShowMessage.showErrorMessage("错误", "无法加载课程详情: " + e.getMessage()); }
    }

    private void editCourse(int index) {
        if (index < 0 || index >= filteredCourses.size()) return; Course c = filteredCourses.get(index);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/teacher/editCourse.fxml")); Parent root = loader.load();
            editCourseController ctrl = loader.getController(); Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); stage.initStyle(StageStyle.DECORATED); stage.setTitle("编辑课程");
            stage.setScene(new Scene(root, 800, 600)); stage.setMinWidth(700); stage.setMinHeight(550);
            ctrl.initCourseId(Integer.parseInt(c.getCode())); ctrl.setStage(stage);
            stage.showAndWait();
            if (isSearchModeActive) {
                Callback<Integer, Node> factory = coursePagination.getPageFactory();
                if (factory != null) factory.call(coursePagination.getCurrentPageIndex());
            } else {
                fetchCourseList(coursePagination.getCurrentPageIndex() + 1, ROWS_PER_PAGE);
            }
        } catch (IOException e) { e.printStackTrace(); ShowMessage.showErrorMessage("错误", "无法加载编辑窗口: " + e.getMessage());}
    }

    private void stopCourse(int index) {
        if (index < 0 || index >= filteredCourses.size()) return; Course c = filteredCourses.get(index);
        if (!c.getIsActive()) { ShowMessage.showInfoMessage("操作提示", "该课程已停开"); return; }
        if (showConfirmDialog("确认操作", "确定停开课程 " + c.getName() + " 吗？")) {
            String url = "/class/deleteAd/" + c.getCode();
            NetworkUtils.post(url, "", new NetworkUtils.Callback<String>() {
                @Override public void onSuccess(String result) {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.get("code").getAsInt() == 200) {
                        ShowMessage.showInfoMessage("操作成功", "已删除课程: " + c.getName());
                        if (isSearchModeActive) {
                            completeSearchResults.remove(c); // Also remove from the full search list
                            Callback<Integer, Node> factory = coursePagination.getPageFactory();
                            if (factory != null) factory.call(coursePagination.getCurrentPageIndex());
                        } else {
                            fetchCourseList(coursePagination.getCurrentPageIndex() + 1, ROWS_PER_PAGE);
                        }
                    } else { ShowMessage.showErrorMessage("操作失败", res.has("msg") ? res.get("msg").getAsString() : "未知错误"); }
                }
                @Override public void onFailure(Exception e) { ShowMessage.showErrorMessage("操作失败", "网络错误: " + e.getMessage()); }
            });
        }
    }

    private void viewPendingCourse(int index) {
        if (index < 0 || index >= pendingCourseTable.getItems().size()) return; CourseApplication app = pendingCourseTable.getItems().get(index);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/CourseDetails.fxml")); Parent root = loader.load();
            CourseDetailsController ctrl = loader.getController(); Stage stage = new Stage();
            stage.initModality(Modality.NONE); stage.initStyle(StageStyle.UNIFIED); stage.setTitle("查看课程详情");
            stage.setScene(new Scene(root, 800, 600)); stage.setMinWidth(700); stage.setMinHeight(550);
            ctrl.setStage(stage); ctrl.loadCourseDetails(app.getId()); ctrl.setApplicable(true);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); ShowMessage.showErrorMessage("错误", "无法加载待审批课程详情: " + e.getMessage());}
    }

    private void fetchAvailableClasses(java.util.function.Consumer<List<ClassSimpleInfo>> callback) {
        String url = "/section/getSectionListAll?page=1&size=500";
        NetworkUtils.get(url, new NetworkUtils.Callback<String>() {
            @Override public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200 && res.has("data")) {
                        JsonObject data = res.getAsJsonObject("data"); Type listType = new TypeToken<ArrayList<ClassSimpleInfo>>(){}.getType();
                        List<ClassSimpleInfo> classes = gson.fromJson(data.get("section"), listType);
                        callback.accept(classes != null ? classes : new ArrayList<>());
                    } else { ShowMessage.showErrorMessage("获取班级失败", res.has("msg") ? res.get("msg").getAsString() : "格式错误"); callback.accept(new ArrayList<>()); }
                } catch (Exception ex) { ShowMessage.showErrorMessage("获取班级失败", "解析数据失败: " + ex.getMessage()); callback.accept(new ArrayList<>()); }
            }
            @Override public void onFailure(Exception e) { ShowMessage.showErrorMessage("获取班级失败", "网络请求失败: " + e.getMessage()); callback.accept(new ArrayList<>()); }
        });
    }

    private void approveCourse(int index) {
        if (index < 0 || index >= pendingCourseTable.getItems().size()) return; CourseApplication app = pendingCourseTable.getItems().get(index);
        if (!showConfirmDialog("确认操作", "通过课程 " + app.getName() + " 申请?")) return;
        if ("必修".equals(app.getType())) {
            fetchAvailableClasses(classes -> {
                if (classes == null || classes.isEmpty()) { ShowMessage.showErrorMessage("操作失败", "无可用班级"); return; }
                Dialog<ClassSimpleInfo> dialog = new Dialog<>(); dialog.setTitle("选择班级");
                dialog.setHeaderText("为课程 '" + app.getName() + "' 选择班级绑定");
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                ComboBox<ClassSimpleInfo> combo = new ComboBox<>(FXCollections.observableArrayList(classes));
                combo.setPromptText("选择班级"); if (!classes.isEmpty()) combo.setValue(classes.get(0));
                VBox vbox = new VBox(10, new Label("选择绑定班级:"), combo); dialog.getDialogPane().setContent(vbox);
                Node okBtn = dialog.getDialogPane().lookupButton(ButtonType.OK); okBtn.setDisable(combo.getValue() == null);
                combo.valueProperty().addListener((obs,ov,nv) -> okBtn.setDisable(nv == null));
                dialog.setResultConverter(btn -> btn == ButtonType.OK ? combo.getValue() : null);
                dialog.showAndWait().ifPresent(selected -> sendApprovalRequest(app, "1", String.valueOf(selected.getId()), null, selected.getName()));
            });
        } else sendApprovalRequest(app, "1", null, null, null);
    }

    private void rejectCourse(int index) {
        if (index < 0 || index >= pendingCourseTable.getItems().size()) return; CourseApplication app = pendingCourseTable.getItems().get(index);
        if (!showConfirmDialog("确认操作", "拒绝课程 " + app.getName() + " 申请?")) return;
        TextInputDialog dialog = new TextInputDialog(); dialog.setTitle("拒绝课程申请");
        dialog.setHeaderText("输入拒绝原因"); dialog.setContentText("原因:");
        dialog.showAndWait().filter(reason -> !reason.trim().isEmpty()).ifPresentOrElse(
                reason -> sendApprovalRequest(app, "2", app.getClassNum(), reason.trim(), null),
                () -> ShowMessage.showErrorMessage("操作取消", "需提供原因")
        );
    }

    private void sendApprovalRequest(CourseApplication app, String status, String classInfo, String reason, String successClassName) {
        Map<String, String> params = new HashMap<>(); params.put("status", status);
        if ("1".equals(status) && classInfo != null) params.put("ccourseId", classInfo);
        if ("2".equals(status) && classInfo != null && reason != null) {
            params.put("classNum", classInfo);
            params.put("reason", reason);
        } else if ("2".equals(status) && reason != null){
            params.put("reason", reason);
        }

        String url = "/class/approve/" + app.getId();
        NetworkUtils.post(url, params, "", new NetworkUtils.Callback<String>() {
            @Override public void onSuccess(String result) {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    String msg = "1".equals(status) ? "已批准: " + app.getName() + (successClassName != null ? " 绑定到: " + successClassName : "") : "已拒绝: " + app.getName();
                    ShowMessage.showInfoMessage("操作成功", msg);
                    pendingCourses.remove(app);
                    updatePendingBadge();
                    updatePendingPageInfo(); // 正确更新待审批课程视图及其分页

                    if ("1".equals(status)) { // 课程已批准，刷新主课程列表到第1页
                        if (coursePagination.getCurrentPageIndex() == 0) {
                            fetchCourseList(1, ROWS_PER_PAGE);
                        } else {

                            coursePagination.setCurrentPageIndex(0);
                        }
                    }
                } else {
                    ShowMessage.showErrorMessage("操作失败", (status.equals("1")?"批准":"拒绝")+"失败: " + (res.has("msg")?res.get("msg").getAsString():"未知错误"));
                }
            }
            @Override public void onFailure(Exception e) { ShowMessage.showErrorMessage("操作失败", "网络错误: " + e.getMessage()); }
        });
    }
    private boolean showConfirmDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); alert.setTitle(title);
        alert.setHeaderText(null); alert.setContentText(content);
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }


}