package com.work.javafx.controller.teacher;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.work.javafx.util.NetworkUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.lang.reflect.Type;
import java.util.List;

public class StudentListViewController {

    @FXML
    private TableView<StudentInfo> studentTable;

    @FXML
    private TableColumn<StudentInfo, Integer> idColumn;

    @FXML
    private TableColumn<StudentInfo, String> usernameColumn;

    @FXML
    private TableColumn<StudentInfo, Integer> sectionNumberColumn;

    private ObservableList<StudentInfo> studentList = FXCollections.observableArrayList();
    private static final Gson gson = new Gson();

    @FXML
    public void initialize() {
        // 初始化 TableView 列与 StudentInfo 属性的绑定
        idColumn.setCellValueFactory(new PropertyValueFactory<>("sduid"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        sectionNumberColumn.setCellValueFactory(new PropertyValueFactory<>("sectionNumber"));

        // 设置 TableView 的数据源
        studentTable.setItems(studentList);
    }

    /**
     * 从外部调用，加载指定课程的学生数据
     * @param courseId 课程的ID (对应 API 中的 {courseId})
     */
    public void initializeData(String courseId) {
        if (courseId == null || courseId.isEmpty()) {
            showErrorAlert("错误", "课程ID无效。");
            return;
        }

        String apiUrl = "/class/" + courseId + "/students";

        // 清空旧数据
        studentList.clear();

        NetworkUtils.get(apiUrl, new NetworkUtils.Callback<String>() {
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
                                    // 可以选择显示提示信息，如 "该课程暂无学生"
                                    System.out.println("课程 " + courseId + " 暂无学生数据");
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
                    System.err.println("网络请求失败: " + e.getMessage());
                    e.printStackTrace();
                    showErrorAlert("网络错误", "无法连接到服务器，请检查网络连接。");
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

    // 用于 TableView 的数据模型类
    public static class StudentInfo {
        private int id;
        private String username;
        private int sectionNumber;
        private String sduid;

        public int getId() {
            return id;
        }

        public String getSduid() {
            return sduid;
        }

        public void setSduid(String sduid) {
            this.sduid = sduid;
        }

        public String getUsername() {
            return username;
        }

        public int getSectionNumber() {
            return sectionNumber;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setSectionNumber(int sectionNumber) {
            this.sectionNumber = sectionNumber;
        }
        
        public StudentInfo(int id, String username, int sectionNumber,String sduid) {
            this.id = id;
            this.username = username;
            this.sectionNumber = sectionNumber;
            this.sduid = sduid;
        }
        
        // Gson 需要无参构造函数（或者确保所有字段都有Setter）
        public StudentInfo() {}
    }
}
