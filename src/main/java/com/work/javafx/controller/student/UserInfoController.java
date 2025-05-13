package com.work.javafx.controller.student;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.NetworkUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class UserInfoController implements Initializable {
    @FXML private Label admissionLabel_1;
    @FXML private Label surnameLabel;
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
    @FXML private Label admissionLabel;
    @FXML private Label graduationLabel;
    @FXML private Label userLabel;
    @FXML private Label useridLabel;

    private Gson gson = new Gson();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setDefaultValues();
        fetchUserInfo();
    }

    private void setDefaultValues() {
        setLabels("加载中...");
    }

    private void setLabels(String text) {
        admissionLabel_1.setText(text);
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
        admissionLabel.setText(text);
        graduationLabel.setText(text);
    }

    private String getFirstLetter(String fullName) {
        if (fullName != null && !fullName.isEmpty()) {
            return fullName.substring(0, 1).toUpperCase();
        }
        return "";
    }

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
            return majorCode;
        }
    }

    private void loadUserInfo() {
        Platform.runLater(() -> {
            String fullName = UserSession.getInstance().getUsername();
            String firstLetter = getFirstLetter(fullName);
            surnameLabel.setText(firstLetter);

            admissionLabel_1.setText( UserSession.getInstance().getAdmission() + "年");
            nameLabel.setText(fullName);
            stuIdLabel.setText(UserSession.getInstance().getSduid());
            genderLabel.setText(UserSession.getInstance().getSex());
            nationLabel.setText(UserSession.getInstance().getNation());
            ethnicLabel.setText(UserSession.getInstance().getEthnic());
            politicsLabel.setText(UserSession.getInstance().getPoliticsStatus());
            majorLabel.setText(convertMajorCode(UserSession.getInstance().getMajor()));
            classLabel.setText(UserSession.getInstance().getSection());
            phoneLabel.setText(UserSession.getInstance().getPhone());
            emailLabel.setText(UserSession.getInstance().getEmail());
            userLabel.setText(fullName);
            useridLabel.setText(UserSession.getInstance().getSduid());
            admissionLabel.setText(UserSession.getInstance().getAdmission() + "年");
            graduationLabel.setText(UserSession.getInstance().getGraduation() + "年");
        });
    }

    public void fetchUserInfo() {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer " + UserSession.getInstance().getToken());

        NetworkUtils.post("/status/getStatusCard", "", header, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject responseJson = gson.fromJson(result, JsonObject.class);
                if (responseJson.has("code") && responseJson.get("code").getAsInt() == 200) {
                    JsonObject dataJson = responseJson.getAsJsonObject("data");
                    JsonObject user = dataJson.getAsJsonObject("user");
                    JsonObject status = dataJson.getAsJsonObject("status");
                    updateUserSession(user);
                    updateUserSessionforStatus(status);
                    loadUserInfo();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> setLabels("加载失败"));
            }
        });
    }

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

    private void updateUserSessionforStatus(JsonObject dataJson) {
        try {
            UserSession session = UserSession.getInstance();
            session.setAdmission(getJsonValue(dataJson, "admission"));
            session.setGraduation(getJsonValue(dataJson, "graduation"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getJsonValue(JsonObject dataJson, String key) {
        return dataJson.has(key) && !dataJson.get(key).isJsonNull()
                ? dataJson.get(key).getAsString()
                : "";
    }

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
        controller.setOnSaveListener(() -> fetchUserInfo());

        popupStage.setScene(scene);
        popupStage.initOwner(((Node) event.getSource()).getScene().getWindow());
        popupStage.show();
        popupStage.setResizable(false);
    }

    @FXML
    public void PasswordChange(ActionEvent event) throws IOException {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("修改密码");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/student/PasswordChange.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/com/work/javafx/css/student/PasswordChange.css")).toExternalForm()
        );

        PasswordChangeController controller = loader.getController();
        controller.setStage(popupStage);

        popupStage.setScene(scene);
        popupStage.initOwner(((Node) event.getSource()).getScene().getWindow());
        popupStage.setResizable(false);
        popupStage.show();
    }


    public Label getGenderLabel() {
        return genderLabel;
    }
}
