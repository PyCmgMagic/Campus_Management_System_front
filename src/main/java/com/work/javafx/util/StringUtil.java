package com.work.javafx.util;

public class StringUtil {


    //判断字符串是否为空
    public static boolean isEmpty(String string){
        return string == null || string.trim().equals("");
    }

}
