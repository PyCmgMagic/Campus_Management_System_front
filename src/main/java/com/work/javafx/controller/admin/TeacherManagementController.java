package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.work.javafx.util.NetworkUtils;
import javafx.application.Platform;
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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TeacherManagementController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(TeacherManagementController.class.getName());
    private static final Gson gson = new Gson(); // Gson instance for JSON parsing

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
    @FXML private ComboBox<String> departmentFilter; // Represents 'college' for API

    // Batch Actions
    @FXML private Button batchResetPasswordBtn;
    @FXML private Button batchDeleteBtn;

    // Teacher Table
    @FXML private TableView<TeacherInfo> teacherTable;
    @FXML private TableColumn<TeacherInfo, String> idColumn; // Employee ID (mapped to sduid)
    @FXML private TableColumn<TeacherInfo, String> nameColumn; // Mapped to username
    @FXML private TableColumn<TeacherInfo, String> departmentColumn; // Displays 'college'
    @FXML private TableColumn<TeacherInfo, String> contactColumn; // Displays 'email' as contactInfo
    @FXML private TableColumn<TeacherInfo, String> statusColumn; // Displays 'email' as contactInfo
    @FXML private TableColumn<TeacherInfo, Void> actionColumn;

    // Pagination
    @FXML private Pagination teacherPagination;
    @FXML private Label pageInfo;

    // Pagination Parameters
    private final int ROWS_PER_PAGE = 10;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initFilters();
        initTeacherTable(); // Initialize table columns and cell factories
        initPagination();   // Initialize pagination and set page factory

        // Initial data load for page 0
        fetchTeacherData(0);
    }

    private void initFilters() {
        departmentFilter.setItems(FXCollections.observableArrayList(
                "全部院系", "计算机学院", "数学学院", "物理学院", "外语学院", "经济管理学院" // Keep UI labels
        ));
        departmentFilter.getSelectionModel().selectFirst();

        // Add listeners to trigger data reload on filter change
        departmentFilter.setOnAction(e -> triggerDataReload());
    }

    private void initTeacherTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id")); // Corresponds to TeacherInfo.id (mapped from sduid)
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name")); // Corresponds to TeacherInfo.name (mapped from username)
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("college")); // Corresponds to TeacherInfo.college
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactInfo")); // Corresponds to TeacherInfo.contactInfo (mapped from email)
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        actionColumn.setCellFactory(createActionCellFactory());
    }

    private Callback<TableColumn<TeacherInfo, Void>, TableCell<TeacherInfo, Void>> createActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<TeacherInfo, Void> call(TableColumn<TeacherInfo, Void> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button();
                    private final Button editBtn = new Button();
                    private final Button resetPassBtn = new Button();
                    private final Button deleteBtn = new Button();
                    private final HBox actionBox = new HBox(5);

                    {
                        Region viewIcon = new Region(); viewIcon.getStyleClass().add("view-icon"); viewBtn.setGraphic(viewIcon);
                        viewBtn.setTooltip(new Tooltip("查看详情"));
                        viewBtn.getStyleClass().addAll("table-button", "default-btn");

                        Region editIcon = new Region(); editIcon.getStyleClass().add("edit-icon"); editBtn.setGraphic(editIcon);
                        editBtn.setTooltip(new Tooltip("编辑信息"));
                        editBtn.getStyleClass().addAll("table-button", "warning-btn");

                        Region resetIcon = new Region(); resetIcon.getStyleClass().add("approval-icon"); resetPassBtn.setGraphic(resetIcon);
                        resetPassBtn.setTooltip(new Tooltip("重置密码"));
                        resetPassBtn.getStyleClass().addAll("table-button", "success-btn");

                        Region deleteIcon = new Region(); deleteIcon.getStyleClass().add("delete-icon"); deleteBtn.setGraphic(deleteIcon);
                        deleteBtn.setTooltip(new Tooltip("删除教师"));
                        deleteBtn.getStyleClass().addAll("table-button", "danger-btn");

                        actionBox.getChildren().addAll(viewBtn, editBtn, resetPassBtn, deleteBtn);
                        actionBox.setAlignment(Pos.CENTER);

                        viewBtn.setOnAction(event -> viewTeacher(getTableRow().getIndex()));
                        editBtn.setOnAction(event -> editTeacher(getTableRow().getIndex()));
                        resetPassBtn.setOnAction(event -> resetPassword(getTableRow().getIndex()));
                        deleteBtn.setOnAction(event -> deleteTeacher(getTableRow().getIndex()));
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
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
        teacherPagination.setPageCount(1); // Start with 1 page
        teacherPagination.setCurrentPageIndex(0);
        teacherPagination.setPageFactory(this::fetchTeacherData);
    }

    private Node fetchTeacherData(int pageIndex) {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(pageIndex + 1)); // API expects 1-based page index
        params.put("limit", String.valueOf(ROWS_PER_PAGE));

        // Add filter parameters
        String selectedDepartment = departmentFilter.getValue();
        // Send department only if it's not "全部院系"
        if (selectedDepartment != null && !"全部院系".equals(selectedDepartment)) {
            params.put("college", selectedDepartment);
        }

        // Add search term if present
        String searchTerm = searchField.getText();
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
             // Assuming API supports a general search parameter, adjust if needed
            params.put("search", searchTerm.trim()); // Sending search term
        }

         teacherTable.setPlaceholder(new Label("正在加载数据..."));

        NetworkUtils.getAsync("/admin/getTeacherList", params)
            .thenAcceptAsync(response -> Platform.runLater(() -> { // Ensure UI updates happen on the FX thread
                try {
                    JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

                    if (jsonResponse.has("code") && jsonResponse.get("code").getAsInt() == 200 && jsonResponse.has("data") && jsonResponse.get("data").isJsonArray()) {
                        JsonArray dataArray = jsonResponse.getAsJsonArray("data");

                        ObservableList<TeacherInfo> currentPageData = FXCollections.observableArrayList();
                        for (JsonElement element : dataArray) {
                            JsonObject teacherJson = element.getAsJsonObject();
                            TeacherInfo teacher = new TeacherInfo(
                                getStringOrNull(teacherJson, "sduid"),    // Map sduid to id
                                getStringOrNull(teacherJson, "username"), // Map username to name
                                getStringOrNull(teacherJson, "college"),
                                getStringOrNull(teacherJson, "email"),     // Map email to contactInfo
                                    "在职"
                            );
                            currentPageData.add(teacher);
                        }

                        // Update table items
                        teacherTable.setItems(currentPageData);
                        if (currentPageData.isEmpty() && pageIndex == 0) {
                             teacherTable.setPlaceholder(new Label("没有找到符合条件的教师数据"));
                        } else if (currentPageData.isEmpty() && pageIndex > 0) {
                             teacherTable.setPlaceholder(new Label("没有更多数据了"));
                        }

                        // Update pagination controls (Approximate logic without total)
                        int fetchedSize = currentPageData.size();
                        if (fetchedSize < ROWS_PER_PAGE) {
                            // This is likely the last page
                            teacherPagination.setPageCount(pageIndex + 1);
                        } else {
                            // Assume there might be more pages
                            teacherPagination.setPageCount(pageIndex + 2);
                        }
                        // Ensure current page index doesn't exceed new page count
                        if (pageIndex >= teacherPagination.getPageCount()) {
                            teacherPagination.setCurrentPageIndex(teacherPagination.getPageCount() - 1);
                        }

                        // Update page info label (Simplified)
                        updatePageInfoLabel(pageIndex, fetchedSize);

                    } else {
                        String errorMsg = "获取教师数据失败";
                        if(jsonResponse.has("msg")) {
                            errorMsg += ": " + jsonResponse.get("msg").getAsString();
                        }
                        LOGGER.log(Level.WARNING, "API Error: " + errorMsg);
                        showErrorDialog("加载失败", errorMsg);
                         handleFetchError();
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "解析教师数据失败", e);
                    showErrorDialog("处理错误", "无法处理服务器响应。 Exception: " + e.getMessage());
                     handleFetchError();
                } finally {
                    // Hide loading indicator (optional)
                }
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> { // Ensure UI updates happen on the FX thread
                    LOGGER.log(Level.SEVERE, "网络请求失败", ex);
                    showErrorDialog("网络错误", "无法连接到服务器: " + ex.getMessage());
                     handleFetchError();
                });
                return null; // Required for exceptionally
            });

        return teacherTable;
    }

    private String getStringOrNull(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private void handleFetchError() {
        teacherTable.setItems(FXCollections.observableArrayList()); // Clear table
        teacherTable.setPlaceholder(new Label("加载数据失败，请重试"));
        teacherPagination.setPageCount(1);
        teacherPagination.setCurrentPageIndex(0);
        updatePageInfoLabel(-1, 0); // Update label to show 0 results
    }

    // Updates the pagination info label (Simplified)
    private void updatePageInfoLabel(int currentPageIndex, int fetchedCount) {
         if (currentPageIndex < 0 || fetchedCount <= 0) {
            pageInfo.setText("第 1 页 / 共 1 页 - 显示 0 条"); // Or just "无记录"
            if(currentPageIndex == -1) pageInfo.setText("加载失败");
        } else {
            int pageNum = currentPageIndex + 1;
            int totalPagesApproximation = teacherPagination.getPageCount(); // Use current page count
            int fromRecord = currentPageIndex * ROWS_PER_PAGE + 1;
            int toRecord = fromRecord + fetchedCount - 1;
            pageInfo.setText(String.format("第 %d 页 / 共 %d 页 (预估) - 显示 %d-%d 条",
                    pageNum, totalPagesApproximation, fromRecord, toRecord));
        }
    }


    // Event Handlers
    @FXML
    private void searchTeachers() {
        triggerDataReload();
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        departmentFilter.getSelectionModel().selectFirst();
        triggerDataReload();
    }

    private void triggerDataReload() {
         if (teacherPagination.getCurrentPageIndex() == 0) {
            fetchTeacherData(0);
        } else {
            teacherPagination.setCurrentPageIndex(0);
         }
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
         FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择教师信息文件 (Excel)");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel 文件", "*.xlsx", "*.xls")
        );
        File selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());

        if (selectedFile != null) {
            showInfoDialog("提示", "正在上传文件: " + selectedFile.getName() + "...");
            // Ensure this endpoint is correct for teacher import
            NetworkUtils.postMultipartFileAsync("/admin/teacher/upload", selectedFile)
                .thenAcceptAsync(response -> Platform.runLater(() -> {
                    try {
                         JsonObject res = gson.fromJson(response, JsonObject.class);
                         if (res.has("code") && res.get("code").getAsInt() == 200) {
                            showInfoDialog("成功", "文件上传成功！教师数据已导入。");
                            triggerDataReload(); // Refresh the table
                         } else {
                             String errorMsg = res.has("msg") ? res.get("msg").getAsString() : "未知错误";
                             showErrorDialog("导入失败", "文件上传或处理失败: " + errorMsg);
                         }
                    } catch (Exception e) {
                         LOGGER.log(Level.SEVERE, "处理导入响应失败", e);
                         showErrorDialog("处理错误", "无法处理导入响应。 Exception: " + e.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                     Platform.runLater(() -> {
                         LOGGER.log(Level.SEVERE, "文件上传失败", ex);
                         showErrorDialog("上传错误", "文件上传失败: " + ex.getMessage());
                     });
                     return null;
                 });
        } else {
             showInfoDialog("提示", "未选择任何文件。");
        }
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
            // TODO: Implement actual export logic (e.g., call backend API for export)
            System.out.println("Exporting to: " + file.getAbsolutePath());
            showInfoDialog("功能待实现", "导出功能需要后端API支持。导出目标:" + file.getAbsolutePath());
        }
    }

    // Batch Actions - Implement API calls for selected IDs
    @FXML
    private void batchResetPassword() {
         ObservableList<TeacherInfo> selectedTeachers = teacherTable.getSelectionModel().getSelectedItems();
         if (selectedTeachers.isEmpty()){
             showInfoDialog("提示", "请先在当前页选择要重置密码的教师。");
             return;
         }
        // TODO: Call API for each selectedTeacher.getId() (which is sduid)
        showInfoDialog("功能开发中", "批量重置密码功能待实现。选中: " + selectedTeachers.size() + "个");
    }

    @FXML
    private void batchDeleteTeachers() {
         ObservableList<TeacherInfo> selectedTeachers = teacherTable.getSelectionModel().getSelectedItems();
         if (selectedTeachers.isEmpty()){
             showInfoDialog("提示", "请先在当前页选择要删除的教师。");
             return;
         }
         if (showConfirmDialog("确认批量删除", "确定要删除当前页选中的 " + selectedTeachers.size() + " 位教师吗？")) {
             // TODO: Call API for each selectedTeacher.getId() (which is sduid)
             showInfoDialog("功能开发中", "批量删除功能待实现。选中: " + selectedTeachers.size() + "个");
             // After successful API calls, refresh: triggerDataReload();
         }
    }

    // Table Actions - Use correct TeacherInfo fields
    private void viewTeacher(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= teacherTable.getItems().size()) return;
        TeacherInfo selectedTeacher = teacherTable.getItems().get(rowIndex);
         Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("教师详情");
        // Use mapped fields: id (sduid), name (username), college, contactInfo (email)
        alert.setHeaderText("教师: " + selectedTeacher.getName() + " (工号: " + selectedTeacher.getId() + ")");
        String content = String.format(
            "工号: %s\n姓名: %s\n院系: %s\n联系方式 (Email): %s",
            selectedTeacher.getId(), selectedTeacher.getName(), selectedTeacher.getCollege(),
            selectedTeacher.getContactInfo()
        );
        alert.setContentText(content);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private void editTeacher(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= teacherTable.getItems().size()) return;
        TeacherInfo selectedTeacher = teacherTable.getItems().get(rowIndex);
        // TODO: Implement edit view/dialog using selectedTeacher's data
        showInfoDialog("编辑教师", "编辑教师信息: " + selectedTeacher.getName() + "\n(编辑功能待实现)");
    }

    private void resetPassword(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= teacherTable.getItems().size()) return;
        TeacherInfo selectedTeacher = teacherTable.getItems().get(rowIndex);
        String teacherId = selectedTeacher.getId(); // This is sduid
        if (showConfirmDialog("确认操作", "确定要重置教师 " + selectedTeacher.getName() + " (工号: "+ teacherId +") 的密码吗？")) {
            // TODO: Call API POST /admin/teacher/resetPassword/{sduid} ? Need confirmation on ID used in API path
            showInfoDialog("操作成功", "已重置教师 " + selectedTeacher.getName() + " 的密码。(模拟)");
            // Example API call structure (adjust endpoint/method/ID as needed):
            /*
            NetworkUtils.postAsync("/admin/teacher/resetPassword/" + teacherId, null) // Pass sduid
                .thenAcceptAsync(response -> Platform.runLater(() -> { ... check response ... }))
                .exceptionally(ex -> { ... handle error ... });
            */
        }
    }

    private void deleteTeacher(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= teacherTable.getItems().size()) return;
        TeacherInfo selectedTeacher = teacherTable.getItems().get(rowIndex);
        String teacherId = selectedTeacher.getId(); // This is sduid
        String teacherName = selectedTeacher.getName(); // Get name before potential removal

        if (showConfirmDialog("确认删除", "确定要删除教师 " + teacherName + " (工号: "+ teacherId +") 吗？")) {
            // Call API DELETE /admin/teacher/{sduid} ? Need confirmation on ID used in API path
             NetworkUtils.deleteAsync("/admin/teacher/" + teacherId) // Assuming API uses sduid in path
                .thenAcceptAsync(response -> Platform.runLater(() -> {
                    try {
                         JsonObject res = gson.fromJson(response, JsonObject.class);
                         if (res.has("code") && res.get("code").getAsInt() == 200) {
                            showInfoDialog("操作成功", "已删除教师: " + teacherName);
                            fetchTeacherData(teacherPagination.getCurrentPageIndex());
                         } else {
                             String errorMsg = res.has("msg") ? res.get("msg").getAsString() : "未知错误";
                             showErrorDialog("删除失败", "删除教师失败: " + errorMsg);
                         }
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "处理删除响应失败", e);
                        showErrorDialog("处理错误", "无法处理删除响应。 Exception: " + e.getMessage());
                    }

                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "删除请求失败", ex);
                        showErrorDialog("删除失败", "删除教师请求失败: " + ex.getMessage());
                    });
                    return null;
                });
        }
    }

    // Helper Dialog Methods
    private void showInfoDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
         alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

     private void showErrorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
         alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }


    private boolean showConfirmDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
         alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    /**
     * Inner class for Teacher data model.
     * Updated based on API response and UI requirements.
     */
    public static class TeacherInfo {
        private final SimpleStringProperty id;          // Mapped from sduid
        private final SimpleStringProperty name;        // Mapped from username
        private final SimpleStringProperty college;
        private final SimpleStringProperty contactInfo; // Mapped from email
        private final SimpleStringProperty status;

         public TeacherInfo(String id, String name, String college, String contactInfo, String status) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.college = new SimpleStringProperty(college);
            this.contactInfo = new SimpleStringProperty(contactInfo);
             this.status = new SimpleStringProperty(status);
         }

        public String getStatus() {
            return status.get();
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }

        // Getters required by PropertyValueFactory
        public String getId() { return id.get(); } // Returns sduid
        public SimpleStringProperty idProperty() { return id; }
        public String getName() { return name.get(); } // Returns username
        public SimpleStringProperty nameProperty() { return name; }
        public String getCollege() { return college.get(); }
        public SimpleStringProperty collegeProperty() { return college; }
        public String getContactInfo() { return contactInfo.get(); } // Returns email
        public SimpleStringProperty contactInfoProperty() { return contactInfo; }
    }
}
