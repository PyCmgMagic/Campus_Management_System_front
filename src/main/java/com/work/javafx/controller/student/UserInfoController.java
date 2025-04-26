package com.work.javafx.controller.student;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.NetworkUtils;
import javafx.application.Platform;
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

public class UserInfoController implements Initializable {
    @FXML private Label nameLabel;
    @FXML private Label stuIdLabel;
    @FXML private Label genderLabel;
    @FXML private Label nationLabel;
    @FXML private Label ethnicLabel;
    @FXML private Label politicsLabel;
    @FXML private Label majorLabel;
    @FXML private Label classLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label entryLabel;
    @FXML private Label graduationLabel;
    @FXML private Label userLabel;
    @FXML private Label useridLabel;
    Gson gson = new Gson();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 先设置默认值或加载中状态
        setDefaultValues();

        // 异步获取个人信息
        fetchUserInfo();
    }

    // 设置默认值
    private void setDefaultValues() {
        nameLabel.setText("加载中...");
        stuIdLabel.setText("加载中...");
        genderLabel.setText("加载中...");
        nationLabel.setText("加载中...");
        ethnicLabel.setText("加载中...");
        politicsLabel.setText("加载中...");
        majorLabel.setText("加载中...");
        classLabel.setText("加载中...");
        phoneLabel.setText("加载中...");
        emailLabel.setText("加载中...");
        userLabel.setText("加载中...");
        useridLabel.setText("加载中...");
    }

    // 专业代码转换方法
    private String convertMajorCode(String majorCode) {
        if (majorCode == null || majorCode.trim().isEmpty()) {
            return "未知专业";
        }

        try {
            int code = Integer.parseInt(majorCode);
            switch (code) {
                case 0: return "软件工程";
                case 1: return "数字媒体技术";
                case 2: return "大数据";
                case 3: return "人工智能";
                default: return "未知专业";
            }
        } catch (NumberFormatException e) {
            return majorCode; // 如果不是数字，直接返回原始值
        }
    }

    // 加载用户信息到UI
    private void loadUserInfo(){
        Platform.runLater(() -> {
            nameLabel.setText(UserSession.getInstance().getUsername());
            stuIdLabel.setText(UserSession.getInstance().getSduid());
            genderLabel.setText(UserSession.getInstance().getSex());
            nationLabel.setText(UserSession.getInstance().getNation());
            ethnicLabel.setText(UserSession.getInstance().getEthnic());
            politicsLabel.setText(UserSession.getInstance().getPoliticsStatus());
            majorLabel.setText(convertMajorCode(UserSession.getInstance().getMajor()));
            classLabel.setText(UserSession.getInstance().getSection());
            phoneLabel.setText(UserSession.getInstance().getPhone());
            emailLabel.setText(UserSession.getInstance().getEmail());
            userLabel.setText(UserSession.getInstance().getUsername());
            useridLabel.setText(UserSession.getInstance().getSduid());
        });
    }

    // 获取用户信息
    public void fetchUserInfo() {
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

                        // 更新UserSession中的数据
                        updateUserSession(dataJson);

                        // 数据加载完成后更新UI
                        loadUserInfo();
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    // 显示错误信息
                    nameLabel.setText("加载失败");
                    stuIdLabel.setText("加载失败");
                    // 其他标签...
                });
            }
        });
    }

    // 更新UserSession中的数据
    private void updateUserSession(JsonObject dataJson) {
        try {
            if(dataJson.has("phone") && !dataJson.get("phone").isJsonNull()) {
                UserSession.getInstance().setPhone(dataJson.get("phone").getAsString());
            } else {
                UserSession.getInstance().setPhone("");
            }

            if(dataJson.has("email") && !dataJson.get("email").isJsonNull()) {
                UserSession.getInstance().setEmail(dataJson.get("email").getAsString());
            } else {
                UserSession.getInstance().setEmail("");
            }

            if(dataJson.has("username") && !dataJson.get("username").isJsonNull()) {
                UserSession.getInstance().setUsername(dataJson.get("username").getAsString());
            } else {
                UserSession.getInstance().setUsername("");
            }

            if(dataJson.has("section") && !dataJson.get("section").isJsonNull()) {
                UserSession.getInstance().setSection(dataJson.get("section").getAsString());
            } else {
                UserSession.getInstance().setSection("");
            }

            if(dataJson.has("politicsStatus") && !dataJson.get("politicsStatus").isJsonNull()) {
                UserSession.getInstance().setPoliticsStatus(dataJson.get("politicsStatus").getAsString());
            } else {
                UserSession.getInstance().setPoliticsStatus("");
            }

            if(dataJson.has("nation") && !dataJson.get("nation").isJsonNull()) {
                UserSession.getInstance().setNation(dataJson.get("nation").getAsString());
            } else {
                UserSession.getInstance().setNation("");
            }

            if(dataJson.has("ethnic") && !dataJson.get("ethnic").isJsonNull()) {
                UserSession.getInstance().setEthnic(dataJson.get("ethnic").getAsString());
            } else {
                UserSession.getInstance().setEthnic("");
            }

            if(dataJson.has("sex") && !dataJson.get("sex").isJsonNull()) {
                UserSession.getInstance().setSex(dataJson.get("sex").getAsString());
            } else {
                UserSession.getInstance().setSex("");
            }

            if(dataJson.has("major") && !dataJson.get("major").isJsonNull()) {
                UserSession.getInstance().setMajor(dataJson.get("major").getAsString());
            } else {
                UserSession.getInstance().setMajor("");
            }

            if(dataJson.has("sduid") && !dataJson.get("sduid").isJsonNull()) {
                UserSession.getInstance().setSduid(dataJson.get("sduid").getAsString());
            } else {
                UserSession.getInstance().setSduid("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 修改个人信息弹窗
    public void UserInfo1(ActionEvent event) throws IOException {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("修改个人信息");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/student/UserInfo_1.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/com/work/javafx/css/student/UserInfo_1.css")).toExternalForm()
        );

        UserInfo1 controller = loader.getController();
        controller.setStage(popupStage);

        popupStage.setScene(scene);
        popupStage.initOwner(((Node)event.getSource()).getScene().getWindow());
        popupStage.show();
        popupStage.setResizable(false);
    }

    public Label getGenderLabel() {
        return genderLabel;
    }
}
