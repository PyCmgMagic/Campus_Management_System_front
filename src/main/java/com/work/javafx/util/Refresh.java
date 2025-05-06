package com.work.javafx.util;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.work.javafx.entity.UserSession;

import java.util.HashMap;
import java.util.Map;

public class Refresh {
    static Gson gson = new Gson();
    public static void refreshtoken(){
        Map<String,String> header = new HashMap<>();
        String refreshToken = UserSession.getInstance().getRefreshToken();
        header.put("Authorization","Bearer "+ refreshToken);
        NetworkUtils.post("/login/refreshToken", "", header, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject responseJson = gson.fromJson(result, JsonObject.class);
                    if (responseJson.has("code") && responseJson.get("code").getAsInt() == 200) {
                        JsonObject dataObject = responseJson.getAsJsonObject("data");
                        if (dataObject.has("accessToken"))
                        {
                            String accessToken = dataObject.get("accessToken").getAsString();
                            // 存储 Token
                            UserSession.getInstance().setToken(accessToken);
                            String successMsg = responseJson.has("msg") ? responseJson.get("msg").getAsString() : "登录成功";
                            System.out.println("刷新成功: " + successMsg);

                        }
                    } else {
                        String message = responseJson.has("msg") ? responseJson.get("msg").getAsString() : "刷新失败";
                    }
                } catch (Exception e) {
                    System.err.println("处理刷新时出错: " + e.getMessage());
                }
            }
            @Override
            public void onFailure(Exception e) {
                System.err.println("处理刷新时出错: " + e.getMessage());
            }
        });
    }
}
