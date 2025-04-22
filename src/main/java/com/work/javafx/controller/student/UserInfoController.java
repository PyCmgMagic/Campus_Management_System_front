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
    @FXML private Label idLabel;
    @FXML private Label nationLabel;
    @FXML private Label politicsLabel;
    @FXML private Label majorLabel;
    @FXML private Label classLabel;
    @FXML private Label entryLabel;
    @FXML private Label graduationLabel;
    Gson gson = new Gson();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //获取个人信息
        try {
            fetchUserInfo();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //显示用户信息
        loadUserInfo();
    }
//加载用户信息
    public void loadUserInfo(){
        nameLabel.setText(UserSession.getInstance().getUsername());
        stuIdLabel.setText(UserSession.getInstance().getSduid());
        genderLabel.setText(UserSession.getInstance().getSex());
        idLabel.setText(UserSession.getInstance().getSection());
        nationLabel.setText(UserSession.getInstance().getEthnic());
        politicsLabel.setText(UserSession.getInstance().getPoliticsStatus());
        majorLabel.setText(UserSession.getInstance().getMajor());
        classLabel.setText(UserSession.getInstance().getSection());
        //entryLabel.setText(UserSession.getInstance().);
    }
    //获取个人信息
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

            }
        });
    }
    public void UserInfo1(ActionEvent event) throws IOException {
        // 创建新窗口
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("修改个人信息");

        // 加载 FXML 文件
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/work/javafx/student/UserInfo_1.fxml"));
        Parent root = loader.load();

        // 创建 Scene 并加载样式
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/com/work/javafx/css/student/UserInfo_1.css")).toExternalForm()
        );

        // 获取控制器并传递 Stage
        UserInfo1 controller = loader.getController();
        controller.setStage(popupStage);  // 你要在 ChangeUserInfo 类中加个 setStage(Stage) 方法

        // 设置 Scene 与所属窗口
        popupStage.setScene(scene);
        popupStage.initOwner(((Node)event.getSource()).getScene().getWindow());

        // 显示弹窗
        popupStage.show();

        // 限制窗口大小
        popupStage.setResizable(false);
    }


    public Label getGenderLabel() {
        return genderLabel;
    }
}
