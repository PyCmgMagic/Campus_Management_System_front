package com.work.javafx.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * 消息提示工具类
 */
public class ShowMessage {
    
    /**
     * 显示信息类型消息对话框
     * @param title 标题
     * @param message 内容
     */
    public static void showInfoMessage(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("信息");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 显示警告类型消息对话框
     * @param title 标题
     * @param message 内容
     */
    public static void showWarningMessage(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("警告");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 显示错误类型消息对话框
     * @param title 标题
     * @param message 内容
     */
    public static void showErrorMessage(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 显示确认类型消息对话框
     * @param title 标题
     * @param message 内容
     * @return 是否确认
     */
    public static boolean showConfirmMessage(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("确认");
        alert.setHeaderText(title);
        alert.setContentText(message);
        return alert.showAndWait().get().getButtonData().isDefaultButton();
    }
}
