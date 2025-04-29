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
    @FXML private Label surnameLabel;   // 姓名首字母标签
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

    private Gson gson = new Gson();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 先设置默认值或加载中状态
        setDefaultValues();

        // 异步获取个人信息
        fetchUserInfo();
    }

    // 设置默认值
    private void setDefaultValues() {
        setLabels("加载中...");
    }

    // 设置多个标签的默认值
    private void setLabels(String text) {
        nameLabel.setText(text);
        stuIdLabel.setText(text);
        genderLabel.setText(text);
        nationLabel.setText(text);
        ethnicLabel.setText(text);
        politicsLabel.setText(text);
        majorLabel.setText(text);
        classLabel.setText(text);
        phoneLabel.setText(text);
        emailLabel.setText(text);
        userLabel.setText(text);
        useridLabel.setText(text);
    }

    // 提取名字的首字母并返回大写字母
    private String getFirstLetter(String fullName) {
        if (fullName != null && !fullName.isEmpty()) {
            return fullName.substring(0, 1).toUpperCase();
        }
        return "";  // 如果名字为空，返回空字符串
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
    private void loadUserInfo() {
        Platform.runLater(() -> {
            // 设置头像的首字母
            String fullName = UserSession.getInstance().getUsername();
            String firstLetter = getFirstLetter(fullName);  // 调用提取首字母的方法
            surnameLabel.setText(firstLetter);  // 更新头像显示首字母

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
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer " + UserSession.getInstance().getToken());

        NetworkUtils.post("/user/getInfo", "", header, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject responseJson = gson.fromJson(result, JsonObject.class);
                if (responseJson.has("code")) {
                    int code = responseJson.get("code").getAsInt();
                    if (code == 200) {
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
                    setLabels("加载失败");
                });
            }
        });
    }

    // 更新UserSession中的数据
    private void updateUserSession(JsonObject dataJson) {
        try {
            UserSession session = UserSession.getInstance();

            session.setPhone(getJsonValue(dataJson, "phone"));
            session.setEmail(getJsonValue(dataJson, "email"));
            session.setUsername(getJsonValue(dataJson, "username"));
            session.setSection(getJsonValue(dataJson, "section"));
            session.setPoliticsStatus(getJsonValue(dataJson, "politicsStatus"));
            session.setNation(getJsonValue(dataJson, "nation"));
            session.setEthnic(getJsonValue(dataJson, "ethnic"));
            session.setSex(getJsonValue(dataJson, "sex"));
            session.setMajor(getJsonValue(dataJson, "major"));
            session.setSduid(getJsonValue(dataJson, "sduid"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 辅助方法：获取Json字段的值
    private String getJsonValue(JsonObject dataJson, String key) {
        return dataJson.has(key) && !dataJson.get(key).isJsonNull()
                ? dataJson.get(key).getAsString()
                : "";
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
        popupStage.initOwner(((Node) event.getSource()).getScene().getWindow());
        popupStage.show();
        popupStage.setResizable(false);
    }

    public Label getGenderLabel() {
        return genderLabel;
    }
}
