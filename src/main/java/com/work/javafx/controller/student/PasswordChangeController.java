package com.work.javafx.controller.student;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ShowMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class PasswordChangeController {

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private Stage stage;
    private static final Gson gson = new Gson();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleSave() {
        String oldPwd = oldPasswordField.getText();
        String newPwd = newPasswordField.getText();
        String confirmPwd = confirmPasswordField.getText();

        if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
            ShowMessage.showErrorMessage("错误", "请填写所有字段");
            return;
        }

        if (!newPwd.equals(confirmPwd)) {
            ShowMessage.showErrorMessage("错误", "两次输入的新密码不一致");
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("oldPassword", oldPwd);
        body.put("newPassword", newPwd);

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());

        NetworkUtils.post("/user/updatePassword", body, headers.toString(), new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    ShowMessage.showInfoMessage("成功", "密码修改成功");
                    Platform.runLater(() -> stage.close());
                } else {
                    ShowMessage.showErrorMessage("失败", res.get("msg").getAsString());
                }
            }

            @Override
            public void onFailure(Exception e) {
                ShowMessage.showErrorMessage("请求失败", e.getMessage());
            }
        });
    }


    @FXML
    private void handleCancel() {
        stage.close();
    }

    public void handleCancelRight(ActionEvent actionEvent) {
    }

    public void handleSaveRight(ActionEvent actionEvent) {
    }

}
