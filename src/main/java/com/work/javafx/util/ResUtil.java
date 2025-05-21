package com.work.javafx.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ResUtil {
    static Gson gson =new Gson();
    public static String getMsgFromException(Exception e){
        return gson.fromJson(e.getMessage().substring(e.getMessage().indexOf("{")), JsonObject.class).get("msg").getAsString();
    }
}
