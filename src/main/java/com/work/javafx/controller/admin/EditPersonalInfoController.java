package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ShowMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class EditPersonalInfoController implements Initializable {
    
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    
    private Stage stage;
    private Gson gson = new Gson();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 加载当前用户信息到表单中
        loadUserInfo();
    }
    
    private void loadUserInfo() {
        // 设置当前信息到表单中
        phoneField.setText(UserSession.getInstance().getPhone());
        emailField.setText(UserSession.getInstance().getEmail());
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        // 获取表单数据
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        
        // 构建请求参数
        Map<String, String> params = new HashMap<>();
        params.put("phone", phone);

        // 构建请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
        
        // 发送更新请求
        NetworkUtils.post("/user/updatePhone", params,null,  new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject responseJson = gson.fromJson(result, JsonObject.class);
                if (responseJson.has("code")) {
                    int code = responseJson.get("code").getAsInt();
                    if (code == 200) {
                        // 更新成功，更新本地数据
                        UserSession.getInstance().setPhone(phone);

                        Platform.runLater(() -> {
                            ShowMessage.showInfoMessage("修改成功", "个人信息已更新成功！");
                            closeWindow();
                            
                            // 刷新主页面数据
                            if (stage != null && stage.getOwner() != null) {
                                Stage ownerStage = (Stage) stage.getOwner();
                                if (ownerStage.getUserData() instanceof PersonalCenterController) {
                                    PersonalCenterController controller = (PersonalCenterController) ownerStage.getUserData();
                                    controller.fetchUserInfo();
                                }
                            }
                        });
                    } else {
                        String msg = responseJson.has("msg") ? responseJson.get("msg").getAsString() : "修改失败";
                        Platform.runLater(() -> ShowMessage.showErrorMessage("修改失败", msg));
                    }
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("网络错误", "无法连接到服务器，请稍后再试"));
            }
        });
    }
    
    private void closeWindow() {
        if (stage != null) {
            stage.close();
        }
    }
} 