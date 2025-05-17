package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ShowMessage;
import javafx.application.Platform;
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
import com.work.javafx.model.TeacherInfo;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TeacherManagementController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(TeacherManagementController.class.getName());
    private static final Gson gson = new Gson(); // Gson 实例用于 JSON 解析

    // FXML 绑定
    @FXML private ScrollPane rootPane;
    @FXML private VBox mainTeachersView;
    @FXML private Label mainPageTitle;
    @FXML private HBox mainTitleContainer;

    // 快速操作
    @FXML private BorderPane addTeacherCard;
    @FXML private BorderPane importTeacherCard;
    @FXML private BorderPane exportTeacherCard;

    // Search and Filter
    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter; // API 中对应 'college'

    // Batch Actions
    @FXML private Button batchResetPasswordBtn;
    @FXML private Button batchDeleteBtn;

    // Teacher Table
    @FXML private TableView<TeacherInfo> teacherTable;
    @FXML private TableColumn<TeacherInfo, String> idColumn; // 职工号 (映射自 sduid)
    @FXML private TableColumn<TeacherInfo, String> nameColumn; // 映射自 username
    @FXML private TableColumn<TeacherInfo, String> departmentColumn; // 显示 'college'
    @FXML private TableColumn<TeacherInfo, String> contactColumn; // 显示 'email' 作为联系方式
    @FXML private TableColumn<TeacherInfo, String> statusColumn; // 状态列
    @FXML private TableColumn<TeacherInfo, Void> actionColumn;

    // Pagination
    @FXML private Pagination teacherPagination;
    @FXML private Label pageInfo;

    // Pagination Parameters
    private final int ROWS_PER_PAGE = 10;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initFilters();
        initTeacherTable(); // 初始化表格列和单元格工厂
        initPagination();   // 初始化分页并设置页面工厂

        // 初始加载第 0 页数据
        fetchTeacherData(0);
    }

    private void initFilters() {
        departmentFilter.setItems(FXCollections.observableArrayList(
                "全部院系", "计算机学院", "数学学院", "物理学院", "外语学院", "经济管理学院"
        ));
        departmentFilter.getSelectionModel().selectFirst();

        // 添加监听器，在筛选条件改变时触发数据重新加载
        departmentFilter.setOnAction(e -> triggerDataReload());
    }

    private void initTeacherTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("sduid"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name")); // 对应 TeacherInfo.name (映射自 username)
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("college")); // 对应 TeacherInfo.college
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactInfo")); // 对应 TeacherInfo.contactInfo (映射自 email)
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        actionColumn.setCellFactory(createActionCellFactory());
    }

    private Callback<TableColumn<TeacherInfo, Void>, TableCell<TeacherInfo, Void>> createActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<TeacherInfo, Void> call(TableColumn<TeacherInfo, Void> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button();
                    private final Button deleteBtn = new Button();
                    private final HBox actionBox = new HBox(5);

                    {
                        viewBtn.getStyleClass().addAll("table-button", "default-btn");
                        deleteBtn.getStyleClass().addAll("table-button", "danger-btn");

                        // 添加图标
                        Region viewIcon = new Region();
                        viewIcon.getStyleClass().add("view-icon");
                        viewBtn.setGraphic(viewIcon);

                        Region editIcon = new Region();
                        editIcon.getStyleClass().add("edit-icon");

                        Region deleteIcon = new Region();
                        deleteIcon.getStyleClass().add("delete-icon");
                        deleteBtn.setGraphic(deleteIcon);

                        actionBox.getChildren().addAll(viewBtn, deleteBtn);
                        actionBox.setAlignment(Pos.CENTER);

                        viewBtn.setOnAction(event -> viewTeacher(getTableRow().getIndex()));
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
        teacherPagination.setPageCount(1); // 初始为 1 页
        teacherPagination.setCurrentPageIndex(0);
        teacherPagination.setPageFactory(this::fetchTeacherData);
    }

    private Node fetchTeacherData(int pageIndex) {
        teacherTable.setPlaceholder(new Label("正在加载数据..."));
        if(searchField.getText().isEmpty()){
            Map<String, String> params = new HashMap<>();
            params.put("page", String.valueOf(pageIndex + 1)); // API 需要从 1 开始的页码
            params.put("limit", String.valueOf(ROWS_PER_PAGE));
            String selectedDepartment = departmentFilter.getValue();
            if (selectedDepartment != null && !"全部院系".equals(selectedDepartment)) {
                params.put("college", selectedDepartment);
            }

            NetworkUtils.getAsync("/admin/getTeacherList", params)
                .thenAcceptAsync(response -> Platform.runLater(() -> { // 确保 UI 更新在 FX 线程执行
                    try {
                        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

                        if (jsonResponse.has("code") && jsonResponse.get("code").getAsInt() == 200 && jsonResponse.has("data") && jsonResponse.get("data").isJsonObject()) {
                            JsonObject dataObject = jsonResponse.getAsJsonObject("data"); // 获取 data 对象

                            // 检查 'data' 中是否存在 'user' 数组
                            if (dataObject.has("user") && dataObject.get("user").isJsonArray()) {
                                JsonArray dataArray = dataObject.getAsJsonArray("user"); // 获取 user 数组

                                ObservableList<TeacherInfo> currentPageData = FXCollections.observableArrayList();
                                for (JsonElement element : dataArray) {
                                    JsonObject teacherJson = element.getAsJsonObject();
                                    TeacherInfo teacher = new TeacherInfo(
                                            getStringOrNull(teacherJson, "id"),

                                            getStringOrNull(teacherJson, "sduid"),
                                            getStringOrNull(teacherJson, "username"), // 映射 username 到 name
                                            getStringOrNull(teacherJson, "college"),
                                            getStringOrNull(teacherJson, "email"),     // 映射 email 到 contactInfo
                                            "在职" // 假设状态为在职，如果API返回状态字段，应进行映射
                                    );
                                    currentPageData.add(teacher);
                                }

                                // 更新表格数据
                                teacherTable.setItems(currentPageData);
                                if (currentPageData.isEmpty() && pageIndex == 0) {
                                    teacherTable.setPlaceholder(new Label("没有找到符合条件的教师数据"));
                                } else if (currentPageData.isEmpty() && pageIndex > 0) {
                                    teacherTable.setPlaceholder(new Label("没有更多数据了"));
                                }

                                // 使用响应中的总页数更新分页控件
                                int totalPages = 1; // 如果未提供，默认为 1
                                if (dataObject.has("page") && dataObject.get("page").isJsonPrimitive()) {
                                    totalPages = dataObject.get("page").getAsInt();
                                    if (totalPages <= 0) totalPages = 1; // 确保至少有 1 页
                                } else {

                                }
                                teacherPagination.setPageCount(totalPages);

                                if (pageIndex >= totalPages) {
                                    int newPageIndex = Math.max(0, totalPages - 1);
                                    teacherPagination.setCurrentPageIndex(newPageIndex);
                                    // 如果索引改变，我们可能需要重新获取数据，但先更新标签
                                    updatePageInfoLabel(newPageIndex, currentPageData.size(), totalPages);
                                } else {
                                    teacherPagination.setCurrentPageIndex(pageIndex); // 如果有效，保持当前索引
                                    updatePageInfoLabel(pageIndex, currentPageData.size(), totalPages);
                                }

                            } else {
                                // 处理 'user' 数组缺失或无效的情况
                                showErrorDialog("加载失败", "服务器返回的数据格式不正确 (缺少教师列表)。");
                                handleFetchError();
                            }
                        } else {
                            String errorMsg = "获取教师数据失败";
                            if (jsonResponse.has("msg")) {
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
                        // 隐藏加载指示器 (可选)
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> { // 确保 UI 更新在 FX 线程执行
                        LOGGER.log(Level.SEVERE, "网络请求失败", ex);
                        showErrorDialog("网络错误", "无法连接到服务器: " + ex.getMessage());
                        handleFetchError();
                    });
                    return null; // exceptionally 需要返回 null
                });
    }else{
            Map<String,String> params = new HashMap<>();
            params.put("keyword",searchField.getText());
            params.put("permission","1");
            NetworkUtils.get("/admin/searchSdu", params, new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JsonObject jsonResponse = gson.fromJson(result, JsonObject.class);

                        if (jsonResponse.has("code") && jsonResponse.get("code").getAsInt() == 200 && jsonResponse.has("data") ) {
                                JsonObject data = jsonResponse.getAsJsonObject("data");
                                JsonArray dataArray = data.getAsJsonArray("list");
                                ObservableList<TeacherInfo> currentPageData = FXCollections.observableArrayList();
                                for (JsonElement element : dataArray) {
                                    JsonObject teacherJson = element.getAsJsonObject();
                                    TeacherInfo teacher = new TeacherInfo(
                                            getStringOrNull(teacherJson, "id"),
                                            getStringOrNull(teacherJson, "sduid"),
                                            getStringOrNull(teacherJson, "username"), // 映射 username 到 name
                                            getStringOrNull(teacherJson, "college"),
                                            getStringOrNull(teacherJson, "email"),     // 映射 email 到 contactInfo
                                            "在职" // 假设状态为在职，如果API返回状态字段，应进行映射
                                    );
                                    currentPageData.add(teacher);
                                }

                                // 更新表格数据
                                teacherTable.setItems(currentPageData);
                                if (currentPageData.isEmpty() && pageIndex == 0) {
                                    teacherTable.setPlaceholder(new Label("没有找到符合条件的教师数据"));
                                } else if (currentPageData.isEmpty() && pageIndex > 0) {
                                    teacherTable.setPlaceholder(new Label("没有更多数据了"));
                                }

                                // 使用响应中的总页数更新分页控件
                                int totalPages = 1; // 如果未提供，默认为 1
                                teacherPagination.setPageCount(totalPages);

                                if (pageIndex >= totalPages) {
                                    int newPageIndex = Math.max(0, totalPages - 1);
                                    teacherPagination.setCurrentPageIndex(newPageIndex);
                                    // 如果索引改变，我们可能需要重新获取数据，但先更新标签
                                    updatePageInfoLabel(newPageIndex, currentPageData.size(), totalPages);
                                } else {
                                    teacherPagination.setCurrentPageIndex(pageIndex); // 如果有效，保持当前索引
                                    updatePageInfoLabel(pageIndex, currentPageData.size(), totalPages);
                                }

                            } else {
                                handleFetchError();
                            }
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "解析教师数据失败", e);
                        showErrorDialog("处理错误", "无法处理服务器响应。 Exception: " + e.getMessage());
                        handleFetchError();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    LOGGER.log(Level.SEVERE, "解析教师数据失败", e);
                    showErrorDialog("处理错误", "无法处理服务器响应。 Exception: " + e.getMessage());
                    handleFetchError();
                }
            });
        }
        return teacherTable;
    }

    private String getStringOrNull(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private void handleFetchError() {
        teacherTable.setItems(FXCollections.observableArrayList()); // 清空表格
        teacherTable.setPlaceholder(new Label("加载数据失败，请重试"));
        teacherPagination.setPageCount(1);
        teacherPagination.setCurrentPageIndex(0);
        updatePageInfoLabel(-1, 0, 1); // 更新标签以显示加载失败状态
    }

    // 更新分页信息标签
    private void updatePageInfoLabel(int currentPageIndex, int fetchedCount, int totalPages) {
         if (currentPageIndex < 0 || fetchedCount <= 0 && currentPageIndex == 0 && totalPages <= 1) { // 更好地处理无数据的情况
             pageInfo.setText("第 1 页 / 共 1 页 - 显示 0 条");
             if(currentPageIndex == -1) pageInfo.setText("加载失败"); // 加载失败的特定情况
         } else {
            int pageNum = currentPageIndex + 1; // 显示页码从 1 开始
             int fromRecord = currentPageIndex * ROWS_PER_PAGE + 1;
             int toRecord = fromRecord + fetchedCount - 1;
             // 确保当 fetchedCount 为 0 且页码大于 1 时，toRecord 不小于 fromRecord
             if (toRecord < fromRecord) toRecord = fromRecord -1;
             pageInfo.setText(String.format("第 %d 页 / 共 %d 页 - 显示 %d-%d 条",
                     pageNum, totalPages, fromRecord, toRecord));
         }
    }


    // 事件处理
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
            teacherPagination.setCurrentPageIndex(0); // 否则跳回第一页（会自动触发 fetchTeacherData）
         }
    }


    // 快速操作
    @FXML
    private void showAddTeacherView(javafx.scene.input.MouseEvent event) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/addNewTeacher.fxml"));
            Parent root = loader.load();
            AddNewTeacherController controller = loader.getController();
            Stage stage = new Stage();
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("添加教师");
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
    private void importTeachers(javafx.scene.input.MouseEvent event) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择教师信息文件");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Excel 文件", "*.xlsx", "*.xls")
            );

            // 获取当前窗口Stage，用于显示文件选择器
            Stage stage = (Stage) importTeacherCard.getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                try {
                    // 显示一个加载提示或禁用UI元素
                    ShowMessage.showInfoMessage( "提示", "正在上传文件: " + selectedFile.getName() + "...");

                    NetworkUtils.postMultipartFileAsync("/admin/upload", selectedFile)
                            .thenAcceptAsync(response -> {
                                // 在 UI 线程上更新
                                Platform.runLater(() -> {
                                    JsonObject res = gson.fromJson(response,JsonObject.class);
                                    if(res.has("code") && res.get("code").getAsInt()==200){
                                        System.out.println( response); // 打印响应以供调试
                                        ShowMessage.showInfoMessage( "成功", "文件上传成功！");
                                        //刷新列表
                                        fetchTeacherData(teacherPagination.getCurrentPageIndex());
                                    }

                                });
                            }, Platform::runLater) // 确保 thenAcceptAsync 的回调也在UI线程执行
                            .exceptionally(ex -> {
                                // 在 UI 线程上更新
                                Platform.runLater(() -> {
                                    LOGGER.log(Level.SEVERE, "文件上传失败", ex);
                                    ShowMessage.showErrorMessage( "错误", "文件上传失败: " + ex.getMessage());
                                });
                                return null;
                            });

                } catch (Exception e) {
                    // 处理调用 NetworkUtils 可能出现的直接异常（虽然异步调用本身不太可能在这里抛出）
                    LOGGER.log(Level.SEVERE, "启动文件上传时出错", e);
                    ShowMessage.showErrorMessage ("错误", "启动文件上传时出错: " + e.getMessage());
                }
            } else {
                System.out.println("未选择文件。");
                // 如果用户取消选择，显示提示
                ShowMessage.showErrorMessage("提示", "未选择任何文件。");
            }
        }




    // 批量操作 - 为选定的 ID 实现 API 调用
    @FXML
    private void batchResetPassword() {
         ObservableList<TeacherInfo> selectedTeachers = teacherTable.getSelectionModel().getSelectedItems();
         if (selectedTeachers.isEmpty()){
             showInfoDialog("提示", "请先在当前页选择要重置密码的教师。");
             return;
         }
        // TODO: 为每个 selectedTeacher.getId() (即 sduid) 调用 API
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
             // TODO: 为每个 selectedTeacher.getId()  调用 API
             showInfoDialog("功能开发中", "批量删除功能待实现。选中: " + selectedTeachers.size() + "个");
             // 成功调用 API 后刷新: triggerDataReload();
         }
    }

    // 表格操作
    private void viewTeacher(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= teacherTable.getItems().size()) return;
        TeacherInfo selectedTeacher = teacherTable.getItems().get(rowIndex);
       try{
           FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/UserDetails_teacher.fxml"));
           Parent root = loader.load();
           //获取控制器
           UserDetailsController_teacher controller = loader.getController();
           //创建新窗口
           Stage stage = new Stage();
           stage.initStyle(StageStyle.DECORATED);
           stage.setTitle("用户详情——"+selectedTeacher.getName());
           stage.setScene(new Scene(root));
           //窗口引用传递给控制器
           controller.setStage(stage);
           // 传递用户ID并加载数据
           controller.loadUserData(selectedTeacher.getId());
           //显示窗口
           stage.showAndWait();
       }catch (Exception e){
           e.printStackTrace();
       }
    }


    private void resetPassword(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= teacherTable.getItems().size()) return;
        TeacherInfo selectedTeacher = teacherTable.getItems().get(rowIndex);
        String teacherId = selectedTeacher.getId();
        if (showConfirmDialog("确认操作", "确定要重置教师 " + selectedTeacher.getName() + " (工号: "+ selectedTeacher.getSduid() +") 的密码吗？")) {
            // TODO: 调用 API POST /admin/teacher/resetPassword/{sduid} ? 需要确认 API 路径中使用的 ID
            showInfoDialog("操作成功", "已重置教师 " + selectedTeacher.getName() + " 的密码。(模拟)");
            // API 调用结构示例 (根据需要调整端点/方法/ID):
            /*
            NetworkUtils.postAsync("/admin/teacher/resetPassword/" + teacherId, null) // 传递 sduid
                .thenAcceptAsync(response -> Platform.runLater(() -> { ... 检查响应 ... }))
                .exceptionally(ex -> { ... 处理错误 ... });
            */
        }
    }

    private void deleteTeacher(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= teacherTable.getItems().size()) return;
        TeacherInfo selectedTeacher = teacherTable.getItems().get(rowIndex);
        String teacherId = selectedTeacher.getId();
        String teacherName = selectedTeacher.getName();
        if (showConfirmDialog("确认删除", "确定要删除教师 " + teacherName + " (工号: "+ selectedTeacher.getSduid() +") 吗？")) {
            String endpoint = "/admin/deleteUser";
            String urlWithParams = endpoint + "?userid=" + teacherId;

             NetworkUtils.postAsync(urlWithParams, null) // 使用 POST 方法，body 为 null
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
//
//    /**
//     * 教师数据模型的内部类。
//     */
//    public static class TeacherInfo {
//        private final SimpleStringProperty id;
//        private final SimpleStringProperty sduid;
//        private final SimpleStringProperty name;        // 映射自 username
//        private final SimpleStringProperty college;
//        private final SimpleStringProperty contactInfo; // 映射自 email
//        private final SimpleStringProperty status;
//
//        public String getSduid() {
//            return sduid.get();
//        }
//
//        public SimpleStringProperty sduidProperty() {
//            return sduid;
//        }
//
//        public TeacherInfo(String id, String sduid, String name, String college, String contactInfo, String status) {
//            this.id = new SimpleStringProperty(id);
//             this.sduid = new SimpleStringProperty(sduid);
//             this.name = new SimpleStringProperty(name);
//            this.college = new SimpleStringProperty(college);
//            this.contactInfo = new SimpleStringProperty(contactInfo);
//             this.status = new SimpleStringProperty(status);
//         }
//
//        public String getStatus() {
//            return status.get();
//        }
//
//        public SimpleStringProperty statusProperty() {
//            return status;
//        }
//
//        // PropertyValueFactory 需要的 Getters
//        public String getId() { return id.get(); } // 返回 sduid
//        public SimpleStringProperty idProperty() { return id; }
//        public String getName() { return name.get(); } // 返回 username
//        public SimpleStringProperty nameProperty() { return name; }
//        public String getCollege() { return college.get(); }
//        public SimpleStringProperty collegeProperty() { return college; }
//        public String getContactInfo() { return contactInfo.get(); } // Returns email
//        public SimpleStringProperty contactInfoProperty() { return contactInfo; }
//    }
}
