package com.work.javafx.controller.teacher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.work.javafx.controller.student.UserInfo1;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.NetworkUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class PersonalCenterContent implements Initializable {
    @FXML private Label nameLabel;
    @FXML private Label stuIdLabel;
    Gson gson = new Gson();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //获取个人信息
        try {
            fetchUserInfo();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //显示用户信息
        loadUserInfo();
    }
    //加载用户信息
    public void loadUserInfo(){
        nameLabel.setText(UserSession.getInstance().getUsername());
        stuIdLabel.setText(UserSession.getInstance().getSduid());
    }
    //获取个人信息
    public void fetchUserInfo() throws IOException {
        Map<String,String> header = new HashMap<>();
        header.put("Authorization","Bearer "+ UserSession.getInstance().getToken());
        NetworkUtils.post("/user/getInfo", "", header, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject responseJson = gson.fromJson(result, JsonObject.class);
                if(responseJson.has("code")){
                    int code = responseJson.get("code").getAsInt();
                    if(code == 200){
                        JsonObject dataJson = responseJson.getAsJsonObject("data");
                        String username = dataJson.get("username").getAsString();
                        String email = dataJson.get("email").getAsString();
                        String phone = dataJson.get("phone").getAsString();
//                        String sex = dataJson.get("sex").getAsString();
                        String section = dataJson.get("section").getAsString();
                        String nation = dataJson.get("nation").getAsString();
                        String ethnic = dataJson.get("ethnic").getAsString();
                        String sduid = dataJson.get("sduid").getAsString();
                        String major = dataJson.get("major").getAsString();
                        UserSession.getInstance().setUsername(username);
                        UserSession.getInstance().setUsername(email);
                        UserSession.getInstance().setUsername(phone);
//                    UserSession.getInstance().setUsername(sex);
                        UserSession.getInstance().setUsername(section);
                        UserSession.getInstance().setUsername(nation);
                        UserSession.getInstance().setUsername(ethnic);
                        UserSession.getInstance().setUsername(sduid);
                        UserSession.getInstance().setUsername(major);

                    }
                }


            }

            @Override
            public void onFailure(Exception e) {

            }
        });
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
