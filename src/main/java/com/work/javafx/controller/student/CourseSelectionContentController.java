package com.work.javafx.controller.student;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.work.javafx.MainApplication;
import com.work.javafx.model.UltimateCourse;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ResUtil;
import com.work.javafx.util.ShowMessage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class CourseSelectionContentController implements Initializable {

    // 选课导航按钮
    @FXML private Button thisTermBtn;
    @FXML private Button selectedCoursesBtn;
    @FXML private Button courseResultBtn;

    // 查询条件控件
    @FXML private ComboBox<String> collegeComboBox;
    @FXML private ComboBox<String> courseTypeComboBox;
    @FXML private TextField courseNameField;
    @FXML private Button searchButton;

    // 课程表格
    @FXML private TableView<UltimateCourse> courseTableView;
    @FXML private TableColumn<UltimateCourse, Integer> numberColumn;
    @FXML private TableColumn<UltimateCourse, String> classNumColumn;
    @FXML private TableColumn<UltimateCourse, String> courseCodeColumn;
    @FXML private TableColumn<UltimateCourse, Integer> creditColumn;
    @FXML private TableColumn<UltimateCourse, String> courseTypeColumn;
    @FXML private TableColumn<UltimateCourse, String> teacherColumn;
    @FXML private TableColumn<UltimateCourse, String> timeLocationColumn;
    @FXML private TableColumn<UltimateCourse, String> capacityColumn;
    @FXML private TableColumn<UltimateCourse, String> actionColumn;

    // 分页控件
    @FXML private Label courseCountLabel;
    @FXML private Label currentPageLabel;

    // 当前活动的导航按钮
    private Button currentActiveNavButton;
    // 当前页码
    private int currentPage = 1;
    // 每页显示数量
    private final int PAGE_SIZE = 5;
    
    // 保存已选课程的映射表，用于标记课程状态
    private Map<Integer, Boolean> selectedCourseMap = new HashMap<>();
    Gson gson = new Gson();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化当前活动导航按钮
        currentActiveNavButton = thisTermBtn;
        // 初始化导航按钮样式（如果存在）
        if (thisTermBtn != null) {
            thisTermBtn.getStyleClass().add("active-nav-button");
        }
        switchActiveNavButton(currentActiveNavButton);

        // 初始化下拉框
        initComboBoxes();

        // 初始化表格
        initTableView();

        // 加载数据
        loadSampleCourses();

        System.out.println("选课系统界面初始化成功");
    }

    /**
     * 初始化下拉框选项
     */
    private void initComboBoxes() {
        // 学院下拉框
        ObservableList<String> colleges = FXCollections.observableArrayList(
                "所有学院", "计算机学院", "数学学院", "物理学院", "外语学院", "信息学院"
        );
        collegeComboBox.setItems(colleges);
        collegeComboBox.setValue("所有学院");

        // 课程性质下拉框
        ObservableList<String> courseTypes = FXCollections.observableArrayList(
                "所有类型", "必修课", "选修课", "通识课", "体育课"
        );
        courseTypeComboBox.setItems(courseTypes);
        courseTypeComboBox.setValue("所有类型");
    }

    /**
     * 初始化表格结构
     */
    private void initTableView() {
        // 设置列的值工厂
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        courseCodeColumn.setCellValueFactory(data -> {
            UltimateCourse course = data.getValue();
            return new SimpleStringProperty(course.getName());
        });
        creditColumn.setCellValueFactory(new PropertyValueFactory<>("point"));
        courseTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        classNumColumn.setCellValueFactory(new PropertyValueFactory<>("classNum"));
        teacherColumn.setCellValueFactory(data -> {
            UltimateCourse course = data.getValue();
            return new SimpleStringProperty( course.getTeacherName());
        });
        timeLocationColumn.setCellValueFactory(data -> {
            UltimateCourse course = data.getValue();
            String timeStr = "第" + course.getWeekStart() + "-" + course.getWeekEnd() + "周";
            return new SimpleStringProperty(timeStr + "\n" + course.getClassroom());
        });
        capacityColumn.setCellValueFactory(data -> {
            UltimateCourse course = data.getValue();
            // 这里暂时显示容量而非已选人数
            return new SimpleStringProperty(course.getSelectedCount()+ "/" + course.getCapacity());
        });

        // 自定义操作列
        actionColumn.setCellFactory(createActionCellFactory());

        // 自定义行样式
        courseTableView.setRowFactory(tv -> {
            TableRow<UltimateCourse> row = new TableRow<UltimateCourse>() {
                @Override
                protected void updateItem(UltimateCourse course, boolean empty) {
                    super.updateItem(course, empty);
                    if (course == null || empty) {
                        getStyleClass().removeAll("available-row", "selected-row", "full-row");
                    } else {
                        getStyleClass().removeAll("available-row", "selected-row", "full-row");
                        if (CourseSelectionContentController.this.isSelected(course)) {
                            getStyleClass().add("selected-row");
                        } else if (course.getCapacity() <= 0) {
                            getStyleClass().add("full-row");
                        } else {
                            getStyleClass().add("available-row");
                        }
                    }
                }
            };
            return row;
        });
    }

    /**
     * 检查课程是否已选
     */
    private boolean isSelected(UltimateCourse course) {
        return selectedCourseMap.getOrDefault(course.getId(), false);
    }

    /**
     * 创建自定义操作列工厂
     */
    private Callback<TableColumn<UltimateCourse, String>, TableCell<UltimateCourse, String>> createActionCellFactory() {
        return column -> new TableCell<UltimateCourse, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    UltimateCourse course = getTableView().getItems().get(getIndex());

                    Button actionButton = new Button();
                    if (CourseSelectionContentController.this.isSelected(course)) {
                        actionButton.setText("退选");
                        actionButton.getStyleClass().add("withdraw-button");
                        actionButton.setOnAction(event -> {
                            withdrawCourse(course);
                        });
                    } else if (course.getCapacity() <= 0) {
                        actionButton.setText("已满");
                        actionButton.getStyleClass().add("disabled-button");
                        actionButton.setDisable(true);
                    } else {
                        actionButton.setText("选课");
                        actionButton.getStyleClass().add("select-button");
                        actionButton.setOnAction(event -> {
                            selectCourse(course);
                        });
                    }

                    HBox box = new HBox(actionButton);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        };
    }

    /**
     * 加载数据
     */
    private void loadSampleCourses() {
        load();
    }

    /**
     * 查询课程
     */
    @FXML
    private void searchCourses() {
        String collegeName = collegeComboBox.getValue();
        String courseType = courseTypeComboBox.getValue();
        String courseName = courseNameField.getText();

        System.out.println("查询课程: 学院=" + collegeName + ", 课程性质=" + courseType + ", 课程名称=" + courseName);

        // 构建查询参数
        Map<String, String> params = new HashMap<>();
        params.put("keyword", courseName);
        
        // 发起网络请求
        NetworkUtils.get("/course-selection/search", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject responseJson = gson.fromJson(result, JsonObject.class);
                    
                    if (responseJson.has("code") && responseJson.get("code").getAsInt() == 200) {
                        processCoursesResponse(result);
                    } else {
                        // 处理错误
                        String message = responseJson.has("msg") ? 
                                responseJson.get("msg").getAsString() : "获取课程数据失败";
                        ShowMessage.showErrorMessage("查询失败", message);
                    }
                } catch (Exception e) {
                    String msg = ResUtil.getMsgFromException(e);
                    ShowMessage.showErrorMessage("数据解析错误", msg);
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                String msg = ResUtil.getMsgFromException(e);
                ShowMessage.showErrorMessage("数据解析错误", msg);
            }
        });
    }    /**
     * 获取未选课程列表
     */
    @FXML
    private void load() {

        // 发起网络请求
        NetworkUtils.get("/course-selection/unChoose", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    Gson gson = new Gson();
                    JsonObject responseJson = gson.fromJson(result, JsonObject.class);

                    if (responseJson.has("code") && responseJson.get("code").getAsInt() == 200) {
                        processCoursesResponse(result);
                    } else {
                        // 处理错误
                        String message = responseJson.has("msg") ?
                                responseJson.get("msg").getAsString() : "获取课程数据失败";
                        ShowMessage.showErrorMessage("查询失败", message);
                    }
                } catch (Exception e) {
                    String msg = ResUtil.getMsgFromException(e);
                    ShowMessage.showErrorMessage("数据解析错误", msg);
                }
            }

            @Override
            public void onFailure(Exception e) {
                String msg = ResUtil.getMsgFromException(e);
                ShowMessage.showErrorMessage("数据解析错误", msg);
            }
        });
    }
    
    /**
     * 解析课程数据
     */
    private List<UltimateCourse> parseCourseData(JsonArray coursesArray) {
        List<UltimateCourse> courses = new ArrayList<>();
        
        for (int i = 0; i < coursesArray.size(); i++) {
            JsonObject courseJson = coursesArray.get(i).getAsJsonObject();
            
            // 创建UltimateCourse对象并设置属性
            UltimateCourse course = new UltimateCourse();
            
            if (courseJson.has("id") && !courseJson.get("id").isJsonNull()) 
                course.setId(courseJson.get("id").getAsInt());
            
            if (courseJson.has("name") && !courseJson.get("name").isJsonNull()) 
                course.setName(courseJson.get("name").getAsString());
            else
                course.setName("");
            
            if (courseJson.has("category") && !courseJson.get("category").isJsonNull()) 
                course.setCategory(courseJson.get("category").getAsString());
            else
                course.setCategory("");
            
            if (courseJson.has("point") && !courseJson.get("point").isJsonNull()) 
                course.setPoint(courseJson.get("point").getAsInt());
            
            if (courseJson.has("teacherId") && !courseJson.get("teacherId").isJsonNull()) 
                course.setTeacherId(courseJson.get("teacherId").getAsInt());

            if (courseJson.has("teacherName") && !courseJson.get("teacherName").isJsonNull())
                course.setTeacherName(courseJson.get("teacherName").getAsString());
            
            if (courseJson.has("classroom") && !courseJson.get("classroom").isJsonNull()) 
                course.setClassroom(courseJson.get("classroom").getAsString());
            else
                course.setClassroom("");
            
            if (courseJson.has("weekStart") && !courseJson.get("weekStart").isJsonNull()) 
                course.setWeekStart(courseJson.get("weekStart").getAsInt());
            
            if (courseJson.has("weekEnd") && !courseJson.get("weekEnd").isJsonNull()) 
                course.setWeekEnd(courseJson.get("weekEnd").getAsInt());
            
            if (courseJson.has("period") && !courseJson.get("period").isJsonNull()) 
                course.setPeriod(courseJson.get("period").getAsInt());
            
            if (courseJson.has("time") && !courseJson.get("time").isJsonNull()) 
                course.setTime(courseJson.get("time").getAsString());
            else
                course.setTime("");
            
            if (courseJson.has("college") && !courseJson.get("college").isJsonNull()) 
                course.setCollege(courseJson.get("college").getAsString());
            else
                course.setCollege("");
            
            if (courseJson.has("term") && !courseJson.get("term").isJsonNull()) 
                course.setTerm(courseJson.get("term").getAsString());
            else
                course.setTerm("");
            
            if (courseJson.has("classNum") && !courseJson.get("classNum").isJsonNull()) 
                course.setClassNum(courseJson.get("classNum").getAsString());
            else
                course.setClassNum("");
            
            if (courseJson.has("type") && !courseJson.get("type").isJsonNull()) 
                course.setType(courseJson.get("type").getAsString());
            else
                course.setType("");
            
            if (courseJson.has("capacity") && !courseJson.get("capacity").isJsonNull()) 
                course.setCapacity(courseJson.get("capacity").getAsInt());
            if (courseJson.has("selectedCount") && !courseJson.get("selectedCount").isJsonNull())
                course.setSelectedCount(courseJson.get("selectedCount").getAsInt());
            
            if (courseJson.has("status") && !courseJson.get("status").isJsonNull()) 
                course.setStatus(courseJson.get("status").getAsString());
            else
                course.setStatus("");
            
            if (courseJson.has("intro") && !courseJson.get("intro").isJsonNull()) 
                course.setIntro(courseJson.get("intro").getAsString());
            else
                course.setIntro("");
            
            if (courseJson.has("examination") && !courseJson.get("examination").isJsonNull()) 
                course.setExamination(courseJson.get("examination").getAsInt());
            
            if (courseJson.has("f_reason") && !courseJson.get("f_reason").isJsonNull()) 
                course.setF_reason(courseJson.get("f_reason").getAsString());
            else
                course.setF_reason("");
            
            if (courseJson.has("published") && !courseJson.get("published").isJsonNull()) 
                course.setPublished(courseJson.get("published").getAsBoolean());
            
            if (courseJson.has("regularRatio") && !courseJson.get("regularRatio").isJsonNull()) 
                course.setRegularRatio(courseJson.get("regularRatio").getAsDouble());
            
            if (courseJson.has("finalRatio") && !courseJson.get("finalRatio").isJsonNull()) 
                course.setFinalRatio(courseJson.get("finalRatio").getAsDouble());
            
            courses.add(course);
        }
        
        return courses;
    }

    /**
     * 更新课程表格
     */
    private void updateCourseTable(List<UltimateCourse> courses) {
        courseTableView.setItems(FXCollections.observableArrayList(courses));
        courseCountLabel.setText("共找到" + courses.size() + "门课程");
    }

    /**
     * 显示本学期课程
     */
    @FXML
    private void showThisTermCourses() {
        if (thisTermBtn != null) {
            switchActiveNavButton(thisTermBtn);
        }
        System.out.println("显示本学期课程");
        loadSampleCourses();
    }




    /**
     * 显示选课结果
     */
    @FXML
    private void showCourseResults() {
        if (courseResultBtn != null) {
            switchActiveNavButton(courseResultBtn);
        }
        System.out.println("显示选课结果");
        

        NetworkUtils.get("/course-selection/results", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    processCoursesResponse(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    ShowMessage.showErrorMessage("数据解析错误", "无法解析服务器响应: " + e.getMessage());
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                ShowMessage.showErrorMessage("网络错误", "无法连接到服务器: " + e.getMessage());
                
                // 出错时加载空列表
                updateCourseTable(new ArrayList<>());
            }
        });
    }
    
    /**
     * 处理课程数据响应
     */
    private void processCoursesResponse(String result) {
        // 解析JSON响应
        Gson gson = new Gson();
        JsonObject responseJson = gson.fromJson(result, JsonObject.class);
        
        if (responseJson.has("code") && responseJson.get("code").getAsInt() == 200) {
            // 获取课程数据
            JsonArray coursesArray = responseJson.getAsJsonArray("data");
            List<UltimateCourse> courses = parseCourseData(coursesArray);
            
            // 如果是获取已选课程或选课结果，标记为已选
            if (currentActiveNavButton == selectedCoursesBtn || currentActiveNavButton == courseResultBtn) {
                for (UltimateCourse course : courses) {
                    selectedCourseMap.put(course.getId(), true);
                }
            }
            
            // 更新UI
            updateCourseTable(courses);
        } else {
            // 处理错误
            String message = responseJson.has("msg") ? 
                    responseJson.get("msg").getAsString() : "获取课程数据失败";
            ShowMessage.showErrorMessage("查询失败", message);
        }
    }



    
    /**
     * 选课操作
     */
    private void selectCourse(UltimateCourse course) {

        String url = "/course-selection/select/" + course.getId();
        
        // 发送选课请求
        NetworkUtils.post(url, "", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    Gson gson = new Gson();
                    JsonObject responseJson = gson.fromJson(result, JsonObject.class);
                    
                    if (responseJson.has("code") && responseJson.get("code").getAsInt() == 200) {
                        selectedCourseMap.put(course.getId(), true);
                        courseTableView.refresh();
                        ShowMessage.showInfoMessage("操作成功", "已成功选择课程：" + course.getName());
                    } else {
                        // 处理错误
                        String message = responseJson.has("msg") ? 
                                responseJson.get("msg").getAsString() : "选课失败";
                        ShowMessage.showErrorMessage("选课失败", message);
                    }
                } catch (Exception e) {
                    String msg = ResUtil.getMsgFromException(e);
                    ShowMessage.showErrorMessage("数据解析错误", msg);
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                String msg = ResUtil.getMsgFromException(e);
                ShowMessage.showErrorMessage("失败", msg);
            }
        });
    }
    
    /**
     * 退选操作
     */
    private void withdrawCourse(UltimateCourse course) {

        String url = "/course-selection/drop/" + course.getId();
        
        // 发送退选请求
        NetworkUtils.post(url, "", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    Gson gson = new Gson();
                    JsonObject responseJson = gson.fromJson(result, JsonObject.class);
                    
                    if (responseJson.has("code") && responseJson.get("code").getAsInt() == 200) {
                        selectedCourseMap.put(course.getId(), false);
                        courseTableView.refresh();
                        ShowMessage.showInfoMessage("操作成功", "已成功退选课程：" + course.getName());
                    } else {
                        String message = responseJson.has("msg") ?
                                responseJson.get("msg").getAsString() : "退选失败";
                        ShowMessage.showErrorMessage("退选失败", message);
                    }
                } catch (Exception e) {
                    String msg = ResUtil.getMsgFromException(e);
                    ShowMessage.showErrorMessage("失败", msg);
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                String msg = ResUtil.getMsgFromException(e);
                ShowMessage.showErrorMessage("失败", msg);
            }
        });
    }

    /**
     * 切换导航按钮高亮状态
     */
    private void switchActiveNavButton(Button newActiveButton) {
        // 进行空检查
        if (currentActiveNavButton == null || newActiveButton == null) {
            return;
        }
        
        // 移除当前活动按钮的高亮样式
        currentActiveNavButton.getStyleClass().remove("active-nav-button");

        // 为新的活动按钮添加高亮样式
        if (!newActiveButton.getStyleClass().contains("active-nav-button")) {
            newActiveButton.getStyleClass().add("active-nav-button");
        }

        // 更新当前活动按钮
        currentActiveNavButton = newActiveButton;
    }

    /**
     * 退出登录
     */
    @FXML
    private void logout() {
        try {
            MainApplication.changeView("Login.fxml", "css/Login.css");
        } catch (IOException e) {
            e.printStackTrace();
            ShowMessage.showErrorMessage("退出登录失败", null);
        }
    }
} 