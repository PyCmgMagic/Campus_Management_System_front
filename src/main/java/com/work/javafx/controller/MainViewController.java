package com.work.javafx.controller;

import com.work.javafx.MainApplication;
import com.work.javafx.util.ShowMessage;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * 主界面控制器
 * 负责处理教务管理系统主界面的交互逻辑
 */
public class MainViewController implements Initializable {
    @FXML
    private Button notificationBtn;
    @FXML
    private Button userBtn;
    @FXML
    private Button logoutBtn;
    @FXML
    private Label dateText;
    @FXML
    private VBox scheduleContainer;
    @FXML
    private Button viewAllBtn;
    // 添加菜单按钮引用
    @FXML private Button homeBtn;
    @FXML private Button personalCenterBtn;
    @FXML private Button courseScheduleBtn;
    @FXML private Button courseSelectionBtn;
    @FXML private Button gradeQueryBtn;
    @FXML private Button teachingEvaluationBtn;

    // 当前活动的按钮
    private Button currentActiveButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化控制器
        System.out.println("教务管理系统主界面界面初始化成功");

        // 设置当前日期
        updateCurrentDate();

        // 初始化按钮事件
        initButtonEvents();
        //初始化当前按钮
        currentActiveButton = homeBtn;
    }

    /**
     * 更新当前日期显示
     */
    private void updateCurrentDate() {
        // 如果界面上有日期标签，则更新为当前日期
        if (dateText != null) {
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE");
            String formattedDate = now.format(formatter);
            dateText.setText(formattedDate);
        }
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
     * 初始化按钮事件
     */
    private void initButtonEvents() {
        // 通知按钮点击事件
        if (notificationBtn != null) {
            notificationBtn.setOnAction(event -> {
                System.out.println("点击了通知按钮");
                showNotifications();
            });
        }

        // 用户按钮点击事件
        if (userBtn != null) {
            userBtn.setOnAction(event -> {
                System.out.println("点击了用户按钮");
                showUserProfile();
            });
        }

        // 退出登录按钮点击事件
        if (logoutBtn != null) {
            logoutBtn.setOnAction(event -> {
                System.out.println("点击了退出登录按钮");
                logout();
            });
        }

        // 查看完整课表按钮点击事件
        if (viewAllBtn != null) {
            viewAllBtn.setOnAction(event -> {
                System.out.println("点击了查看完整课表按钮");
                viewFullSchedule();
            });
        }

    }

    /**
     * 显示通知列表
     */
    private void showNotifications() {
        // TODO: 实现显示通知列表的逻辑
        System.out.println("显示通知列表");
    }

    /**
     * 显示用户个人资料
     */
    private void showUserProfile() {
        // TODO: 实现显示用户个人资料的逻辑
        System.out.println("显示用户个人资料");
    }

    /**
     * 退出登录
     */
    @FXML
    private void logout() {
        // TODO: 实现退出登录的逻辑
        //TODO： 清除token

        //切换到登陆界面
        try {
            MainApplication.changeView("Login.fxml","css/Login.css");
        } catch (IOException e) {
            e.printStackTrace();
              ShowMessage.showErrorMessage("退出登录失败",null);
        }
        System.out.println("执行退出登录操作");
    }

    /**
     * 查看完整课表
     */
    @FXML
    private void viewFullSchedule() {
        // TODO: 实现查看完整课表的逻辑
        System.out.println("查看完整课表");
    }

    /**
     * 切换到首页
     */
    @FXML
    private void switchToHome() {
        System.out.println("切换到首页");
        switchActiveButton(homeBtn);
        // 当前已经是首页，无需切换
    }

    /**
     * 切换到个人中心
     */
    @FXML
    private void switchToPersonalCenter() {
        System.out.println("切换到个人中心");
        switchActiveButton(personalCenterBtn);

        // TODO: 实现切换到个人中心的逻辑

    }

    /**
     * 切换到课表与课程
     */
    @FXML
    private void switchToCourseSchedule() {
        System.out.println("切换到课表");
        switchActiveButton(courseScheduleBtn);

        // TODO: 实现切换到课表与课程的逻辑
    }

    /**
     * 切换到选课系统
     */
    @FXML
    private void switchToCourseSelection() {
        System.out.println("切换到选课系统");
        switchActiveButton(courseSelectionBtn);

        // TODO: 实现切换到选课系统的逻辑
    }

    /**
     * 切换到成绩查询
     */
    @FXML
    private void switchToGradeQuery() {
        System.out.println("切换到成绩查询");
        switchActiveButton(gradeQueryBtn);

        // TODO: 实现切换到成绩查询的逻辑
    }


    /**
     * 切换到教学评价
     */
    @FXML
    private void switchToTeachingEvaluation() {
        System.out.println("切换到教学评价");
        switchActiveButton(teachingEvaluationBtn);
        // TODO: 实现切换到教学评价的逻辑
    }
}