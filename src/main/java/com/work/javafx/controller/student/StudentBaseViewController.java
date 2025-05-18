package com.work.javafx.controller.student;

import com.work.javafx.MainApplication;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.ShowMessage;
import com.work.javafx.util.ViewTransitionAnimation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * 基础视图控制器
 * 负责处理顶部导航栏和侧边菜单，以及加载不同的内容视图
 */
public class StudentBaseViewController implements Initializable {
    
    @FXML private StackPane contentArea;
    //右上角名称展示
    @FXML Button userBtn;
    // 菜单按钮
    @FXML private Button homeBtn;
    @FXML private Button personalCenterBtn;
    @FXML private Button courseScheduleBtn;
    @FXML private Button courseSelectionBtn;
    @FXML private Button gradeQueryBtn;
    @FXML private Button teachingEvaluationBtn;

    // 当前活动的按钮
    private Button currentActiveButton;
    
    // 保存当前加载的视图ID
    private String currentViewId = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("基础视图初始化成功");

        // 设置当前 controller 为 Scene 的 userData，便于其他 controller 获取
        Platform.runLater(() -> {
            Scene scene = contentArea.getScene();
            if (scene != null) {
                scene.setUserData(this);
            }
        });

        // 其他初始化内容
        currentActiveButton = homeBtn;
        homeBtn.getStyleClass().add("active-menu-item");
        loadView("StudentHomeContent.fxml");
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
            if (fxmlPath.equals(currentViewId)) {
                return;
            }
            

            // 加载新的内容student
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/com/work/javafx/student/" + fxmlPath)));
            Parent newView = loader.load();


            // 应用CSS样式
            String cssPath = getCssPathForView(fxmlPath);
            if (cssPath != null) {
                newView.getStylesheets().clear();
                newView.getStylesheets().add(
                        Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm());
            }

            // 使用ViewTransitionAnimation工具类应用动画
            ViewTransitionAnimation.playAnimation(contentArea, newView);
            
            // 更新当前视图ID
            currentViewId = fxmlPath;
            
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
        if (fxmlPath.equals("StudentHomeContent.fxml")) {
            return "/com/work/javafx/css/student/HomeContent.css";
        } else if (fxmlPath.equals("CourseScheduleContent.fxml")) {
            return "/com/work/javafx/css/student/CourseSchedule.css";
        } else if (fxmlPath.equals("CourseSelectionContent.fxml")) {
            return "/com/work/javafx/css/student/CourseSelection.css";
        } else if (fxmlPath.equals("PersonalCenterContent.fxml")) {
            return "/com/work/javafx/css/student/UserInfo.css";
        } else if (fxmlPath.equals("ScoreSearchContent.fxml")) {
            return "/com/work/javafx/css/student/ScoreSearch.css";
        } else if (fxmlPath.equals("TeachingEvaluationContent.fxml")) {
            return "/com/work/javafx/css/student/TeachingEvaluation.css";
        }
        else if (fxmlPath.equals("UserInfo.fxml")) {
            return "/com/work/javafx/css/student/UserInfo.css";
        }
        return null;
    }
    
    /**
     * 切换按钮高亮状态
     * @param newActiveButton 需要高亮的按钮
     */
    private void switchActiveButton(Button newActiveButton) {
        // 移除当前活动按钮的高亮样式
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active-menu-item");
        }
        
        // 为新的活动按钮添加高亮样式
        if (newActiveButton != null && !newActiveButton.getStyleClass().contains("active-menu-item")) {
            newActiveButton.getStyleClass().add("active-menu-item");
        }
        
        // 更新当前活动按钮
        currentActiveButton = newActiveButton;
    }
    
    /**
     * 退出登录
     */
    @FXML
    private void logout() {
        //清除用户信息
        UserSession.getInstance().clearSession();
        MainApplication.stopTokenRefreshTimer();
        // 切换到登录页面
        try {
            MainApplication.changeView("Login.fxml", "css/Login.css");
        } catch (IOException e) {
            e.printStackTrace();
            ShowMessage.showErrorMessage("退出登录失败", null);
        }
        System.out.println("执行退出登录操作");
    }
    
    /**
     * 切换到首页
     */
    @FXML
    private void switchToHome() {
        System.out.println("切换到首页");
        switchActiveButton(homeBtn);
        loadView("StudentHomeContent.fxml");
    }

    /**
     * 切换到个人中心
     */
    @FXML
    private void switchToPersonalCenter() {
        System.out.println("切换到个人中心");
        switchActiveButton(personalCenterBtn);
        loadView("UserInfo.fxml");
    }
    
    /**
     * 切换到课表查询
     */
    @FXML
    public void switchToCourseSchedule() {
        System.out.println("切换到课表查询");
        switchActiveButton(courseScheduleBtn);
        loadView("CourseScheduleContent.fxml");
    }
    
    /**
     * 切换到选课系统
     */
    @FXML
    private void switchToCourseSelection() {
        System.out.println("切换到选课系统");
        switchActiveButton(courseSelectionBtn);
        loadView("CourseSelectionContent.fxml");
    }
    
    /**
     * 切换到成绩查询
     */
    @FXML
    private void switchToGradeQuery() {
        System.out.println("切换到成绩查询");
        switchActiveButton(gradeQueryBtn);
        loadView("ScoreSearchContent.fxml");
    }
    
    /**
     * 切换到教学评价
     */
    @FXML
    private void switchToTeachingEvaluation() {
        System.out.println("切换到教学评价");
        switchActiveButton(teachingEvaluationBtn);
        loadView("TeachingEvaluationContent.fxml");
    }
} 