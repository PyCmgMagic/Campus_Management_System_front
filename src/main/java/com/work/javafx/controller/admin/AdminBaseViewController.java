package com.work.javafx.controller.admin;

import com.work.javafx.MainApplication;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.ShowMessage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * 基础视图控制器
 * 负责处理顶部导航栏和侧边菜单，以及加载不同的内容视图
 */
public class AdminBaseViewController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Button userBtn;
    // 菜单按钮
    @FXML private Button homeBtn;
    @FXML private Button personalCenterBtn;
    @FXML private Button studentMangementBtn;
    @FXML private Button courseManagementBtn;
    @FXML private Button teacherManagementBtn;
    @FXML private Button classManagementBtn;
    @FXML private Button manageCourseBtn;
    // 当前活动的按钮
    private Button currentActiveButton;
    // 保存当前加载的视图ID
    private String currentView = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentActiveButton = homeBtn;
        homeBtn.getStyleClass().add("active-menu-item");
        loadView("AdminHomePage.fxml");
        loadUserName();
        userBtn.setOnAction(event -> switchToPersonalCenter());
    }
    //    加载右上角名称显示
    private void loadUserName(){
        int permission = UserSession.getInstance().getIdentity();
        String username = UserSession.getInstance().getUsername();
        String nameToShow = username.charAt(0) + "" ;
        switch (permission){
            case 0:
                nameToShow += "管理";
                break;
            case 1:
                nameToShow += " 老师";
                break;
            case 2:
                nameToShow += "同学";
                break;
            default:
        }
        userBtn.setText(nameToShow);
    }
    /**
     * 加载视图内容到内容区
     * @param fxmlPath FXML文件路径
     */
    private void loadView(String fxmlPath) {
        try {
            // 如果是相同的视图，则不重新加载
            if (fxmlPath.equals(currentView)) {
                return;
            }

            // 清空当前内容区
            contentArea.getChildren().clear();

            // 加载新的内容student
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/com/work/javafx/admin/" + fxmlPath)));
            Parent view = loader.load();

            // 应用CSS样式
            String cssPath = getCssPathForView(fxmlPath);
            if (cssPath != null) {
                view.getStylesheets().clear();
                view.getStylesheets().add(
                        Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm());
            }

            // 将新内容添加到内容区
            contentArea.getChildren().add(view);

            // 更新当前视图ID
            currentView = fxmlPath;

            System.out.println("成功加载视图: " + fxmlPath + (cssPath != null ? ", 应用样式: " + cssPath : ""));

        } catch (IOException e) {
            e.printStackTrace();
            ShowMessage.showErrorMessage("加载视图失败: " + fxmlPath, null);
        }
    }

    /**
     * 根据视图路径获取对应的CSS路径
     * @param fxmlPath 视图路径
     * @return CSS路径
     */
    private String getCssPathForView(String fxmlPath) {
        if (fxmlPath.equals("AdminHomePage.fxml")) {
            return "/com/work/javafx/css/admin/AdminHomePage.css";
        } else if (fxmlPath.equals("courseManagement.fxml")) {
            return "/com/work/javafx/css/admin/courseManagement.css";
        } else if (fxmlPath.equals("CourseSelectionContent.fxml")) {
            return "/com/work/javafx/css/admin/CourseSelection.css";
        } else if (fxmlPath.equals("PersonalCenterContent.fxml")) {
            return "/com/work/javafx/css/PersonalCenter.css";
        } else if (fxmlPath.equals("ScoreSearchContent.fxml")) {
            return "/com/work/javafx/css/admin/ScoreSearch.css";
        } else if (fxmlPath.equals("teacherManagement.fxml")) {
            return "/com/work/javafx/css/admin/teacherManagement.css";
        }else if (fxmlPath.equals("classManagement.fxml")) {
            return "/com/work/javafx/css/admin/classManagement.css";
        }
        return null;
    }

    /**
     * 切换到首页
     */
    @FXML
    private void switchToHome() {
        if (!"AdminHomePage.fxml".equals(currentView)) {
            resetMenuButtons();
            homeBtn.getStyleClass().add("active-menu-item");
            loadView("AdminHomePage.fxml");
            currentView = "AdminHomePage.fxml";
            currentActiveButton = homeBtn;
        }
    }

    /**
     * 切换到个人中心
     */
    @FXML
    private void switchToPersonalCenter() {
        if (!"personalCenter".equals(currentView)) {
            resetMenuButtons();
            personalCenterBtn.getStyleClass().add("active-menu-item");
            loadView("personalCenter.fxml");
            currentView = "personalCenter.fxml";
        }
    }

    /**
     * 切换到学生管理
     */
    @FXML
    protected void switchTostudentMangement() {
        if (!"studentMangement".equals(currentView)) {
            resetMenuButtons();
            studentMangementBtn.getStyleClass().add("active-menu-item");
            loadView("studentMangement.fxml");
            currentView = "studentMangement.fxml";
        }
    }

    /**
     * 切换到课程管理
     */
    @FXML
    private void switchTocourseManagement() {
        if (!"courseManagement".equals(currentView)) {
            resetMenuButtons();
            courseManagementBtn.getStyleClass().add("active-menu-item");
            loadView("courseManagement.fxml");
            currentView = "courseManagement.fxml";
        }
    }

    /**
     * 切换到教师管理
     */
    @FXML
    private void switchToteacherManagement() {
        if (!"teacherManagement".equals(currentView)) {
            resetMenuButtons();
            teacherManagementBtn.getStyleClass().add("active-menu-item");
            loadView("teacherManagement.fxml");
            currentView = "teacherManagement.fxml";
        }
    }  /**
     * 切换班级管理
     */
    @FXML
    private void switchToclassManagement() {
        if (!"classManagement".equals(currentView)) {
            resetMenuButtons();
            classManagementBtn.getStyleClass().add("active-menu-item");
            loadView("classManagement.fxml");
            currentView = "classManagement.fxml";
        }
    }

    /**
     * 切换到排课管理
     */
    @FXML
    private void switchTomanageCourse() {
        if (!"manageCourse".equals(currentView)) {
            resetMenuButtons();
            manageCourseBtn.getStyleClass().add("active-menu-item");
            loadView("manageCourse.fxml");
            currentView = "manageCourse.fxml";
        }
    }

    /**
     * 退出登录
     */
    @FXML
    private void logout() {
        //清除用户信息
        UserSession.getInstance().clearSession();

        // 切换到登录页面
        try {
            MainApplication.changeView("Login.fxml", "css/Login.css");
        } catch (IOException e) {
            e.printStackTrace();
            ShowMessage.showErrorMessage("退出登录失败", null);
        }
        System.out.println("执行退出登录操作");
    }

    private void resetMenuButtons() {
        homeBtn.getStyleClass().remove("active-menu-item");
        personalCenterBtn.getStyleClass().remove("active-menu-item");
        studentMangementBtn.getStyleClass().remove("active-menu-item");
        courseManagementBtn.getStyleClass().remove("active-menu-item");
        teacherManagementBtn.getStyleClass().remove("active-menu-item");
        manageCourseBtn.getStyleClass().remove("active-menu-item");
        classManagementBtn.getStyleClass().remove("active-menu-item");
    }
}