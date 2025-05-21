package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.work.javafx.entity.Data;
import com.work.javafx.model.term;
import com.work.javafx.util.NetworkUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ManageCourseController implements Initializable {

    private static final Gson gson = new Gson();

    @FXML
    private TextField newSemesterField; // 新学期输入框

    @FXML
    private Button addSemesterButton; // 添加学期按钮

    @FXML
    private ComboBox<String> semesterComboBox; // 学期选择下拉框

    @FXML
    private Button startSelectionButton; // 开始选课按钮

    @FXML
    private Button endSelectionButton; // 结束选课按钮

    @FXML
    private Button arrangeCoursesButton; // 执行排课按钮

    @FXML
    private Label statusLabel; // 状态显示标签
    @FXML
    private Label isOpenLabel; // 是否开始选课标签

    private ObservableList<String> semesterList; // 学期列表数据

    // 添加标记变量，防止无限循环
    private boolean isUpdatingFromServer = false;

    private List<term> termList = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        semesterList = FXCollections.observableArrayList();
        semesterComboBox.setItems(semesterList);
        firstloadSemesters(); // 初始化时加载已有学期
    }

    /**
     * 加载学期列表
     */
    private void firstloadSemesters() {
        NetworkUtils.get("/term/getTermList", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    JsonArray dataArray = res.getAsJsonArray("data");
                    List<String> loadedSemesters = new ArrayList<>();
                    for (int i = 0; i < dataArray.size(); i++) {
                        termList.add(new term(dataArray.get(i).getAsJsonObject().get("term").getAsString(), dataArray.get(i).getAsJsonObject().get("open").getAsBoolean()));
                        loadedSemesters.add(dataArray.get(i).getAsJsonObject().get("term").getAsString());
                    }
                    semesterList.clear();
                    semesterList.addAll(loadedSemesters);
                    Data.getInstance().setSemesterList(semesterList);
                    if (!semesterList.isEmpty()) {
                        isUpdatingFromServer = true; // 设置标记，避免触发无限循环
                        semesterComboBox.getSelectionModel().selectFirst(); // 默认选中第一个
                        // 更新初始选中项的状态显示
                        if (!termList.isEmpty()) {
                            updateTermStatus(termList.get(0));
                        }
                    }
                    updateStatus("学期列表加载成功", true);
                } else {
                    updateStatus("加载学期列表失败: " + (res.has("msg") ? res.get("msg").getAsString() : "未知错误"), false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                updateStatus("加载学期列表异常: " + e.getMessage(), false);
                e.printStackTrace();
            }
        });
    }


    /**
     * 在状态变更后刷新当前学期信息
     */
    private void refreshCurrentTermStatus() {
        isUpdatingFromServer = true;  // 设置标记，防止触发无限循环

        // 获取当前选中的学期
        String currentTerm = semesterComboBox.getValue();
        int currentIndex = semesterComboBox.getSelectionModel().getSelectedIndex();

        // 清空并重新加载数据
        termList.clear();
        NetworkUtils.get("/term/getTermList", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    JsonArray dataArray = res.getAsJsonArray("data");
                    List<String> loadedSemesters = new ArrayList<>();

                    for (int i = 0; i < dataArray.size(); i++) {
                        term newTerm = new term(
                                dataArray.get(i).getAsJsonObject().get("term").getAsString(),
                                dataArray.get(i).getAsJsonObject().get("open").getAsBoolean()
                        );
                        termList.add(newTerm);
                        loadedSemesters.add(newTerm.getTerm());
                    }

                    semesterList.clear();
                    semesterList.addAll(loadedSemesters);

                    // 恢复选择的学期
                    if (currentIndex >= 0 && currentIndex < semesterList.size()) {
                        semesterComboBox.getSelectionModel().select(currentIndex);
                    } else if (!semesterList.isEmpty()) {
                        // 如果索引无效，尝试找到相同名称的学期
                        for (int i = 0; i < semesterList.size(); i++) {
                            if (semesterList.get(i).equals(currentTerm)) {
                                semesterComboBox.getSelectionModel().select(i);
                                break;
                            }
                        }
                    }

                    // 更新当前选中学期的状态显示
                    if (!termList.isEmpty() && semesterComboBox.getValue() != null) {
                        for (term t : termList) {
                            if (t.getTerm().equals(semesterComboBox.getValue())) {
                                updateTermStatus(t);
                                break;
                            }
                        }
                    }
                } else {
                    updateStatus("刷新学期列表失败", false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                updateStatus("刷新学期状态异常: " + e.getMessage(), false);
                e.printStackTrace();
            }
        });
    }

    /**
     * 更新学期状态显示
     */
    private void updateTermStatus(term t) {
        isOpenLabel.setText(t.isOpen() ? "已开始选课" : "未开始选课");
        isOpenLabel.setTextFill(t.isOpen() ? Color.GREEN : Color.RED);
    }

    /**
     * 处理学期变更事件
     */
    public void handleTermChange(ActionEvent actionEvent) {
        if (isUpdatingFromServer) {
            isUpdatingFromServer = false;
            return;
        }

        String selectedTerm = semesterComboBox.getValue();
        if (selectedTerm == null) {
            return;
        }

        for (term t : termList) {
            if (t.getTerm().equals(selectedTerm)) {
                updateTermStatus(t);
                break;
            }
        }
    }

    /**
     * 处理添加学期按钮点击事件
     */
    @FXML
    private void handleAddSemester() {
        String newSemester = newSemesterField.getText().trim();
        if (newSemester.isEmpty()) {
            updateStatus("请输入新学期名称", false);
            return;
        }

        // 校验学期格式
        if (!newSemester.matches("\\d{4}-\\d{4}-[12]")) {
            updateStatus("学期格式不正确，应为 YYYY-YYYY-1 或 YYYY-YYYY-2", false);
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("term", newSemester);

        NetworkUtils.post("/term/addTerm", params, null, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    updateStatus("学期 '" + newSemester + "' 添加成功", true);
                    newSemesterField.clear(); // 清空输入框
                    refreshCurrentTermStatus(); // 刷新学期列表
                } else {
                    updateStatus("添加学期失败: " + (res.has("msg") ? res.get("msg").getAsString() : "未知错误"), false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                updateStatus("添加学期异常: " + e.getMessage(), false);
                e.printStackTrace();
            }
        });
    }

    /**
     * 处理开始选课按钮点击事件
     */
    @FXML
    private void handleStartSelection() {
        String selectedSemester = semesterComboBox.getSelectionModel().getSelectedItem();
        if (selectedSemester == null || selectedSemester.isEmpty()) {
            updateStatus("请先选择一个学期", false);
            return;
        }
        performSemesterAction("/term/editSelection", selectedSemester, "开始选课");
    }

    /**
     * 处理结束选课按钮点击事件
     */
    @FXML
    private void handleEndSelection() {
        String selectedSemester = semesterComboBox.getSelectionModel().getSelectedItem();
        if (selectedSemester == null || selectedSemester.isEmpty()) {
            updateStatus("请先选择一个学期", false);
            return;
        }
        performSemesterAction("/term/editSelection", selectedSemester, "结束选课");
    }

    /**
     * 处理执行排课按钮点击事件
     */
    @FXML
    private void handleArrangeCourses() {
        String selectedSemester = semesterComboBox.getSelectionModel().getSelectedItem();
        if (selectedSemester == null || selectedSemester.isEmpty()) {
            updateStatus("请先选择一个学期", false);
            return;
        }
        performSemesterAction("/class/autoSchedule", selectedSemester, "执行排课");
    }

    /**
     * 执行与学期相关的操作 (开始/结束选课, 排课)
     *
     * @param endpoint API 接口路径
     * @param semester 学期
     * @param actionName 操作名称 (用于显示状态信息)
     */
    private void performSemesterAction(String endpoint, String semester, String actionName) {
        Map<String, String> params = new HashMap<>();
        params.put("term", semester);
        if (actionName.equals("开始选课")) {
            params.put("open", "true");
        } else if (actionName.equals("结束选课")) {
            params.put("open", "false");
        } else if (actionName.equals("执行排课")) {

        }
        updateStatus("正在为学期 '" + semester + "' " + actionName + "...", true);

        NetworkUtils.post(endpoint, params, null, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    updateStatus("学期 '" + semester + "' " + actionName + " 成功", true);
                    // 操作成功后刷新学期状态
                    refreshCurrentTermStatus();
                } else {
                    updateStatus(actionName + " 失败: " + (res.has("msg") ? res.get("msg").getAsString() : "未知错误"), false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                JsonObject err = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")), JsonObject.class);
                updateStatus(actionName + " 异常: " + err.get("msg").getAsString(), false);
                e.printStackTrace();
            }
        });
    }

    /**
     * 更新状态标签的文本和颜色
     *
     * @param message 状态信息
     * @param isSuccess 是否成功
     */
    private void updateStatus(String message, boolean isSuccess) {
        statusLabel.setText(message);
        statusLabel.setTextFill(isSuccess ? Color.GREEN : Color.RED);
    }
}
