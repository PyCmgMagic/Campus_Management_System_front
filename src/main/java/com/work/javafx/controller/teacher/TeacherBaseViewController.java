package com.work.javafx.controller.teacher;

import com.work.javafx.MainApplication;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.ShowMessage;
import com.work.javafx.util.ViewTransitionAnimation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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
public class TeacherBaseViewController implements Initializable {
    
    @FXML private StackPane contentArea;
    @FXML private Button userBtn;
    // 菜单按钮
    @FXML private Button homeBtn;
    @FXML private Button personalCenterBtn;
    @FXML private Button courseScheduleManagementBtn;
    @FXML private Button courseManagementBtn;
    @FXML private Button scoreInputBtn;

    // 当前活动的按钮
    private Button currentActiveButton;
    
    // 保存当前加载的视图ID
    private String currentViewId = "";
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("基础视图初始化成功");
        
        // 初始化当前按钮为首页
        currentActiveButton = homeBtn;
        homeBtn.getStyleClass().add("active-menu-item");
        
        // 默认加载首页内容
        loadView("TeacherHomePage.fxml");
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
            

            
            // 加载新的内容
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/com/work/javafx/teacher/" + fxmlPath)));
            Parent newView = loader.load();
            Object controller = loader.getController();
            if (controller instanceof TeacherHomePageController) {
                ((TeacherHomePageController) controller).setBaseController(this);
            }

            // 应用CSS样式
            String cssPath = getCssPathForView(fxmlPath);
            if (cssPath != null) {
                newView.getStylesheets().clear();
                newView.getStylesheets().add(
                        Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm());
            }
            // 使用ViewTransitionAnimation工具类应用动画
            ViewTransitionAnimation.playAnimationWithType(contentArea, newView, ViewTransitionAnimation.AnimationType.BOUNCE);

            
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
        if (fxmlPath.equals("TeacherHomePage.fxml")) {
            return "/com/work/javafx/css/teacher/TeacherBaseView.css";
        } else if (fxmlPath.equals("ScoreInputContent.fxml")) {
            return "/com/work/javafx/css/teacher/ScoreInputContent.css";
        } else if (fxmlPath.equals("CourseSelectionContent.fxml")) {
            return "/com/work/javafx/css/student/CourseSelection.css";
        } else if (fxmlPath.equals("PersonalCenterContent.fxml")) {
            return "/com/work/javafx/css/student/UserInfo.css";
        } else if (fxmlPath.equals("CourseScheduleContent_teacher.fxml")) {
            return "/com/work/javafx/css/teacher/TeacherCourseSchedule.css";
        }
        else if (fxmlPath.equals("courseManagementContent.fxml")) {
            return "/com/work/javafx/css/teacher/courseManagementContent.css";
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
    protected void switchToHome() {
        System.out.println("切换到首页");
        switchActiveButton(homeBtn);
        loadView("TeacherHomePage.fxml");
    }
    
    /**
     * 切换到个人中心
     */
    @FXML
    private void switchToPersonalCenter() {
        System.out.println("切换到个人中心");
        switchActiveButton(personalCenterBtn);
        loadView("PersonalCenterContent.fxml");
    }
    
    /**
     * 切换到课表查询
     */
    @FXML
    protected void switchTocourseScheduleManagement() {
        System.out.println("切换到课表查询");
        switchActiveButton(courseScheduleManagementBtn);
        loadView("CourseScheduleContent_teacher.fxml");
    }
    
    /**
     * 切换到课程管理
     */
    @FXML
    protected void switchTocourseManagement() {
        System.out.println("切换到课程管理");
        switchActiveButton(courseManagementBtn);
        loadView("courseManagementContent.fxml");
    }
    
    /**
     * 切换到成绩录入
     */
    @FXML
    protected void switchToscoreInput() {
        System.out.println("切换到成绩录入");
        switchActiveButton(scoreInputBtn);
        loadView("ScoreInputContent.fxml");
    }
    

}