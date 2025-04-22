package com.work.javafx.controller.student;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class UserInfoController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void UserInfo1(ActionEvent event) throws IOException {
        // 创建新窗口（模态）
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("修改个人信息");

        // 加载 FXML 文件
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/student/UserInfo_1.fxml"));
        Parent root = loader.load();

        // 创建 Scene 并加载样式
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/com/work/javafx/css/student/UserInfo_1.css")).toExternalForm()
        );

        // 获取控制器并传递 Stage
        UserInfo1 controller = loader.getController();
        controller.setStage(popupStage);  // 你要在 ChangeUserInfo 类中加个 setStage(Stage) 方法

        // 设置 Scene 与所属窗口
        popupStage.setScene(scene);
        popupStage.initOwner(((Node)event.getSource()).getScene().getWindow());

        // 可选：添加图标
        // popupStage.getIcons().add(new Image("/com/work/images/icon.png"));

        // 显示弹窗
        popupStage.show();

        // 限制窗口大小
        popupStage.setResizable(false);
    }


}
