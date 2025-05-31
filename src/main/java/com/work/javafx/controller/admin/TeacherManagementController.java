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

    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter;

    @FXML private TableView<TeacherInfo> teacherTable;
    @FXML private TableColumn<TeacherInfo, String> idColumn;
    @FXML private TableColumn<TeacherInfo, String> nameColumn;
    @FXML private TableColumn<TeacherInfo, String> departmentColumn;
    @FXML private TableColumn<TeacherInfo, String> contactColumn;
    @FXML private TableColumn<TeacherInfo, String> statusColumn;
    @FXML private TableColumn<TeacherInfo, Void> actionColumn;

    @FXML private Pagination teacherPagination;
    @FXML private Label pageInfo;

    private final int ROWS_PER_PAGE = 10;
    
    // 添加分页相关的成员变量
    private ObservableList<TeacherInfo> masterData = FXCollections.observableArrayList();
    private int totalItems = 0;
    private int totalPages = 1;
    private int currentPage = 1;
    
    // 页码变化监听器
    private javafx.beans.value.ChangeListener<Number> pageChangeListener;
    
    // 添加页面加载锁定标志
    private volatile boolean isPageLoadingLocked = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initFilters();
        initTeacherTable();
        initPagination();

        // 初始加载第 1 页数据
        loadTeachersFromApi(1);
    }

    private void initFilters() {
        departmentFilter.setItems(FXCollections.observableArrayList(
                "全部院系", "计算机学院", "数学学院", "物理学院", "外语学院", "经济管理学院","软件学院"
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
    /**
     * 初始化分页控件
     */
    private void initPagination() {
        teacherPagination.setCurrentPageIndex(0);
        teacherPagination.setPageFactory(this::createPage);
        
        // 添加页码变化监听器
        pageChangeListener = (obs, oldIndex, newIndex) -> {
             if (newIndex.intValue() != oldIndex.intValue() && !isPageLoadingLocked) {
                 isPageLoadingLocked = true;
                 loadTeachersFromApi(newIndex.intValue() + 1);
             } else if (isPageLoadingLocked) {
                 Platform.runLater(() -> {
                     teacherPagination.currentPageIndexProperty().removeListener(pageChangeListener);
                     teacherPagination.setCurrentPageIndex(oldIndex.intValue());
                     teacherPagination.currentPageIndexProperty().addListener(pageChangeListener);
                 });
             }
        };
        
        teacherPagination.currentPageIndexProperty().addListener(pageChangeListener);
        updatePageInfo();
    }

    /**
     * 创建分页页面内容
     */
    private Node createPage(int pageIndex) {
        return new VBox();
    }
    
    /**
     * 更新分页信息标签
     */
    private void updatePageInfo() {
        if (pageInfo == null) return;

        int itemsOnCurrentPage = masterData.size(); 

        if (totalItems == 0) {
            pageInfo.setText("共 0 条记录");
        } else {
            int fromIndex = (currentPage - 1) * ROWS_PER_PAGE + 1;
            int toIndex = Math.min(fromIndex + itemsOnCurrentPage - 1, totalItems);
             if (itemsOnCurrentPage == 0 && currentPage > 1) {
                 pageInfo.setText(String.format("第 %d 页 (共 %d 页) 无记录", currentPage, totalPages));
             } else if (itemsOnCurrentPage == 0 && totalItems > 0) {
                 pageInfo.setText(String.format("共 %d 条记录 (0 显示)", totalItems));
             } else {
                 pageInfo.setText(String.format("显示 %d-%d 条，共 %d 条 (第 %d/%d 页)", 
                                                 fromIndex, toIndex, totalItems, currentPage, totalPages));
             }
        }
    }

    /**
     * 从API加载教师数据
     * @param pageNum 页码（从1开始）
     */
    private void loadTeachersFromApi(int pageNum) {
        // 用户请求的页码，记录下来以便后续比较
        final int requestedPage = pageNum;
        
        if(searchField.getText().trim().isEmpty()){
            Map<String, String> params = new HashMap<>();
            params.put("page", String.valueOf(pageNum));
            params.put("limit", String.valueOf(ROWS_PER_PAGE));
            String selectedDepartment = departmentFilter.getValue();
            if (selectedDepartment != null && !"全部院系".equals(selectedDepartment)) {
                params.put("college", selectedDepartment);
            }

            // 确保UI被禁用，显示加载状态
            Platform.runLater(() -> {
                teacherTable.setDisable(true);
                teacherTable.setPlaceholder(new Label("正在加载数据..."));
            });

            NetworkUtils.getAsync("/admin/getTeacherList", params)
                .thenAcceptAsync(response -> {
                    try {
                        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

                        if (jsonResponse.has("code") && jsonResponse.get("code").getAsInt() == 200 && jsonResponse.has("data") && jsonResponse.get("data").isJsonObject()) {
                            JsonObject dataObject = jsonResponse.getAsJsonObject("data");

                            // 检查 'data' 中是否存在 'user' 数组
                            if (dataObject.has("user") && dataObject.get("user").isJsonArray()) {
                                JsonArray dataArray = dataObject.getAsJsonArray("user");
                                
                                // 获取分页信息
                                int apiTotalPages = 1;
                                if (dataObject.has("page") && dataObject.get("page").isJsonPrimitive()) {
                                    apiTotalPages = dataObject.get("page").getAsInt();
                                    if (apiTotalPages <= 0) apiTotalPages = 1;
                                }
                                
                                // 计算总记录数
                                int apiTotalItems = (apiTotalPages - 1) * ROWS_PER_PAGE + dataArray.size();
                                if (pageNum < apiTotalPages) {
                                    apiTotalItems = apiTotalPages * ROWS_PER_PAGE; // 估算值
                                }

                                ObservableList<TeacherInfo> newData = FXCollections.observableArrayList();
                                for (JsonElement element : dataArray) {
                                    JsonObject teacherJson = element.getAsJsonObject();
                                    TeacherInfo teacher = new TeacherInfo(
                                            getStringOrNull(teacherJson, "id"),
                                            getStringOrNull(teacherJson, "sduid"),
                                            getStringOrNull(teacherJson, "username"),
                                            getStringOrNull(teacherJson, "college"),
                                            getStringOrNull(teacherJson, "email"),
                                            "在职"
                                    );
                                    newData.add(teacher);
                                }

                                // 最后在UI线程中更新界面
                                int finalApiTotalItems = apiTotalItems;
                                int finalApiTotalPages = apiTotalPages;
                                Platform.runLater(() -> {
                                    try {
                                        // 保存API返回的数据到模型
                                        totalItems = finalApiTotalItems;
                                        totalPages = finalApiTotalPages;
                                        currentPage = requestedPage;
                                        
                                        // 只有当当前页面的数据确实是我们请求的页面的数据时，才更新UI
                                        if (requestedPage == pageNum) {
                                            // 临时禁用监听器
                                            teacherPagination.currentPageIndexProperty().removeListener(pageChangeListener);
                                            
                                            // 设置总页数
                                            teacherPagination.setPageCount(Math.max(1, totalPages));
                                            
                                            // 更新页码
                                            teacherPagination.setCurrentPageIndex(currentPage - 1);
                                            
                                            // 重新添加监听器
                                            teacherPagination.currentPageIndexProperty().addListener(pageChangeListener);
                                            
                                            // 更新表格数据
                                            masterData.setAll(newData);
                                            if (teacherTable.getItems() != masterData) {
                                                teacherTable.setItems(masterData);
                                            }
                                            
                                            if (newData.isEmpty() && pageNum == 1) {
                                                teacherTable.setPlaceholder(new Label("没有找到符合条件的教师数据"));
                                            } else if (newData.isEmpty() && pageNum > 1) {
                                                teacherTable.setPlaceholder(new Label("没有更多数据了"));
                                            }
                                        }
                                    } finally {
                                        // 无论如何都要解除加载锁定，启用UI
                                        isPageLoadingLocked = false;
                                        teacherTable.setDisable(false);
                                        updatePageInfo();
                                    }
                                });
                            } else {
                                Platform.runLater(() -> {
                                    try {
                                        ShowMessage.showErrorMessage("加载失败", "服务器返回的数据格式不正确 (缺少教师列表)。");
                                        handleFetchError();
                                    } finally {
                                        isPageLoadingLocked = false;
                                        teacherTable.setDisable(false);
                                        updatePageInfo();
                                    }
                                });
                            }
                        } else {
                            Platform.runLater(() -> {
                                try {
                                    String errorMsg = "获取教师数据失败";
                                    if (jsonResponse.has("msg")) {
                                        errorMsg += ": " + jsonResponse.get("msg").getAsString();
                                    }
                                    LOGGER.log(Level.WARNING, "API Error: " + errorMsg);
                                    ShowMessage.showErrorMessage("加载失败", errorMsg);
                                    handleFetchError();
                                } finally {
                                    isPageLoadingLocked = false;
                                    teacherTable.setDisable(false);
                                    updatePageInfo();
                                }
                            });
                        }
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            try {
                                LOGGER.log(Level.SEVERE, "解析教师数据失败", e);
                                ShowMessage.showErrorMessage("处理错误", "无法处理服务器响应。 Exception: " + e.getMessage());
                                handleFetchError();
                            } finally {
                                isPageLoadingLocked = false;
                                teacherTable.setDisable(false);
                                updatePageInfo();
                            }
                        });
                    }
                }, Platform::runLater)
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        try {
                            LOGGER.log(Level.SEVERE, "网络请求失败", ex);
                            ShowMessage.showErrorMessage("网络错误", "无法连接到服务器: " + ex.getMessage());
                            handleFetchError();
                        } finally {
                            isPageLoadingLocked = false;
                            teacherTable.setDisable(false);
                            updatePageInfo();
                        }
                    });
                    return null;
                });
        } else {
            // 搜索逻辑保持不变，但需要添加UI状态管理
            Map<String,String> params = new HashMap<>();
            params.put("keyword",searchField.getText());
            params.put("permission","1");
            
            // 确保UI被禁用，显示加载状态
            Platform.runLater(() -> {
                teacherTable.setDisable(true);
                teacherTable.setPlaceholder(new Label("正在搜索..."));
            });
            
            NetworkUtils.get("/admin/searchSdu", params, new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JsonObject jsonResponse = gson.fromJson(result, JsonObject.class);
                        if (jsonResponse.has("code") && jsonResponse.get("code").getAsInt() == 200 && jsonResponse.has("data") ) {
                                JsonObject data = jsonResponse.getAsJsonObject("data");
                                JsonArray dataArray = data.getAsJsonArray("list");
                                ObservableList<TeacherInfo> newData = FXCollections.observableArrayList();
                                for (JsonElement element : dataArray) {
                                    JsonObject teacherJson = element.getAsJsonObject();
                                    TeacherInfo teacher = new TeacherInfo(
                                            getStringOrNull(teacherJson, "id"),
                                            getStringOrNull(teacherJson, "sduid"),
                                            getStringOrNull(teacherJson, "username"),
                                            getStringOrNull(teacherJson, "college"),
                                            getStringOrNull(teacherJson, "email"),
                                            "在职"
                                    );
                                    newData.add(teacher);
                                }

                                Platform.runLater(() -> {
                                    try {
                                        // 搜索结果通常只有一页
                                        totalItems = newData.size();
                                        totalPages = 1;
                                        currentPage = 1;
                                        
                                        // 临时禁用监听器
                                        teacherPagination.currentPageIndexProperty().removeListener(pageChangeListener);
                                        
                                        // 设置总页数
                                        teacherPagination.setPageCount(1);
                                        
                                        // 更新页码
                                        teacherPagination.setCurrentPageIndex(0);
                                        
                                        // 重新添加监听器
                                        teacherPagination.currentPageIndexProperty().addListener(pageChangeListener);
                                        
                                        // 更新表格数据
                                        masterData.setAll(newData);
                                        teacherTable.setItems(masterData);
                                        
                                        if (newData.isEmpty()) {
                                            teacherTable.setPlaceholder(new Label("没有找到符合条件的教师数据"));
                                        }
                                    } finally {
                                        isPageLoadingLocked = false;
                                        teacherTable.setDisable(false);
                                        updatePageInfo();
                                    }
                                });
                            } else {
                                Platform.runLater(() -> {
                                    try {
                                        handleFetchError();
                                    } finally {
                                        isPageLoadingLocked = false;
                                        teacherTable.setDisable(false);
                                        updatePageInfo();
                                    }
                                });
                            }
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            try {
                                LOGGER.log(Level.SEVERE, "解析教师数据失败", e);
                                ShowMessage.showErrorMessage("处理错误", "无法处理服务器响应。 Exception: " + e.getMessage());
                                handleFetchError();
                            } finally {
                                isPageLoadingLocked = false;
                                teacherTable.setDisable(false);
                                updatePageInfo();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Platform.runLater(() -> {
                        try {
                            LOGGER.log(Level.SEVERE, "解析教师数据失败", e);
                            ShowMessage.showErrorMessage("处理错误", "无法处理服务器响应。 Exception: " + e.getMessage());
                            handleFetchError();
                        } finally {
                            isPageLoadingLocked = false;
                            teacherTable.setDisable(false);
                            updatePageInfo();
                        }
                    });
                }
            });
        }
    }

    private String getStringOrNull(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private void handleFetchError() {
        masterData.clear();
        teacherTable.setPlaceholder(new Label("加载数据失败，请重试"));
        totalItems = 0;
        totalPages = 1;
        currentPage = 1;
        teacherPagination.setPageCount(1);
        teacherPagination.setCurrentPageIndex(0);
    }

    private void triggerDataReload() {
         if (teacherPagination.getCurrentPageIndex() == 0) {
            loadTeachersFromApi(1);
        } else {
            teacherPagination.setCurrentPageIndex(0); // 否则跳回第一页（会自动触发 loadTeachersFromApi）
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
                                        loadTeachersFromApi(teacherPagination.getCurrentPageIndex() + 1);
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
                            loadTeachersFromApi(teacherPagination.getCurrentPageIndex() + 1);
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

    public void searchTeachers(ActionEvent actionEvent) {
        loadTeachersFromApi(1);
    }

    public void resetFilters(ActionEvent actionEvent) {
        searchField.clear();
        departmentFilter.setValue("全部院系");
        loadTeachersFromApi(1);
    }
}

