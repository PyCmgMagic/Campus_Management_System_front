package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ShowMessage;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AddNewAnnouncementController implements Initializable {
    private Stage stage;
    private final Gson gson = new Gson();

    @FXML
    private TextField titleField;
    @FXML
    private TextArea contentArea;
    @FXML
    private ToggleGroup visibleScopeToggleGroup;
    @FXML
    private RadioButton teachersOnlyRadio;
    @FXML
    private RadioButton allUsersRadio;
    @FXML
    private CheckBox isTopCheckBox;
    @FXML
    private Button submitButton;
    @FXML
    private Button cancelButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialization code if needed, e.g., setting default values
        // or adding listeners beyond FXML bindings.
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleSubmit() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        if (title.isEmpty()) {
            ShowMessage.showWarningMessage("输入错误", "公告标题不能为空！");
            titleField.requestFocus();
            return;
        }

        if (content.isEmpty()) {
            ShowMessage.showWarningMessage("输入错误", "公告内容不能为空！");
            contentArea.requestFocus();
            return;
        }

        RadioButton selectedScope = (RadioButton) visibleScopeToggleGroup.getSelectedToggle();
        if (selectedScope == null) {
            ShowMessage.showWarningMessage("选择错误", "请选择公告的可见范围！");
            return;
        }

        int visibleScope = allUsersRadio.isSelected() ? 2 : 1; //
        int isTop = isTopCheckBox.isSelected() ? 1 : 0;

        Map<String, String> params = new HashMap<>();
        params.put("title", title);
        params.put("content", content);
        params.put("visibleScope", String.valueOf(visibleScope));
        params.put("isTop", String.valueOf(isTop));

        // Disable button to prevent multiple submissions
        submitButton.setDisable(true);
        cancelButton.setDisable(true);

        NetworkUtils.post("/notice/set", params, null, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                NetworkUtils.runOnUiThread(() -> {
                    try {
                        JsonObject response = gson.fromJson(result, JsonObject.class);
                        if (response.has("code") && response.get("code").getAsInt() == 200) {
                            ShowMessage.showInfoMessage("成功", "公告发布成功！");
                            if (stage != null) {
                                stage.close();
                            }
                        } else {
                            String msg = response.has("msg") ? response.get("msg").getAsString() : "发布失败，请稍后重试。";
                            ShowMessage.showErrorMessage("发布失败", msg);
                        }
                    } catch (Exception e) {
                        JsonObject res = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")), JsonObject.class);
                        ShowMessage.showErrorMessage("解析错误", res.get("msg").getAsString());
                    }
                    submitButton.setDisable(false);
                    cancelButton.setDisable(false);
                });
            }

            @Override
            public void onFailure(Exception e) {
                NetworkUtils.runOnUiThread(() -> {
                    JsonObject res = gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")), JsonObject.class);
                    ShowMessage.showErrorMessage("解析错误", res.get("msg").getAsString());
                    submitButton.setDisable(false);
                    cancelButton.setDisable(false);
                });
            }
        });
    }

    @FXML
    private void handleCancel() {
        if (stage != null) {
            stage.close();
        }
    }
}
