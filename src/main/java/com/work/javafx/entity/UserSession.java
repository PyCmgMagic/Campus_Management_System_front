package com.work.javafx.entity;

public class UserSession {
    private static UserSession instance;
    private String token;
    private String username;
    private Integer identity;

    public Integer getIdentity() {
        return identity;
    }

    public void setIdentity(Integer identity) {
        this.identity = identity;
    }

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if(instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // 设置/获取token的方法
    public void setToken(String token) { this.token = token; }
    public String getToken() { return token; }

    // 设置/获取用户名的方法
    public void setUsername(String username) { this.username = username; }
    public String getUsername() { return username; }

    // 清除会话
    public void clearSession() {
        token = null;
        username = null;
        identity = null;
    }
}
//示例
// 登录时保存token
//UserSession.getInstance().setToken("eyJhbGciOiJIUzI1NiIsInR5...");
//UserSession.getInstance().setUsername("user123");
//UserSession.getInstance().setIdentity("user123");

// 在应用其他部分获取token
//String token = UserSession.getInstance().getToken();

// 退出登录时清除
//UserSession.getInstance().clearSession();