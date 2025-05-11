package com.work.javafx.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.NetworkUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class PersonalCenterController implements Initializable {
    
    @FXML private Label nameLabel;
    @FXML private Label idLabel;
    @FXML private Label genderLabel;
    @FXML private Label nationLabel;
    @FXML private Label ethnicLabel;
    @FXML private Label politicsLabel;
    @FXML private Label phone;
    @FXML private Label email;
    @FXML private Label name;
    @FXML private Label firstname;
    @FXML private Label collegeLabel;
    @FXML private Label stuIdLabel;
    @FXML private Label yearLabel;
    @FXML private Label titleLabel;
    @FXML private Label departmentLabel;
    @FXML private Label entryDateLabel;
    @FXML private Label workYearsLabel;
    
    private Gson gson = new Gson();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        
        // 异步获取个人信息
        fetchUserInfo();
    }
    

    
    // 设置多个标签的默认值
    private void setLabels(String text) {
        nameLabel.setText(text);
        idLabel.setText(text);
        stuIdLabel.setText(text);
        genderLabel.setText(text);
        nationLabel.setText(text);
        ethnicLabel.setText(text);
        politicsLabel.setText(text);
        phone.setText(text);
        email.setText(text);
        name.setText(text);
        collegeLabel.setText(text);
        titleLabel.setText(text);
        departmentLabel.setText(text);
        yearLabel.setText(text);
        entryDateLabel.setText(text);
        workYearsLabel.setText(text);
    }
    
    // 提取名字的首字母并返回大写字母
    private String getFirstLetter(String fullName) {
        if (fullName != null && !fullName.isEmpty()) {
            return fullName.substring(0, 1).toUpperCase();
        }
        return "A";  // 默认显示A
    }
    
    // 加载用户信息到UI
    private void loadUserInfo() {
        Platform.runLater(() -> {
            // 设置头像的首字母
            String fullName = UserSession.getInstance().getUsername();
            String firstLetter = getFirstLetter(fullName);
            firstname.setText(firstLetter);
            
            // 设置基本信息
            name.setText(UserSession.getInstance().getUsername());
            nameLabel.setText(UserSession.getInstance().getUsername());
            idLabel.setText(UserSession.getInstance().getSduid());
            stuIdLabel.setText(UserSession.getInstance().getSduid());
            genderLabel.setText(UserSession.getInstance().getSex());
            nationLabel.setText(UserSession.getInstance().getNation());
            ethnicLabel.setText(UserSession.getInstance().getEthnic());
            politicsLabel.setText(UserSession.getInstance().getPoliticsStatus());
            phone.setText(UserSession.getInstance().getPhone());
            email.setText(UserSession.getInstance().getEmail());
            collegeLabel.setText(UserSession.getInstance().getCollege());
            
            // 设置工作信息 (这些信息可能需要从其他API获取，这里设置默认值)
            titleLabel.setText("系统管理员");
//            departmentLabel.setText("信息技术部");
//            yearLabel.setText("2020");
//            entryDateLabel.setText("2020-01-01");
//            workYearsLabel.setText("4年");
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
            session.setPoliticsStatus(getJsonValue(dataJson, "politicsStatus"));
            session.setNation(getJsonValue(dataJson, "nation"));
            session.setEthnic(getJsonValue(dataJson, "ethnic"));
            session.setSex(getJsonValue(dataJson, "sex"));
            session.setSduid(getJsonValue(dataJson, "sduid"));
            session.setCollege(getJsonValue(dataJson, "college"));
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
    
    @FXML
    public void handleEditInfo(ActionEvent event) {
        try {
            // 创建并显示修改信息的对话框
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("修改个人信息");
            
            // 加载修改个人信息的FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/admin/editPersonalInfo.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/com/work/javafx/css/admin/editPersonalInfo.css")).toExternalForm()
            );
            
            // 获取控制器并设置stage
            EditPersonalInfoController controller = loader.getController();
            controller.setStage(popupStage);
            
            // 获取主窗口并设置用户数据为当前控制器，用于回调刷新
            Stage mainStage = (Stage)((Node)event.getSource()).getScene().getWindow();
            mainStage.setUserData(this);
            
            popupStage.setScene(scene);
            popupStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            popupStage.show();
            popupStage.setResizable(false);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
