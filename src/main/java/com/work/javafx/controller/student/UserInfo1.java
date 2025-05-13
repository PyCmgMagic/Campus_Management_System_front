package com.work.javafx.controller.student;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.work.javafx.controller.teacher.PersonalCenterContent;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ShowMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class UserInfo1 {
    // 新增回调接口
    public interface OnSaveListener {
        void onSaveSuccess() throws IOException;
    }

    private OnSaveListener listener;

    // 设置回调监听器的方法
    public void setOnSaveListener(OnSaveListener listener) {
        this.listener = listener;
    }

    // 右侧字段
    @FXML private TextField phoneField;
    @FXML private TextField emailField;

    private Stage stage;
    private static Gson gson = new Gson();
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //获取个人信息
        fetchUserInfo();
        //显示用户信息
        loadUserInfo_1();
    }

    private void fetchUserInfo() {

    }

    public void loadUserInfo_1(){
        phoneField.setText(UserSession.getInstance().getPhone());
        emailField.setText(UserSession.getInstance().getEmail());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // 左侧取消按钮
    @FXML
    private void handleCancelLeft(ActionEvent event) {
        closeWindow();
    }
    // 保存按钮的事件处理方法
    @FXML
    private void handleSaveRight(ActionEvent event) {
        String phoneNumber = phoneField.getText();
        String email = emailField.getText();
        Map<String, String> params = new HashMap<>();
        params.put("phone", phoneNumber);
        params.put("email", email);

        NetworkUtils.post("/user/updatePhone", params, null, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) throws IOException {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if(res.has("code") && res.get("code").getAsInt() == 200) {
                    UserSession.getInstance().setPhone(phoneNumber);
                    ShowMessage.showInfoMessage("更换成功", "更换手机号成功\n" + "新手机号：" + phoneNumber);

                    // 通知监听器保存成功
                    if (listener != null) {
                        listener.onSaveSuccess();
                    }
                } else {
                    ShowMessage.showErrorMessage("更换失败", res.get("msg").getAsString());
                }
            }

            @Override
            public void onFailure(Exception e) {
                int index = e.getMessage().indexOf("{");
                JsonObject res = gson.fromJson(e.getMessage().substring(index), JsonObject.class);
                ShowMessage.showErrorMessage("更换失败", res.get("msg").getAsString());
            }
        });
        NetworkUtils.post("/user/updateEmail", params, null, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) throws IOException {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if(res.has("code") && res.get("code").getAsInt() == 200) {
                    UserSession.getInstance().setEmail(email);
                    ShowMessage.showInfoMessage("更换成功", "更换邮箱成功\n" + "新邮箱账号：" + email);

                    // 通知监听器保存成功
                    if (listener != null) {
                        listener.onSaveSuccess();
                    }
                } else {
                    ShowMessage.showErrorMessage("更换失败", res.get("msg").getAsString());
                }
            }

            @Override
            public void onFailure(Exception e) {
                int index = e.getMessage().indexOf("{");
                JsonObject res = gson.fromJson(e.getMessage().substring(index), JsonObject.class);
                ShowMessage.showErrorMessage("更换失败", res.get("msg").getAsString());
            }
        });
        closeWindow();
    }


    // 取消按钮的事件处理方法
    @FXML
    private void handleCancelRight() {
        // 清空输入框
        phoneField.clear();
        emailField.clear();
        closeWindow();

        // 可以添加提示信息，告知用户已取消操作
    }
//    // 右侧保存按钮
//    @FXML
//    private void handleSaveRight(ActionEvent event) {
//        String phoneNumber = phoneField.getText();
//        String email = emailField.getText();
//        Map<String, String> params = new HashMap<>();
//        params.put("phone", phoneNumber);
//        // 这里可以添加将信息保存到数据库或文件中的逻辑
//        NetworkUtils.post("/user/updatePhone", params, null, new NetworkUtils.Callback<String>() {
//            @Override
//            public void onSuccess(String result) {
//                JsonObject res = gson.fromJson(result, JsonObject.class);
//                if(res.has("code") && res.get("code").getAsInt() ==200){
//                    UserSession.getInstance().setPhone(phoneNumber);
//                    ShowMessage.showInfoMessage("更换成功","更换手机号成功\n"+ "新手机号："+ phoneNumber);
//
//                }else{
//                    ShowMessage.showErrorMessage("更换失败",res.get("msg").getAsString());
//                }
//
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                int index = e.getMessage().indexOf("{");
//                JsonObject res = gson.fromJson(e.getMessage().substring(index),JsonObject.class);
//                ShowMessage.showErrorMessage("更换失败",res.get("msg").getAsString());
//            }
//        });
//        closeWindow();
//    }


    // 公共关闭窗口方法
    private void closeWindow() {
        if (stage != null) {
            stage.close();
        } else if (phoneField != null) {
            Stage currentStage = (Stage) phoneField.getScene().getWindow();
            currentStage.close();
        }
    }
}
