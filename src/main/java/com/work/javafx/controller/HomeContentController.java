package com.work.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.Scene;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * 首页内容控制器
 * 负责处理首页内容区域的交互逻辑
 */
public class HomeContentController implements Initializable {
    
    @FXML
    private Label dateText;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("首页内容初始化成功");
        
        // 更新当前日期显示
        updateCurrentDate();
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
     * 切换到课表查询
     * 通过获取基础视图控制器实例来切换视图
     */
    @FXML
    private void switchToCourseSchedule() {
        System.out.println("从首页切换到课表查询");
        
        try {
            // 获取当前场景
            Scene scene = dateText.getScene();
            if (scene != null) {
                // 获取基础视图控制器实例
                Object userData = scene.getUserData();
                if (userData instanceof BaseViewController) {
                    BaseViewController baseController = (BaseViewController) userData;
                    // 调用基础视图控制器的方法切换到课表查询
                    baseController.switchToCourseSchedule();
                } else {
                    System.out.println("无法获取基础视图控制器：userData不是BaseViewController类型");
                }
            } else {
                System.out.println("无法获取场景");
            }
        } catch (Exception e) {
            System.out.println("切换到课表查询时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 