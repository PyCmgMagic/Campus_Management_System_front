package com.work.javafx.controller.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 管理员首页控制器
 * 处理管理员控制台页面的交互逻辑
 */
public class AdminHomePageController implements Initializable {

    @FXML
    private Label studentCountLabel;

    @FXML
    private Label teacherCountLabel;

    @FXML
    private VBox noticeListContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 确保CSS样式表已正确加载
        ensureStylesheetsLoaded();
        
        // 加载数据
        loadStatistics();
        loadNotices();
    }

    /**
     * 确保CSS样式表已正确加载
     */
    private void ensureStylesheetsLoaded() {
        if (noticeListContainer.getScene() != null && 
            !noticeListContainer.getScene().getStylesheets().contains("/css/admin-home.css")) {
            noticeListContainer.getScene().getStylesheets().add("/css/admin-home.css");
        }
    }

    /**
     * 加载统计数据
     */
    private void loadStatistics() {
        // 这里可以从服务或数据库加载真实数据
        // 目前使用硬编码的演示数据
        studentCountLabel.setText("12,486");
        teacherCountLabel.setText("843");
    }

    /**
     * 加载公告列表
     */
    private void loadNotices() {
        // 清空容器
        noticeListContainer.getChildren().clear();
        
        // 添加示例公告 (在实际应用中应该从数据库加载)
        addNoticeItem(
            "关于2025年春季学期期末考试安排的通知", 
            "发布时间：今天 10:30 | 发布人：教务处"
        );
        
        addNoticeItem(
            "2025-2026学年学生注册须知", 
            "发布时间：昨天 14:15 | 发布人：学籍管理部"
        );
        
        addNoticeItem(
            "关于暑期学校课程报名的通知", 
            "发布时间：3天前 | 发布人：教务处"
        );
    }
    
    /**
     * 添加一个公告项到列表
     */
    private void addNoticeItem(String title, String timeInfo) {
        HBox noticeItem = new HBox();
        noticeItem.getStyleClass().add("notice-item");
        
        // 公告图标
        StackPane icon = new StackPane();
        icon.getStyleClass().add("notice-icon");
        
        // 公告内容
        VBox content = new VBox();
        content.getStyleClass().add("notice-content");
        
        // 标题行
        HBox titleBox = new HBox();
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("notice-title");
        titleBox.getChildren().add(titleLabel);

        
        // 时间信息
        Label timeLabel = new Label(timeInfo);
        timeLabel.getStyleClass().add("notice-time");
        
        content.getChildren().addAll(titleBox, timeLabel);
        
        // 操作按钮
        HBox actions = new HBox();
        actions.getStyleClass().add("notice-actions");
        
        Button editBtn = new Button("编辑");
        editBtn.getStyleClass().addAll("notice-btn", "edit-btn");
        editBtn.setOnAction(e -> editNotice(title));
        
        Button deleteBtn = new Button("删除");
        deleteBtn.getStyleClass().addAll("notice-btn", "delete-btn");
        deleteBtn.setOnAction(e -> deleteNotice(title));
        
        actions.getChildren().addAll(editBtn, deleteBtn);
        
        // 组合所有元素
        noticeItem.getChildren().addAll(icon, content, actions);
        noticeListContainer.getChildren().add(noticeItem);
    }
    
    /**
     * 发布新公告按钮处理程序
     */
    @FXML
    private void publishNewNotice() {
        // 打开发布新公告的界面
        System.out.println("打开发布新公告界面");
    }
    
    /**
     * 编辑公告
     */
    private void editNotice(String noticeTitle) {
        System.out.println("编辑公告: " + noticeTitle);
    }
    
    /**
     * 删除公告
     */
    private void deleteNotice(String noticeTitle) {
        System.out.println("删除公告: " + noticeTitle);
    }
    
    /**
     * 导航到学生管理界面
     */
    @FXML
    private void navigateToStudentManagement() {
        System.out.println("导航到学生管理界面");
    }
    
    /**
     * 导航到教师管理界面
     */
    @FXML
    private void navigateToTeacherManagement() {
        System.out.println("导航到教师管理界面");
    }
    
    /**
     * 导航到课程管理界面
     */
    @FXML
    private void navigateToCourseManagement() {
        System.out.println("导航到课程管理界面");
    }
    
    /**
     * 导航到排课管理界面
     */
    @FXML
    private void navigateToScheduleManagement() {
        System.out.println("导航到排课管理界面");
    }
    
    /**
     * 导航到考试管理界面
     */
    @FXML
    private void navigateToExamManagement() {
        System.out.println("导航到考试管理界面");
    }
    
    /**
     * 导航到公告管理界面
     */
    @FXML
    private void navigateToNoticeManagement() {
        System.out.println("导航到公告管理界面");
    }
} 