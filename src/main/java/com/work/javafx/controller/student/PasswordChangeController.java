package com.work.javafx.controller.student;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.NetworkUtils;
import com.work.javafx.util.ResUtil;
import com.work.javafx.util.ShowMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class PasswordChangeController {

    @FXML private TextField verifyCodeField;
    @FXML private Button sendCodeButton;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private Stage stage;
    private static final Gson gson = new Gson();
    private String currentTicket = null; // 存储验证码票据

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * 发送验证码到邮箱
     */
    @FXML
    public void handleVerifyCodeSend(ActionEvent actionEvent) {
        String email = UserSession.getInstance().getEmail();

        if (email.isEmpty()) {
            ShowMessage.showErrorMessage("错误", "没有邮箱地址");
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
                        String msg = ResUtil.getMsgFromException(e);
                        ShowMessage.showErrorMessage("错误", msg);
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
     * 验证验证码并修改密码
     */
    @FXML
    private void handleSave() {
        String verifyCode = verifyCodeField.getText().trim();
        String oldPwd = oldPasswordField.getText();
        String newPwd = newPasswordField.getText();
        String confirmPwd = confirmPasswordField.getText();

        if (!newPwd.equals(confirmPwd)) {
            ShowMessage.showErrorMessage("错误", "两次输入的新密码不一致");
            return;
        }

        if (currentTicket == null) {
            ShowMessage.showErrorMessage("错误", "请先获取验证码");
            return;
        }

        // 首先验证验证码
        verifyCode(verifyCode, () -> {
            // 验证码验证成功后，调用修改密码接口
            updatePassword(oldPwd, newPwd);
        });
    }

    /**
     * 验证验证码
     */
    private void verifyCode(String code, Runnable onSuccess) {
        Map<String, String> params = new HashMap<>();
        params.put("ticket", currentTicket);
        params.put("code", code);
        String body  = gson.toJson(params);
        NetworkUtils.post("/verify/verifyCode", body, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                Platform.runLater(() -> {
                    try {
                        JsonObject res = gson.fromJson(result, JsonObject.class);
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            System.out.println("验证码核实成功");
                            // 验证码验证成功，执行回调
                            onSuccess.run();
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
     * 修改密码
     */
    private void updatePassword(String oldPwd, String newPwd) {
        Map<String, String> body = new HashMap<>();
        body.put("oldPassword", oldPwd);
        body.put("newPassword", newPwd);

        NetworkUtils.post("/user/updatePassword", body, "", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                Platform.runLater(() -> {
                    try {
                        JsonObject res = gson.fromJson(result, JsonObject.class);
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            ShowMessage.showInfoMessage("成功", "密码修改成功");
                            stage.close();
                        } else {
                            String errorMsg = res.has("msg") ? res.get("msg").getAsString() : "密码修改失败";
                            ShowMessage.showErrorMessage("失败", errorMsg);
                        }
                    } catch (Exception e) {
                        System.out.println("密码保存失败");
                        ShowMessage.showErrorMessage("错误", "解析响应失败");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    System.out.println("密码保存失败");

                    ShowMessage.showErrorMessage("请求失败", "密码修改失败：" + e.getMessage());
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

    @FXML
    private void handleCancel() {
        stage.close();
    }

    public void handleCancelRight(ActionEvent actionEvent) {
        handleCancel();
    }

    public void handleSaveRight(ActionEvent actionEvent) {
        handleSave();
    }
}