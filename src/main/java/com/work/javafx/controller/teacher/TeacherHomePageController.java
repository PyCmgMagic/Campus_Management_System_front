package com.work.javafx.controller.teacher;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

// 移除对 TeacherBaseViewController 的继承
public class TeacherHomePageController implements Initializable {
    //卡片功能按钮
    @FXML private Label CompleteCourseSchedule;
    // 基础控制器引用
    private TeacherBaseViewController baseController;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        CompleteCourseSchedule.setOnMouseClicked(this::changetocourseScheduleManagement);
    }
    public void setBaseController(TeacherBaseViewController controller) {
        this.baseController = controller;
    }
    //查看完整课表
    private void changetocourseScheduleManagement(javafx.scene.input.MouseEvent mouseEvent) {
        baseController.switchTocourseScheduleManagement();
    }
}