package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.work.javafx.model.StudentInfo;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ShowMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassStudentListViewController {

    @FXML
    private TableView<StudentInfo> studentTable;

    @FXML
    private TableColumn<StudentInfo, Integer> sduidColumn;

    @FXML
    private TableColumn<StudentInfo, String> usernameColumn;

    @FXML
    private TableColumn<StudentInfo, Integer> idColumn;

    private ObservableList<StudentInfo> studentList = FXCollections.observableArrayList();
    private static final Gson gson = new Gson();

    @FXML
    public void initialize() {
        // 初始化 TableView 列与 StudentInfo 属性的绑定
        sduidColumn.setCellValueFactory(new PropertyValueFactory<>("sduid"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        // 设置 TableView 的数据源
        studentTable.setItems(studentList);
    }

    /**
     * 从外部调用，加载指定班级的学生数据
     * @param courseId 班级的ID
     */
    public void initializeData(String courseId) {
        if (courseId == null || courseId.isEmpty()) {
            showErrorAlert("错误", "班级ID无效。");
            return;
        }
        Map<String,String> params = new HashMap<>();
        params.put("id",courseId);
        String apiUrl = "/section/getSectionMember";

        // 清空旧数据
        studentList.clear();

        NetworkUtils.get(apiUrl,params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                Platform.runLater(() -> {
                    try {
                        JsonObject response = gson.fromJson(result, JsonObject.class);
                        if (response.has("code") && response.get("code").getAsInt() == 200) {
                            if (response.has("data") && response.get("data").isJsonArray()) {
                                JsonArray dataArray = response.getAsJsonArray("data");
                                Type listType = new TypeToken<List<StudentInfo>>() {}.getType();
                                List<StudentInfo> loadedStudents = gson.fromJson(dataArray, listType);

                                if (loadedStudents != null && !loadedStudents.isEmpty()) {
                                    studentList.addAll(loadedStudents);
                                } else {
                                    // 可以选择显示提示信息，如 "该班级暂无学生"
                                    System.out.println("班级 " + courseId + " 暂无学生数据");
                                }
                            } else {
                                System.err.println("API 响应格式错误：缺少 'data' 数组。");
                                showErrorAlert("数据错误", "无法解析学生数据，响应格式不正确。");
                            }
                        } else {
                            String msg = response.has("msg") ? response.get("msg").getAsString() : "未知错误";
                            System.err.println("获取学生列表失败: " + msg);
                            showErrorAlert("获取失败", "获取学生列表失败: " + msg);
                        }
                    } catch (Exception e) {
                        System.err.println("解析学生数据失败: " + e.getMessage());
                        e.printStackTrace();
                        showErrorAlert("解析错误", "解析学生数据时发生错误。");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    JsonObject res = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")), JsonObject.class);
                    if(res.get("code").getAsInt() == 404){
                        ShowMessage.showInfoMessage("无学生","当前班级暂无学生");
                    }else {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    // 显示错误弹窗
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


}
