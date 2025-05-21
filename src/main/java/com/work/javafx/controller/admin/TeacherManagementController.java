package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ShowMessage;
import javafx.application.Platform;
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
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TeacherManagementController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(TeacherManagementController.class.getName());
    private static final Gson gson = new Gson();

    // FXML 绑定
    @FXML private ScrollPane rootPane;
    @FXML private VBox mainTeachersView;
    @FXML private Label mainPageTitle;
    @FXML private HBox mainTitleContainer;

    // 快速操作
    @FXML private BorderPane addTeacherCard;
    @FXML private BorderPane importTeacherCard;

    // Search and Filter
    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter;


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
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("college"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactInfo"));
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
                    private final Button resetBtn = new Button();
                    private final HBox actionBox = new HBox(5);

                    {
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
                        editIcon.getStyleClass().add("edit-icon");
                        resetBtn.setGraphic(editIcon);
                        Region deleteIcon = new Region();
                        deleteIcon.getStyleClass().add("delete-icon");
                        deleteBtn.setGraphic(deleteIcon);

                        actionBox.getChildren().addAll(viewBtn,resetBtn, deleteBtn);
                        actionBox.setAlignment(Pos.CENTER);

                        viewBtn.setOnAction(event -> viewTeacher(getTableRow().getIndex()));
                        deleteBtn.setOnAction(event -> deleteTeacher(getTableRow().getIndex()));
                        resetBtn.setOnAction(event -> resetPassword(getTableRow().getIndex()));
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
            params.put("page", String.valueOf(pageIndex + 1));
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
                                            "在职"
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
                                ShowMessage.showErrorMessage("加载失败", "服务器返回的数据格式不正确 (缺少教师列表)。");
                                handleFetchError();
                            }
                        } else {
                            String errorMsg = "获取教师数据失败";
                            if (jsonResponse.has("msg")) {
                                errorMsg += ": " + jsonResponse.get("msg").getAsString();
                            }
                            LOGGER.log(Level.WARNING, "API Error: " + errorMsg);
                            ShowMessage.showErrorMessage("加载失败", errorMsg);
                            handleFetchError();
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "解析教师数据失败", e);
                        ShowMessage.showErrorMessage("处理错误", "无法处理服务器响应。 Exception: " + e.getMessage());
                        handleFetchError();
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> { // 确保 UI 更新在 FX 线程执行
                        LOGGER.log(Level.SEVERE, "网络请求失败", ex);
                        ShowMessage.showErrorMessage("网络错误", "无法连接到服务器: " + ex.getMessage());
                        handleFetchError();
                    });
                    return null;
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
                                            "在职"
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
                        ShowMessage.showErrorMessage("处理错误", "无法处理服务器响应。 Exception: " + e.getMessage());
                        handleFetchError();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    LOGGER.log(Level.SEVERE, "解析教师数据失败", e);
                    ShowMessage.showErrorMessage("处理错误", "无法处理服务器响应。 Exception: " + e.getMessage());
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
         if (currentPageIndex < 0 || fetchedCount <= 0 && currentPageIndex == 0 && totalPages <= 1) {
             pageInfo.setText("第 1 页 / 共 1 页 - 显示 0 条");
             if(currentPageIndex == -1) pageInfo.setText("加载失败"); // 加载失败的特定情况
         } else {
            int pageNum = currentPageIndex + 1; // 显示页码从 1 开始
             int fromRecord = currentPageIndex * ROWS_PER_PAGE + 1;
             int toRecord = fromRecord + fetchedCount - 1;
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
            Map<String,String> params = new HashMap<>();
            params.put("userId",selectedTeacher.getId());
            NetworkUtils.post("/user/resetPassword", params, "", new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) throws IOException {
                    JsonObject res = gson.fromJson(result,JsonObject.class);
                    if(res.get("code").getAsInt() == 200){
                        ShowMessage.showInfoMessage("操作成功", "已重置教师 " + selectedTeacher.getName() + " 的密码(123456)");
                    }else{
                        ShowMessage.showErrorMessage("操作失败",res.get("msg").getAsString());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    JsonObject res = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")),JsonObject.class);
                    ShowMessage.showErrorMessage("重置失败",res.get("msg").getAsString());
                }
            });

        }
    }

    private void deleteTeacher(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= teacherTable.getItems().size()) return;
        TeacherInfo selectedTeacher = teacherTable.getItems().get(rowIndex);
        String teacherId = selectedTeacher.getId();
        String teacherName = selectedTeacher.getName();
        if (showConfirmDialog("确认删除", "确定要删除教师 " + teacherName + " (工号: "+ selectedTeacher.getSduid() +") 吗？")) {
            String endpoint = "/admin/deleteUser";
            String urlWithParams = endpoint + "?userId=" + teacherId;

             NetworkUtils.postAsync(urlWithParams, null) 
                .thenAcceptAsync(response -> Platform.runLater(() -> {
                    try {
                         JsonObject res = gson.fromJson(response, JsonObject.class);
                         if (res.has("code") && res.get("code").getAsInt() == 200) {
                                ShowMessage.showInfoMessage("操作成功", "已删除教师: " + teacherName);
                            fetchTeacherData(teacherPagination.getCurrentPageIndex());
                         } else {
                             String errorMsg = res.has("msg") ? res.get("msg").getAsString() : "未知错误";
                             ShowMessage.showErrorMessage("删除失败", "删除教师失败: " + errorMsg);
                         }
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "处理删除响应失败", e);
                        ShowMessage.showErrorMessage("处理错误", "无法处理删除响应。 Exception: " + e.getMessage());
                    }

                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "删除请求失败", ex);
                        ShowMessage.showErrorMessage("删除失败", "删除教师请求失败: " + ex.getMessage());
                    });
                    return null;
                });
        }
    }




    private boolean showConfirmDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
         alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

}
