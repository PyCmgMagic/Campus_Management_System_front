package com.work.javafx.controller.teacher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.work.javafx.controller.student.PasswordChangeController;
import com.work.javafx.controller.student.UserInfo1;
import com.work.javafx.entity.UserSession;
import com.work.javafx.util.NetworkUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.time.LocalDate;
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
import org.apache.xmlbeans.GDate;

import java.io.IOException;

public class PersonalCenterContent implements Initializable {
    @FXML private Label nameLabel;
    @FXML private Label stuIdLabel;
    @FXML private Label genderLabel;
    @FXML private Label nationLabel;
    @FXML private Label politicsLabel;
    @FXML private Label phone;
    @FXML private Label email;
    @FXML private Label name;
    @FXML private Label idlabel;
    @FXML private Label yearLabel;
    @FXML private Label yearjoining;
    @FXML private Label nation;
    @FXML private Label firstname;


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

    // 提取名字的首字母并返回大写字母
    private String getFirstLetter(String fullName) {
        if (fullName != null && !fullName.isEmpty()) {
            return fullName.substring(0, 1).toUpperCase();
        }
        return "";  // 如果名字为空，返回空字符串
    }
    //加载用户信息
    public void loadUserInfo(){
        String fullName = UserSession.getInstance().getUsername();
        String firstLetter = getFirstLetter(fullName);  // 调用提取首字母的方法
        firstname.setText(firstLetter);  // 更新头像显示首字母
        nameLabel.setText(UserSession.getInstance().getUsername());
        genderLabel.setText(UserSession.getInstance().getSex());
        stuIdLabel.setText(UserSession.getInstance().getSduid());
        System.out.println(UserSession.getInstance().getSduid());
        nationLabel.setText(UserSession.getInstance().getEthnic() );
        politicsLabel.setText(UserSession.getInstance().getPoliticsStatus() );
        phone.setText(UserSession.getInstance().getPhone() );
        email.setText(UserSession.getInstance().getEmail());
        name.setText(UserSession.getInstance().getUsername());
        idlabel.setText(UserSession.getInstance().getSduid());
        // 获取 yearLabel 显示的内容
        String inductionYear = UserSession.getInstance().getInduction();

        try {
            int currentYear = LocalDate.now().getYear();
            int inductionYearInt = Integer.parseInt(inductionYear);
            int yearsOfWork = currentYear - inductionYearInt;

            // 设置计算结果到 yearjoining Label
            yearjoining.setText(String.valueOf(yearsOfWork)+"年" );
        } catch (NumberFormatException e) {
            yearjoining.setText("未知");
        }

        yearLabel.setText(UserSession.getInstance().getInduction());
        nation.setText(UserSession.getInstance().getNation());


    }
    //安全获取数据
    private String safeGet(JsonObject data,String str){
        String d = "";
        try{
            d = data.get(str).getAsString();
        }catch (Exception ignored){
        }
        return d;
    }
    //获取个人信息
    //获取个人信息
    public  void fetchUserInfo() throws IOException {
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
                        if (dataJson != null) {
                            String username = safeGet(dataJson,"username");
                            String email = safeGet(dataJson,"email");
                            String phone = safeGet(dataJson,"phone");
                            String sex = safeGet(dataJson,"sex");
                            String section = safeGet(dataJson,"section");
                            String nation = safeGet(dataJson,"nation");
                            String ethnic = safeGet(dataJson,"ethnic");

                            String sduid = safeGet(dataJson,"sduid");
                            System.out.println(sduid);
                            String major =safeGet(dataJson,"major");
                            String yearLabel= sduid.substring(0,4);
                            String nationLabel = safeGet(dataJson,"nation");
                            String politicsLabel = safeGet(dataJson,"politicsStatus");


                            UserSession.getInstance().setUsername(username);
                            UserSession.getInstance().setInduction(yearLabel);
                            UserSession.getInstance().setEmail(email);
                            UserSession.getInstance().setPhone(phone);
                            UserSession.getInstance().setSex(sex);
                            UserSession.getInstance().setSection(section);
                            UserSession.getInstance().setNation(nation);
                            UserSession.getInstance().setEthnic(ethnic);
                            UserSession.getInstance().setSduid(sduid);
                            UserSession.getInstance().setMajor(major);
                            UserSession.getInstance().setNation(nationLabel);
                            UserSession.getInstance().setPoliticsStatus(politicsLabel);
                        }
                    }
                }
                    loadUserInfo();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void UserInfo1(ActionEvent event) throws IOException {
        // 创建新窗口（模态）
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
        controller.setStage(popupStage);

        // 设置回调监听器
        controller.setOnSaveListener(new UserInfo1.OnSaveListener() {
            @Override
            public void onSaveSuccess() throws IOException {
                // 刷新用户信息
                fetchUserInfo();
            }
        });

        // 设置 Scene 与所属窗口
        popupStage.setScene(scene);
        popupStage.initOwner(((Node) event.getSource()).getScene().getWindow());

        // 显示弹窗
        popupStage.showAndWait();

        // 限制窗口大小
        popupStage.setResizable(false);
    }


}
