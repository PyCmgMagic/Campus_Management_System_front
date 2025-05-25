package com.work.javafx.controller.student;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.work.javafx.controller.teacher.PersonalCenterContent;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ShowMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class UserInfo1 implements Initializable {
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
    @FXML private TextField verifyCodeField;
    @FXML private Button sendCodeButton;

    private Stage stage;
    private static Gson gson = new Gson();
    private String currentTicket = null; // 存储验证码票据
    private String originalEmail; // 存储原始邮箱，用于验证码发送

    public void initialize(URL url, ResourceBundle resourceBundle) {
        //获取个人信息
        fetchUserInfo();
        //显示用户信息
        loadUserInfo_1();

        // 配置验证码输入框
        setupVerifyCodeField();

    }

    /**
     * 配置验证码输入框
     */
    private void setupVerifyCodeField() {
        // 限制验证码输入框只能输入数字，最多6位
        verifyCodeField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*")) {
                verifyCodeField.setText(newText.replaceAll("[^\\d]", ""));
                return;
            }
            if (newText.length() > 6) {
                verifyCodeField.setText(newText.substring(0, 6));
            }
        });
    }

    private void fetchUserInfo() {
        phoneField.setText(UserSession.getInstance().getPhone());
        emailField.setText(UserSession.getInstance().getEmail());

    }

    public void loadUserInfo_1(){
        phoneField.setText(UserSession.getInstance().getPhone());
        emailField.setText(UserSession.getInstance().getEmail());
        originalEmail = UserSession.getInstance().getEmail(); // 保存原始邮箱
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * 发送验证码
     */
    @FXML
    private void handleSendVerifyCode(ActionEvent event) {
        String email = UserSession.getInstance().getEmail();

        if (email.isEmpty()) {
            ShowMessage.showErrorMessage("错误", "无邮箱地址");
            return;
        }

        // 验证邮箱格式
        if (!isValidEmail(email)) {
            ShowMessage.showErrorMessage("错误", "邮箱地址非法");
            return;
        }

        // 显示确认对话框
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认发送");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("确定向邮箱 " + email + " 发送验证码？");

        ButtonType buttonTypeYes = new ButtonType("确定", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo = new ButtonType("取消", ButtonBar.ButtonData.NO);
        confirmAlert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == buttonTypeYes) {
                // 用户点击确定，发送验证码
                sendVerificationCode(email);
            }
        });
    }

    /**
     * 实际发送验证码的方法
     */
    private void sendVerificationCode(String email) {
        // 禁用发送按钮，防止重复点击
        sendCodeButton.setDisable(true);
        sendCodeButton.setText("发送中...");

        Map<String, String> params = new HashMap<>();
        params.put("email", email);

        NetworkUtils.get("/verify/getCode", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                Platform.runLater(() -> {
                    try {
                        JsonObject res = gson.fromJson(result, JsonObject.class);
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            // 获取ticket
                            if (res.has("data")) {
                                currentTicket = res.get("data").getAsString();
                                ShowMessage.showInfoMessage("成功", "验证码已发送到您的邮箱，请查收");

                                // 启动倒计时
                                startCountdown();
                            } else {
                                ShowMessage.showErrorMessage("失败", "获取验证码失败，请重试");
                                resetSendButton();
                            }
                        } else {
                            String errorMsg = res.has("msg") ? res.get("msg").getAsString() : "获取验证码失败";
                            ShowMessage.showErrorMessage("失败", errorMsg);
                            resetSendButton();
                        }
                    } catch (Exception e) {
                        ShowMessage.showErrorMessage("错误", "解析响应失败");
                        resetSendButton();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    ShowMessage.showErrorMessage("请求失败", "网络错误：" + e.getMessage());
                    resetSendButton();
                });
            }
        });
    }

    /**
     * 启动发送验证码按钮的倒计时
     */
    private void startCountdown() {
        new Thread(() -> {
            for (int i = 20; i > 0; i--) {
                final int count = i;
                Platform.runLater(() -> {
                    sendCodeButton.setText(count + "秒后重发");
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            Platform.runLater(this::resetSendButton);
        }).start();
    }

    /**
     * 重置发送按钮状态
     */
    private void resetSendButton() {
        sendCodeButton.setDisable(false);
        sendCodeButton.setText("发送验证码");
    }

    /**
     * 验证邮箱格式
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // 左侧取消按钮
    @FXML
    private void handleCancelLeft(ActionEvent event) {
        closeWindow();
    }

    // 保存按钮的事件处理方法
    @FXML
    private void handleSaveRight(ActionEvent event) {
        String phoneNumber = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String verifyCode = verifyCodeField.getText().trim();

        // 检查必填字段
        if ( email.isEmpty()) {
            ShowMessage.showErrorMessage("错误", "请填写邮箱地址");
            return;
        }

        // 如果邮箱有变化，需要验证验证码
        if (!email.equals(originalEmail)) {
            if (verifyCode.isEmpty()) {
                ShowMessage.showErrorMessage("错误", "邮箱有变化，请输入验证码");
                return;
            }

            if (currentTicket == null) {
                ShowMessage.showErrorMessage("错误", "请先获取验证码");
                return;
            }

            // 先验证验证码，再更新信息
            verifyCodeAndUpdate(phoneNumber, email, verifyCode);
        } else {
            // 邮箱没有变化，直接更新手机号
            updatePhoneOnly(phoneNumber);
        }
    }

    /**
     * 验证验证码并更新信息
     */
    private void verifyCodeAndUpdate(String phoneNumber, String email, String verifyCode) {
        Map<String, String> verifyBody = new HashMap<>();
        verifyBody.put("ticket", currentTicket);
        verifyBody.put("code", verifyCode);
        String body = gson.toJson(verifyBody);
        NetworkUtils.post("/verify/verifyCode", body, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                Platform.runLater(() -> {
                    try {
                        JsonObject res = gson.fromJson(result, JsonObject.class);
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            // 验证码验证成功，更新用户信息
                            updateUserInfo(phoneNumber, email);
                        } else {
                            String errorMsg = res.has("msg") ? res.get("msg").getAsString() : "验证码验证失败";
                            ShowMessage.showErrorMessage("验证失败", errorMsg);
                        }
                    } catch (Exception e) {
                        ShowMessage.showErrorMessage("错误", "解析响应失败");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    ShowMessage.showErrorMessage("请求失败", "验证码验证失败：" + e.getMessage());
                });
            }
        });
    }

    /**
     * 只更新手机号（邮箱未变化时）
     */
    private void updatePhoneOnly(String phoneNumber) {
        Map<String, String> params = new HashMap<>();
        params.put("phone", phoneNumber);

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
                    closeWindow();
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
    }

    /**
     * 更新用户信息（手机号和邮箱）
     */
    private void updateUserInfo(String phoneNumber, String email) {
        // 先更新手机号
        Map<String, String> phoneParams = new HashMap<>();
        phoneParams.put("phone", phoneNumber);

        NetworkUtils.post("/user/updatePhone", phoneParams, null, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) throws IOException {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if(res.has("code") && res.get("code").getAsInt() == 200) {
                    UserSession.getInstance().setPhone(phoneNumber);

                    // 手机号更新成功，继续更新邮箱
                    updateEmail(email);
                } else {
                    ShowMessage.showErrorMessage("更换失败", "手机号更新失败：" + res.get("msg").getAsString());
                }
            }

            @Override
            public void onFailure(Exception e) {
                int index = e.getMessage().indexOf("{");
                JsonObject res = gson.fromJson(e.getMessage().substring(index), JsonObject.class);
                ShowMessage.showErrorMessage("更换失败", "手机号更新失败：" + res.get("msg").getAsString());
            }
        });
    }

    /**
     * 更新邮箱
     */
    private void updateEmail(String email) {
        Map<String, String> emailParams = new HashMap<>();
        emailParams.put("email", email);

        NetworkUtils.post("/user/updateEmail", emailParams, null, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) throws IOException {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if(res.has("code") && res.get("code").getAsInt() == 200) {
                    UserSession.getInstance().setEmail(email);
                    ShowMessage.showInfoMessage("更换成功", "联系方式更新成功\n" +
                            "新手机号：" + UserSession.getInstance().getPhone() + "\n" +
                            "新邮箱：" + email);

                    // 通知监听器保存成功
                    if (listener != null) {
                        listener.onSaveSuccess();
                    }
                    closeWindow();
                } else {
                    ShowMessage.showErrorMessage("更换失败", "邮箱更新失败：" + res.get("msg").getAsString());
                }
            }

            @Override
            public void onFailure(Exception e) {
                int index = e.getMessage().indexOf("{");
                JsonObject res = gson.fromJson(e.getMessage().substring(index), JsonObject.class);
                ShowMessage.showErrorMessage("更换失败", "邮箱更新失败：" + res.get("msg").getAsString());
            }
        });
    }

    // 取消按钮的事件处理方法
    @FXML
    private void handleCancelRight() {
        // 清空输入框
        phoneField.clear();
        emailField.clear();
        verifyCodeField.clear();
        closeWindow();
    }

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