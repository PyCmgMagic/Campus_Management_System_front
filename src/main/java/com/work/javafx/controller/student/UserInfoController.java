package com.work.javafx.controller.student;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.NetworkUtils;
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
        try {
            fetchUserInfo();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loadUserInfo();
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

    public void loadUserInfo(){
        nameLabel.setText(UserSession.getInstance().getUsername());
        stuIdLabel.setText(UserSession.getInstance().getSduid());
        genderLabel.setText(UserSession.getInstance().getSex());
        nationLabel.setText(UserSession.getInstance().getNation());
        ethnicLabel.setText(UserSession.getInstance().getEthnic());
        politicsLabel.setText(UserSession.getInstance().getPoliticsStatus());
        majorLabel.setText(convertMajorCode(UserSession.getInstance().getMajor())); // 使用转换后的专业名称
        classLabel.setText(UserSession.getInstance().getSection());
        phoneLabel.setText(UserSession.getInstance().getPhone());
        emailLabel.setText(UserSession.getInstance().getEmail());
        userLabel.setText(UserSession.getInstance().getUsername());
        useridLabel.setText(UserSession.getInstance().getSduid());
    }

    public void fetchUserInfo() throws IOException {
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
                        if(dataJson.has("phone")){
                            try{
                                String phone = dataJson.get("phone").getAsString();
                                UserSession.getInstance().setPhone(phone);
                            }catch (Exception e){
                                UserSession.getInstance().setPhone("");
                            }
                        }
                        if(dataJson.has("email")){
                            try{
                                String email = dataJson.get("email").getAsString();
                                UserSession.getInstance().setEmail(email);
                            }catch (Exception e){
                                UserSession.getInstance().setEmail("");
                            }
                        }
                        if(dataJson.has("username")){
                            try {
                                String username = dataJson.get("username").getAsString();
                                UserSession.getInstance().setUsername(username);
                            }catch (Exception e){
                                UserSession.getInstance().setUsername("");
                            }
                        }
                        if(dataJson.has("section")){
                            try{
                                String section = dataJson.get("section").getAsString();
                                UserSession.getInstance().setSection(section);
                            }catch (Exception e){
                                UserSession.getInstance().setSection("");
                            }
                        }
                        if(dataJson.has("politicsStatus")){
                            try{
                                String politicsStatus = dataJson.get("politicsStatus").getAsString();
                                UserSession.getInstance().setPoliticsStatus(politicsStatus);
                            }catch (Exception e){
                                UserSession.getInstance().setPoliticsStatus("");
                            }
                        }
                        if (dataJson.has("nation")){
                            try {
                                String nation = dataJson.get("nation").getAsString();
                                UserSession.getInstance().setNation(nation);
                            }catch (Exception e){
                                UserSession.getInstance().setNation("");
                            }
                        }
                        if(dataJson.has("ethnic")){
                            try{
                                String ethnic = dataJson.get("ethnic").getAsString();
                                UserSession.getInstance().setEthnic(ethnic);
                            }catch (Exception e){
                                UserSession.getInstance().setEthnic("");
                            }
                        }
                        if(dataJson.has("sex")){
                            try{
                                String sex = dataJson.get("sex").getAsString();
                                UserSession.getInstance().setSex(sex);
                            }catch (Exception e){
                                UserSession.getInstance().setSex("");
                            }
                        }
                        if(dataJson.has("major")){
                            try{
                                String major = dataJson.get("major").getAsString();
                                UserSession.getInstance().setMajor(major);
                            }catch (Exception e){
                                UserSession.getInstance().setMajor("");
                            }
                        }
                        if(dataJson.has("sduid")){
                            try{
                                String sduid = dataJson.get("sduid").getAsString();
                                UserSession.getInstance().setSduid(sduid);
                            }catch (Exception e){
                                UserSession.getInstance().setSduid("");
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                // 错误处理
            }
        });
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

        popupStage.setScene(scene);
        popupStage.initOwner(((Node)event.getSource()).getScene().getWindow());
        popupStage.show();
        popupStage.setResizable(false);
    }

    public Label getGenderLabel() {
        return genderLabel;
    }
}
